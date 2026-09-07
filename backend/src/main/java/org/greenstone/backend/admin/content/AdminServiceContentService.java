package org.greenstone.backend.admin.content;

import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.entity.ServiceOffering;
import org.greenstone.backend.persistence.entity.ServiceOfferingActivity;
import org.greenstone.backend.persistence.entity.ServiceOfferingActivityType;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingActivityRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AdminServiceContentService {

    private final ServiceOfferingRepository serviceRepository;
    private final ServiceOfferingActivityRepository activityRepository;
    private final AdminUserRepository userRepository;
    private final FileStorageService storageService;

    public AdminServiceContentService(
            ServiceOfferingRepository serviceRepository,
            ServiceOfferingActivityRepository activityRepository,
            AdminUserRepository userRepository,
            FileStorageService storageService
    ) {
        this.serviceRepository = serviceRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<AdminServiceSummaryResponse> list() {
        return serviceRepository.findAllByOrderByDisplayOrderAsc().stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public AdminServiceDetailResponse detail(UUID serviceId) {
        return toDetail(findService(serviceId));
    }

    @Transactional
    public AdminServiceDetailResponse update(UUID serviceId, SaveAdminServiceRequest request, String actorEmail) {
        var service = findService(serviceId);
        verifyVersion(service, request.version());
        requireDraft(service, "Unpublish this service before editing its public content.");
        service.setTitle(clean(request.title()));
        service.setLabel(clean(request.label()));
        service.setSummary(clean(request.summary()));
        service.setDescription(clean(request.description()));
        service.setInclusions(request.inclusions().stream().map(this::clean).reduce((a, b) -> a + "\n" + b).orElse(""));
        service.setNote(clean(request.note()));
        service.setDisplayOrder(request.displayOrder());
        serviceRepository.flush();
        var actor = findActor(actorEmail);
        activityRepository.save(new ServiceOfferingActivity(
                service, actor, ServiceOfferingActivityType.UPDATED,
                actor.getDisplayName() + " updated the service content."
        ));
        return toDetail(service);
    }

    @Transactional
    public AdminServiceDetailResponse setPublication(UUID serviceId, SetProjectPublicationRequest request, String actorEmail) {
        if (request.status() != PublicationStatus.DRAFT && request.status() != PublicationStatus.PUBLISHED) {
            throw new WorkflowConflictException("Services can only be moved between Draft and Published here.");
        }
        var service = findService(serviceId);
        verifyVersion(service, request.version());
        if (service.getStatus() == request.status()) return toDetail(service);
        if (request.status() == PublicationStatus.PUBLISHED && service.getFeaturedImageObjectKey() == null) {
            throw new WorkflowConflictException("Add a featured image before publishing this service.");
        }
        var published = request.status() == PublicationStatus.PUBLISHED;
        service.setStatus(request.status());
        service.setActive(published);
        service.setPublishedAt(published ? OffsetDateTime.now(ZoneOffset.UTC) : null);
        serviceRepository.flush();
        var actor = findActor(actorEmail);
        activityRepository.save(new ServiceOfferingActivity(
                service, actor,
                published ? ServiceOfferingActivityType.PUBLISHED : ServiceOfferingActivityType.UNPUBLISHED,
                actor.getDisplayName() + (published ? " published the service." : " returned the service to Draft.")
        ));
        return toDetail(service);
    }

    @Transactional
    public AdminServiceDetailResponse setImage(UUID serviceId, MultipartFile file, String altText, String actorEmail) {
        var service = findService(serviceId);
        requireDraft(service, "Unpublish this service before changing its featured image.");
        validateAlt(altText);
        var stored = storageService.storeServiceImage(serviceId, file);
        var oldObjectKey = service.getFeaturedImageObjectKey();
        try {
            service.setFeaturedImage(stored.objectKey(), altText.trim(), safeFilename(file.getOriginalFilename()), stored.contentType(), stored.sizeBytes());
            serviceRepository.flush();
            var actor = findActor(actorEmail);
            activityRepository.save(new ServiceOfferingActivity(
                    service, actor, ServiceOfferingActivityType.IMAGE_CHANGED,
                    actor.getDisplayName() + " updated the featured image."
            ));
            if (oldObjectKey != null && !oldObjectKey.startsWith("static:")) storageService.delete(oldObjectKey);
            return toDetail(service);
        } catch (RuntimeException exception) {
            storageService.delete(stored.objectKey());
            throw exception;
        }
    }

    @Transactional
    public AdminServiceDetailResponse removeImage(UUID serviceId, String actorEmail) {
        var service = findService(serviceId);
        requireDraft(service, "Unpublish this service before changing its featured image.");
        if (service.getFeaturedImageObjectKey() == null) return toDetail(service);
        var objectKey = service.getFeaturedImageObjectKey();
        service.clearFeaturedImage();
        serviceRepository.flush();
        if (!objectKey.startsWith("static:")) storageService.delete(objectKey);
        var actor = findActor(actorEmail);
        activityRepository.save(new ServiceOfferingActivity(
                service, actor, ServiceOfferingActivityType.IMAGE_REMOVED,
                actor.getDisplayName() + " removed the featured image."
        ));
        return toDetail(service);
    }

    @Transactional(readOnly = true)
    public AttachmentDownload loadImage(UUID serviceId) {
        var service = findService(serviceId);
        if (service.getFeaturedImageObjectKey() == null || service.getFeaturedImageObjectKey().startsWith("static:")) {
            throw new ResourceNotFoundException("This image is served by the website.");
        }
        return new AttachmentDownload(
                storageService.load(service.getFeaturedImageObjectKey()),
                service.getFeaturedImageFilename(), service.getFeaturedImageContentType(), service.getFeaturedImageSizeBytes()
        );
    }

    private AdminServiceSummaryResponse toSummary(ServiceOffering service) {
        return new AdminServiceSummaryResponse(
                service.getId(), service.getSlug(), service.getTitle(), service.getLabel(), service.getDisplayOrder(),
                service.getStatus(), imageUrl(service, true), service.getUpdatedAt(), service.getVersion()
        );
    }

    private AdminServiceDetailResponse toDetail(ServiceOffering service) {
        var activities = activityRepository.findTop30ByServiceIdOrderByCreatedAtDesc(service.getId()).stream()
                .map(activity -> new AdminServiceActivityResponse(
                        activity.getId(), activity.getActivityType(), activity.getSummary(),
                        activity.getActor().getDisplayName(), activity.getCreatedAt()
                )).toList();
        return new AdminServiceDetailResponse(
                service.getId(), service.getSlug(), service.getTitle(), service.getLabel(), service.getSummary(),
                service.getDescription(), inclusions(service), service.getNote(), service.getDisplayOrder(),
                service.getStatus(), imageUrl(service, true), service.getFeaturedImageAlt(),
                service.getFeaturedImageFilename(), service.getFeaturedImageSizeBytes(), service.getPublishedAt(),
                service.getCreatedAt(), service.getUpdatedAt(), service.getVersion(), activities
        );
    }

    private List<String> inclusions(ServiceOffering service) {
        return Arrays.stream(service.getInclusions().split("\\R")).map(String::trim).filter(value -> !value.isBlank()).toList();
    }

    private String imageUrl(ServiceOffering service, boolean admin) {
        var key = service.getFeaturedImageObjectKey();
        if (key == null) return null;
        if (key.startsWith("static:")) return key.substring("static:".length());
        return admin ? "/api/admin/content/services/" + service.getId() + "/image/file" : "/api/services/" + service.getSlug() + "/image";
    }

    private ServiceOffering findService(UUID serviceId) {
        return serviceRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service was not found."));
    }

    private AdminUser findActor(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
    }

    private void verifyVersion(ServiceOffering service, long version) {
        if (service.getVersion() != version) throw new WorkflowConflictException("This service changed in another session. Reload it before continuing.");
    }

    private void requireDraft(ServiceOffering service, String message) {
        if (service.getStatus() != PublicationStatus.DRAFT) throw new WorkflowConflictException(message);
    }

    private void validateAlt(String altText) {
        if (altText == null || altText.isBlank() || altText.trim().length() > 250) {
            throw new UploadValidationException("Describe the featured image in 250 characters or fewer.");
        }
    }

    private String safeFilename(String filename) {
        var value = filename == null || filename.isBlank() ? "service-image" : filename.trim();
        return value.length() > 255 ? value.substring(value.length() - 255) : value;
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
}
