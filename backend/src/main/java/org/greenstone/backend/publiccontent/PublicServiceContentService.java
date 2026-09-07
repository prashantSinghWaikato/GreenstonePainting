package org.greenstone.backend.publiccontent;

import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.entity.ServiceOffering;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class PublicServiceContentService {

    private final ServiceOfferingRepository serviceRepository;
    private final FileStorageService storageService;

    public PublicServiceContentService(ServiceOfferingRepository serviceRepository, FileStorageService storageService) {
        this.serviceRepository = serviceRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<PublicServiceResponse> services() {
        return serviceRepository.findAllByStatusOrderByDisplayOrderAsc(PublicationStatus.PUBLISHED).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public PublicServiceResponse service(String slug) {
        return response(findPublished(slug));
    }

    @Transactional(readOnly = true)
    public AttachmentDownload image(String slug) {
        var service = findPublished(slug);
        var key = service.getFeaturedImageObjectKey();
        if (key == null || key.startsWith("static:")) throw new ResourceNotFoundException("This image is served by the website.");
        return new AttachmentDownload(
                storageService.load(key), service.getFeaturedImageFilename(),
                service.getFeaturedImageContentType(), service.getFeaturedImageSizeBytes()
        );
    }

    private PublicServiceResponse response(ServiceOffering service) {
        return new PublicServiceResponse(
                service.getSlug(), service.getTitle(), service.getLabel(), service.getSummary(), service.getDescription(),
                Arrays.stream(service.getInclusions().split("\\R")).map(String::trim).filter(value -> !value.isBlank()).toList(),
                service.getNote(), service.getDisplayOrder(), imageUrl(service), service.getFeaturedImageAlt()
        );
    }

    private String imageUrl(ServiceOffering service) {
        var key = service.getFeaturedImageObjectKey();
        if (key == null) return null;
        return key.startsWith("static:") ? key.substring("static:".length()) : "/api/services/" + service.getSlug() + "/image";
    }

    private ServiceOffering findPublished(String slug) {
        return serviceRepository.findBySlugAndStatus(slug, PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Service was not found."));
    }
}
