package org.greenstone.backend.publiccontent;

import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.ProjectImage;
import org.greenstone.backend.persistence.entity.ProjectImagePhase;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.repository.PortfolioProjectRepository;
import org.greenstone.backend.persistence.repository.ProjectImageRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PublicProjectService {

    private final PortfolioProjectRepository projectRepository;
    private final ProjectImageRepository imageRepository;
    private final FileStorageService storageService;

    public PublicProjectService(
            PortfolioProjectRepository projectRepository,
            ProjectImageRepository imageRepository,
            FileStorageService storageService
    ) {
        this.projectRepository = projectRepository;
        this.imageRepository = imageRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<PublicProjectResponse> publishedProjects() {
        return projectRepository.findAllByStatusOrderByFeaturedDescCompletedOnDesc(PublicationStatus.PUBLISHED).stream()
                .map(project -> {
                    var images = imageRepository.findAllByProjectIdOrderByDisplayOrderAsc(project.getId());
                    var image = preferredImage(images);
                    return new PublicProjectResponse(
                            project.getSlug(), project.getTitle(),
                            project.getService() == null ? "Painting project" : project.getService().getTitle(),
                            project.getLocation(), project.getSummary(), highlights(project.getDescription()),
                            image == null ? null : publicImageUrl(project.getSlug(), image),
                            image == null ? "" : image.getAltText()
                    );
                })
                .filter(project -> project.imageUrl() != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttachmentDownload loadPublishedImage(String slug, UUID imageId) {
        var project = projectRepository.findBySlug(slug)
                .filter(candidate -> candidate.getStatus() == PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Project image was not found."));
        var image = imageRepository.findById(imageId)
                .filter(candidate -> candidate.getProject().getId().equals(project.getId()))
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

    private ProjectImage preferredImage(List<ProjectImage> images) {
        return images.stream().filter(image -> image.getPhase() == ProjectImagePhase.AFTER).findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    private String publicImageUrl(String slug, ProjectImage image) {
        return image.getObjectKey().startsWith("static:")
                ? image.getObjectKey().substring("static:".length())
                : "/api/projects/" + slug + "/images/" + image.getId();
    }

    private List<String> highlights(String description) {
        return Arrays.stream(description.split("\\R"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .limit(4)
                .toList();
    }
}
