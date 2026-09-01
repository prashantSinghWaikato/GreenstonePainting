package org.greenstone.backend.admin.enquiry;

import jakarta.persistence.criteria.Predicate;
import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.repository.EnquiryAttachmentRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminEnquiryService {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Pacific/Auckland");

    private final EnquiryRepository enquiryRepository;
    private final EnquiryAttachmentRepository attachmentRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final FileStorageService fileStorageService;

    public AdminEnquiryService(
            EnquiryRepository enquiryRepository,
            EnquiryAttachmentRepository attachmentRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            FileStorageService fileStorageService
    ) {
        this.enquiryRepository = enquiryRepository;
        this.attachmentRepository = attachmentRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public AdminEnquiryPageResponse search(
            String query,
            EnquiryStatus status,
            String serviceSlug,
            LocalDate from,
            LocalDate to,
            int requestedPage,
            int requestedSize
    ) {
        var pageNumber = Math.max(0, requestedPage);
        var pageSize = Math.min(50, Math.max(1, requestedSize));
        var pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = enquiryRepository.findAll(specification(query, status, serviceSlug, from, to), pageRequest);

        var ids = result.getContent().stream().map(Enquiry::getId).toList();
        var attachmentCounts = new HashMap<UUID, Long>();
        if (!ids.isEmpty()) {
            attachmentRepository.countAllByEnquiryIds(ids).forEach(count ->
                    attachmentCounts.put(count.getEnquiryId(), count.getTotal()));
        }

        var statusCounts = new EnumMap<EnquiryStatus, Long>(EnquiryStatus.class);
        for (var enquiryStatus : EnquiryStatus.values()) {
            statusCounts.put(enquiryStatus, 0L);
        }
        enquiryRepository.countAllByStatus().forEach(count -> statusCounts.put(count.getStatus(), count.getTotal()));

        var services = serviceOfferingRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(service -> new AdminServiceFilterResponse(service.getSlug(), service.getTitle()))
                .toList();
        var items = result.getContent().stream()
                .map(enquiry -> toSummary(enquiry, attachmentCounts.getOrDefault(enquiry.getId(), 0L)))
                .toList();

        return new AdminEnquiryPageResponse(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                statusCounts,
                services
        );
    }

    @Transactional(readOnly = true)
    public AdminEnquiryDetailResponse find(UUID enquiryId) {
        var enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry was not found."));
        var attachments = attachmentRepository.findAllByEnquiryId(enquiryId).stream()
                .map(attachment -> new AdminEnquiryAttachmentResponse(
                        attachment.getId(),
                        attachment.getOriginalFilename(),
                        attachment.getContentType(),
                        attachment.getSizeBytes(),
                        attachment.getCreatedAt()
                ))
                .toList();
        var service = enquiry.getService();

        return new AdminEnquiryDetailResponse(
                enquiry.getId(),
                reference(enquiry.getId()),
                enquiry.getType(),
                enquiry.getStatus(),
                enquiry.getFirstName(),
                enquiry.getLastName(),
                enquiry.getEmail(),
                enquiry.getPhone(),
                enquiry.getContactPreference(),
                service == null ? null : service.getSlug(),
                service == null ? null : service.getTitle(),
                enquiry.getPropertyAddress(),
                enquiry.getSuburb(),
                enquiry.getMessage(),
                enquiry.getEstimatedBudget(),
                enquiry.getDesiredStartDate(),
                enquiry.getInternalNotes(),
                enquiry.getCreatedAt(),
                enquiry.getCompletedAt(),
                enquiry.getNotificationSentAt(),
                attachments
        );
    }

    @Transactional(readOnly = true)
    public AttachmentDownload downloadAttachment(UUID enquiryId, UUID attachmentId) {
        var attachment = attachmentRepository.findByIdAndEnquiryId(attachmentId, enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Project photo was not found."));
        return new AttachmentDownload(
                fileStorageService.load(attachment.getObjectKey()),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes()
        );
    }

    private Specification<Enquiry> specification(
            String query,
            EnquiryStatus status,
            String serviceSlug,
            LocalDate from,
            LocalDate to
    ) {
        return (root, criteriaQuery, builder) -> {
            var predicates = new ArrayList<Predicate>();
            var trimmedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            if (!trimmedQuery.isBlank()) {
                var pattern = "%" + escapeLike(trimmedQuery) + "%";
                var service = root.join("service", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("firstName")), pattern, '\\'),
                        builder.like(builder.lower(root.get("lastName")), pattern, '\\'),
                        builder.like(builder.lower(root.get("email")), pattern, '\\'),
                        builder.like(builder.lower(builder.coalesce(root.<String>get("phone"), "")), pattern, '\\'),
                        builder.like(builder.lower(builder.coalesce(root.<String>get("propertyAddress"), "")), pattern, '\\'),
                        builder.like(builder.lower(builder.coalesce(service.<String>get("title"), "")), pattern, '\\'),
                        builder.like(builder.lower(root.get("id").as(String.class)), pattern, '\\')
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (serviceSlug != null && !serviceSlug.isBlank()) {
                predicates.add(builder.equal(root.join("service").get("slug"), serviceSlug.trim()));
            }
            if (from != null) {
                var fromTime = OffsetDateTime.ofInstant(from.atStartOfDay(BUSINESS_TIME_ZONE).toInstant(), BUSINESS_TIME_ZONE);
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), fromTime));
            }
            if (to != null) {
                var untilTime = OffsetDateTime.ofInstant(to.plusDays(1).atStartOfDay(BUSINESS_TIME_ZONE).toInstant(), BUSINESS_TIME_ZONE);
                predicates.add(builder.lessThan(root.get("createdAt"), untilTime));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private AdminEnquirySummaryResponse toSummary(Enquiry enquiry, long attachmentCount) {
        var service = enquiry.getService();
        return new AdminEnquirySummaryResponse(
                enquiry.getId(),
                reference(enquiry.getId()),
                enquiry.getFirstName(),
                enquiry.getLastName(),
                enquiry.getEmail(),
                enquiry.getPhone(),
                service == null ? null : service.getSlug(),
                service == null ? null : service.getTitle(),
                enquiry.getPropertyAddress(),
                enquiry.getStatus(),
                attachmentCount,
                enquiry.getCreatedAt(),
                enquiry.getCompletedAt()
        );
    }

    private String reference(UUID id) {
        return id.toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
