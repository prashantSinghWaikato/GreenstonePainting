package org.greenstone.backend.admin.content;

import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.PortfolioProject;
import org.greenstone.backend.persistence.entity.ProjectActivity;
import org.greenstone.backend.persistence.entity.ProjectActivityType;
import org.greenstone.backend.persistence.entity.ProjectImage;
import org.greenstone.backend.persistence.entity.ProjectImagePhase;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.PortfolioProjectRepository;
import org.greenstone.backend.persistence.repository.ProjectActivityRepository;
import org.greenstone.backend.persistence.repository.ProjectImageRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.UploadValidationException;
import org.greenstone.backend.web.WorkflowConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminProjectService {

    private static final int MAXIMUM_IMAGES = 8;

    private final PortfolioProjectRepository projectRepository;
    private final ProjectImageRepository imageRepository;
    private final ProjectActivityRepository activityRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final AdminUserRepository userRepository;
    private final FileStorageService storageService;

    public AdminProjectService(
            PortfolioProjectRepository projectRepository,
            ProjectImageRepository imageRepository,
            ProjectActivityRepository activityRepository,
            ServiceOfferingRepository serviceRepository,
            AdminUserRepository userRepository,
            FileStorageService storageService
    ) {
        this.projectRepository = projectRepository;
        this.imageRepository = imageRepository;
        this.activityRepository = activityRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public AdminProjectDashboardResponse dashboard() {
        var projects = projectRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toSummary)
                .toList();
        var services = serviceRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(service -> new ProjectServiceOptionResponse(service.getSlug(), service.getTitle()))
                .toList();
        return new AdminProjectDashboardResponse(projects, services);
    }

    @Transactional(readOnly = true)
    public AdminProjectDetailResponse detail(UUID projectId) {
        return toDetail(findProject(projectId));
    }

    @Transactional
    public AdminProjectDetailResponse create(SaveAdminProjectRequest request, String actorEmail) {
        var actor = findActor(actorEmail);
        var slug = uniqueSlug(request.title());
        var project = new PortfolioProject(slug, clean(request.title()), clean(request.summary()), clean(request.description()));
        apply(project, request);
        project = projectRepository.saveAndFlush(project);
        activityRepository.save(new ProjectActivity(
                project, actor, ProjectActivityType.CREATED,
                actor.getDisplayName() + " created the draft project “" + project.getTitle() + "”."
        ));
        return toDetail(project);
    }

    @Transactional
    public AdminProjectDetailResponse update(UUID projectId, SaveAdminProjectRequest request, String actorEmail) {
        var project = findProject(projectId);
        verifyVersion(project, request.version());
        requireDraft(project, "Unpublish this project before editing its public content.");
        var actor = findActor(actorEmail);
        apply(project, request);
        projectRepository.flush();
        activityRepository.save(new ProjectActivity(
                project, actor, ProjectActivityType.UPDATED,
                actor.getDisplayName() + " updated the project details."
        ));
        return toDetail(project);
    }

    @Transactional
    public AdminProjectDetailResponse setPublication(
            UUID projectId,
            SetProjectPublicationRequest request,
            String actorEmail
    ) {
        if (request.status() != PublicationStatus.DRAFT && request.status() != PublicationStatus.PUBLISHED) {
            throw new WorkflowConflictException("Projects can only be moved between Draft and Published here.");
        }
        var project = findProject(projectId);
        verifyVersion(project, request.version());
        if (project.getStatus() == request.status()) return toDetail(project);
        if (request.status() == PublicationStatus.PUBLISHED && imageRepository.countByProjectId(projectId) == 0) {
            throw new WorkflowConflictException("Add at least one project image before publishing.");
        }
        var actor = findActor(actorEmail);
        project.setStatus(request.status());
        project.setPublishedAt(request.status() == PublicationStatus.PUBLISHED
                ? OffsetDateTime.now(ZoneOffset.UTC)
                : null);
        projectRepository.flush();
        var published = request.status() == PublicationStatus.PUBLISHED;
        activityRepository.save(new ProjectActivity(
                project,
                actor,
                published ? ProjectActivityType.PUBLISHED : ProjectActivityType.UNPUBLISHED,
                actor.getDisplayName() + (published ? " published the project." : " returned the project to Draft.")
        ));
        return toDetail(project);
    }

    @Transactional
    public AdminProjectDetailResponse addImage(
            UUID projectId,
            MultipartFile file,
            String altText,
            ProjectImagePhase phase,
            String actorEmail
    ) {
        var project = findProject(projectId);
        requireDraft(project, "Unpublish this project before changing its images.");
        if (imageRepository.countByProjectId(projectId) >= MAXIMUM_IMAGES) {
            throw new UploadValidationException("A project can include up to 8 images.");
        }
        if (altText == null || altText.isBlank() || altText.trim().length() > 250) {
            throw new UploadValidationException("Describe the image in 250 characters or fewer.");
        }
        if (phase == null) throw new UploadValidationException("Choose Before, After, or Gallery for this image.");

        var stored = storageService.storeProjectImage(projectId, file);
        try {
            var filename = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                    ? "project-image"
                    : file.getOriginalFilename().trim();
            var image = new ProjectImage(
                    project, stored.objectKey(), altText.trim(), filename, stored.contentType(), stored.sizeBytes()
            );
            image.setPhase(phase);
            image.setDisplayOrder((int) imageRepository.countByProjectId(projectId));
            imageRepository.saveAndFlush(image);
            var actor = findActor(actorEmail);
            activityRepository.save(new ProjectActivity(
                    project, actor, ProjectActivityType.IMAGE_ADDED,
                    actor.getDisplayName() + " added “" + filename + "”."
            ));
            return toDetail(project);
        } catch (RuntimeException exception) {
            storageService.delete(stored.objectKey());
            throw exception;
        }
    }

    @Transactional
    public AdminProjectDetailResponse removeImage(UUID projectId, UUID imageId, String actorEmail) {
        var project = findProject(projectId);
        requireDraft(project, "Unpublish this project before changing its images.");
        var image = imageRepository.findById(imageId)
                .filter(candidate -> candidate.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project image was not found."));
        var filename = image.getOriginalFilename();
        var objectKey = image.getObjectKey();
        imageRepository.delete(image);
        imageRepository.flush();
        if (!objectKey.startsWith("static:")) storageService.delete(objectKey);
        var actor = findActor(actorEmail);
        activityRepository.save(new ProjectActivity(
                project, actor, ProjectActivityType.IMAGE_REMOVED,
                actor.getDisplayName() + " removed “" + filename + "”."
        ));
        return toDetail(project);
    }

    @Transactional(readOnly = true)
    public AttachmentDownload loadImage(UUID projectId, UUID imageId) {
        var image = imageRepository.findById(imageId)
                .filter(candidate -> candidate.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project image was not found."));
        if (image.getObjectKey().startsWith("static:")) {
            throw new ResourceNotFoundException("This image is served by the website.");
        }
        return new AttachmentDownload(
                storageService.load(image.getObjectKey()),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getSizeBytes()
        );
    }

    private void apply(PortfolioProject project, SaveAdminProjectRequest request) {
        project.setTitle(clean(request.title()));
        project.setSummary(clean(request.summary()));
        project.setDescription(clean(request.description()));
        project.setLocation(clean(request.location()));
        project.setCompletedOn(request.completedOn());
        project.setFeatured(request.featured());
        if (request.serviceSlug() == null || request.serviceSlug().isBlank()) {
            project.setService(null);
        } else {
            project.setService(serviceRepository.findBySlugAndActiveTrue(request.serviceSlug().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("The selected service is unavailable.")));
        }
    }

    private AdminProjectSummaryResponse toSummary(PortfolioProject project) {
        var images = imageRepository.findAllByProjectIdOrderByDisplayOrderAsc(project.getId());
        var image = images.isEmpty() ? null : images.get(0);
        return new AdminProjectSummaryResponse(
                project.getId(), project.getSlug(), project.getTitle(), project.getLocation(),
                project.getService() == null ? null : project.getService().getTitle(),
                project.getStatus(), project.isFeatured(), image == null ? null : adminImageUrl(project, image),
                project.getUpdatedAt(), project.getVersion()
        );
    }

    private AdminProjectDetailResponse toDetail(PortfolioProject project) {
        var images = imageRepository.findAllByProjectIdOrderByDisplayOrderAsc(project.getId()).stream()
                .map(image -> new AdminProjectImageResponse(
                        image.getId(), image.getAltText(), image.getPhase(), image.getDisplayOrder(),
                        adminImageUrl(project, image), image.getOriginalFilename(), image.getContentType(), image.getSizeBytes()
                ))
                .toList();
        var activities = activityRepository.findTop30ByProjectIdOrderByCreatedAtDesc(project.getId()).stream()
                .map(activity -> new AdminProjectActivityResponse(
                        activity.getId(), activity.getActivityType(), activity.getSummary(),
                        activity.getActor().getDisplayName(), activity.getCreatedAt()
                ))
                .toList();
        return new AdminProjectDetailResponse(
                project.getId(), project.getSlug(), project.getTitle(), project.getSummary(), project.getDescription(),
                project.getLocation(), project.getCompletedOn(),
                project.getService() == null ? null : project.getService().getSlug(),
                project.getService() == null ? null : project.getService().getTitle(),
                project.getStatus(), project.isFeatured(), project.getPublishedAt(),
                project.getCreatedAt(), project.getUpdatedAt(), project.getVersion(), images, activities
        );
    }

    private String adminImageUrl(PortfolioProject project, ProjectImage image) {
        return image.getObjectKey().startsWith("static:")
                ? image.getObjectKey().substring("static:".length())
                : "/api/admin/content/projects/" + project.getId() + "/images/" + image.getId() + "/file";
    }

    private PortfolioProject findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project was not found."));
    }

    private AdminUser findActor(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
    }

    private void requireDraft(PortfolioProject project, String message) {
        if (project.getStatus() != PublicationStatus.DRAFT) throw new WorkflowConflictException(message);
    }

    private void verifyVersion(PortfolioProject project, long version) {
        if (project.getVersion() != version) {
            throw new WorkflowConflictException("This project changed in another session. Reload it before continuing.");
        }
    }

    private String uniqueSlug(String title) {
        var base = clean(title).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (base.isBlank()) base = "project";
        if (base.length() > 110) base = base.substring(0, 110).replaceAll("-$", "");
        var slug = base;
        while (projectRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
