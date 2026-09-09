package org.greenstone.backend.admin.enquiry;

import jakarta.persistence.criteria.Predicate;
import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryActivity;
import org.greenstone.backend.persistence.entity.EnquiryActivityType;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.EnquiryPriority;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.EnquiryActivityRepository;
import org.greenstone.backend.persistence.repository.EnquiryAttachmentRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.notification.EnquiryAssignedEvent;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.WorkflowConflictException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminEnquiryService {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Pacific/Auckland");

    private final EnquiryRepository enquiryRepository;
    private final EnquiryAttachmentRepository attachmentRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final FileStorageService fileStorageService;
    private final EnquiryActivityRepository activityRepository;
    private final AdminUserRepository adminUserRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminEnquiryService(
            EnquiryRepository enquiryRepository,
            EnquiryAttachmentRepository attachmentRepository,
            ServiceOfferingRepository serviceOfferingRepository,
            FileStorageService fileStorageService,
            EnquiryActivityRepository activityRepository,
            AdminUserRepository adminUserRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.enquiryRepository = enquiryRepository;
        this.attachmentRepository = attachmentRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.fileStorageService = fileStorageService;
        this.activityRepository = activityRepository;
        this.adminUserRepository = adminUserRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public AdminEnquiryPageResponse search(
            String query,
            EnquiryStatus status,
            String serviceSlug,
            String assignment,
            EnquiryPriority priority,
            EnquiryFollowUpFilter followUp,
            LocalDate from,
            LocalDate to,
            int requestedPage,
            int requestedSize,
            String actorEmail
    ) {
        var actor = adminUserRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
        var assignedAdminId = resolveAssignment(assignment, actor.getId());
        var pageNumber = Math.max(0, requestedPage);
        var pageSize = Math.min(50, Math.max(1, requestedSize));
        var pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = enquiryRepository.findAll(
                specification(query, status, serviceSlug, assignedAdminId, assignment, priority, followUp, from, to),
                pageRequest
        );

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
        var staff = adminUserRepository.findAllByOrderByDisplayNameAsc().stream()
                .map(user -> new AdminStaffOptionResponse(
                        user.getId(), user.getDisplayName(), user.getEmail(), user.isEnabled()
                ))
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
                services,
                staff,
                metrics(actor.getId())
        );
    }

    @Transactional(readOnly = true)
    public AdminEnquiryDetailResponse find(UUID enquiryId) {
        var enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry was not found."));
        return toDetail(enquiry);
    }

    @Transactional
    public AdminEnquiryDetailResponse updateWorkflow(
            UUID enquiryId,
            UpdateEnquiryWorkflowRequest request,
            String actorEmail
    ) {
        var enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry was not found."));
        if (enquiry.getVersion() != request.version()) {
            throw new WorkflowConflictException();
        }

        var actor = adminUserRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account is no longer available."));
        var previousStatus = enquiry.getStatus();
        var previousAssignee = enquiry.getAssignedTo();
        var updatedAssignee = request.assignedAdminId() == null ? null : adminUserRepository
                .findById(request.assignedAdminId())
                .filter(AdminUser::isEnabled)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an active staff member."));
        var previousPriority = enquiry.getPriority();
        var previousFollowUp = enquiry.getFollowUpAt();
        var newNote = normalizeNote(request.newNote());
        var statusChanged = previousStatus != request.status();
        var assignmentChanged = !Objects.equals(
                previousAssignee == null ? null : previousAssignee.getId(),
                updatedAssignee == null ? null : updatedAssignee.getId()
        );
        var priorityChanged = previousPriority != request.priority();
        var followUpChanged = !Objects.equals(previousFollowUp, request.followUpAt());

        if (!statusChanged && !assignmentChanged && !priorityChanged && !followUpChanged && newNote == null) {
            return toDetail(enquiry);
        }

        enquiry.setStatus(request.status());
        enquiry.setAssignedTo(updatedAssignee);
        enquiry.setPriority(request.priority());
        enquiry.setFollowUpAt(request.followUpAt());
        try {
            enquiryRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException exception) {
            throw new WorkflowConflictException();
        }

        if (statusChanged) {
            activityRepository.save(new EnquiryActivity(
                    enquiry,
                    actor,
                    EnquiryActivityType.STATUS_CHANGED,
                    previousStatus,
                    request.status(),
                    "Status changed from " + statusLabel(previousStatus) + " to " + statusLabel(request.status()) + "."
            ));
        }
        if (assignmentChanged) {
            activityRepository.save(new EnquiryActivity(
                    enquiry,
                    actor,
                    EnquiryActivityType.ASSIGNMENT_CHANGED,
                    null,
                    null,
                    updatedAssignee == null
                            ? "Assignment removed."
                            : "Assigned to " + updatedAssignee.getDisplayName() + "."
            ));
        }
        if (priorityChanged) {
            activityRepository.save(new EnquiryActivity(
                    enquiry,
                    actor,
                    EnquiryActivityType.PRIORITY_CHANGED,
                    null,
                    null,
                    "Priority changed from " + priorityLabel(previousPriority) + " to " + priorityLabel(request.priority()) + "."
            ));
        }
        if (followUpChanged) {
            activityRepository.save(new EnquiryActivity(
                    enquiry,
                    actor,
                    EnquiryActivityType.FOLLOW_UP_CHANGED,
                    null,
                    null,
                    request.followUpAt() == null ? "Follow-up reminder removed." : "Follow-up reminder scheduled."
            ));
        }
        if (newNote != null) {
            activityRepository.save(new EnquiryActivity(
                    enquiry,
                    actor,
                    EnquiryActivityType.NOTE_ADDED,
                    null,
                    null,
                    "Private note added.",
                    newNote
            ));
        }
        activityRepository.flush();
        if (assignmentChanged && updatedAssignee != null) {
            eventPublisher.publishEvent(new EnquiryAssignedEvent(
                    enquiry.getId(), updatedAssignee.getId(), enquiry.getVersion()
            ));
        }
        return toDetail(enquiry);
    }

    private AdminEnquiryDetailResponse toDetail(Enquiry enquiry) {
        var enquiryId = enquiry.getId();
        var attachments = attachmentRepository.findAllByEnquiryId(enquiryId).stream()
                .map(attachment -> new AdminEnquiryAttachmentResponse(
                        attachment.getId(),
                        attachment.getOriginalFilename(),
                        attachment.getContentType(),
                        attachment.getSizeBytes(),
                        attachment.getCreatedAt()
                ))
                .toList();
        var activities = activityRepository.findAllByEnquiryIdOrderByCreatedAtDesc(enquiryId).stream()
                .map(activity -> new AdminEnquiryActivityResponse(
                        activity.getId(),
                        activity.getActivityType(),
                        activity.getPreviousStatus(),
                        activity.getNewStatus(),
                        activity.getSummary(),
                        activity.getNoteBody(),
                        isNotificationActivity(activity.getActivityType())
                                ? "Notification service"
                                : activity.getActor().getDisplayName(),
                        activity.getCreatedAt()
                ))
                .toList();
        var service = enquiry.getService();

        return new AdminEnquiryDetailResponse(
                enquiry.getId(),
                reference(enquiry.getId()),
                enquiry.getVersion(),
                enquiry.getType(),
                enquiry.getStatus(),
                enquiry.getAssignedTo() == null ? null : enquiry.getAssignedTo().getId(),
                enquiry.getAssignedTo() == null ? null : enquiry.getAssignedTo().getDisplayName(),
                enquiry.getPriority(),
                enquiry.getFollowUpAt(),
                isOverdue(enquiry),
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
                enquiry.getUpdatedAt(),
                enquiry.getCompletedAt(),
                enquiry.getNotificationSentAt(),
                attachments,
                activities
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
            UUID assignedAdminId,
            String assignment,
            EnquiryPriority priority,
            EnquiryFollowUpFilter followUp,
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
            if ("unassigned".equalsIgnoreCase(assignment)) {
                predicates.add(builder.isNull(root.get("assignedTo")));
            } else if (assignedAdminId != null) {
                predicates.add(builder.equal(root.get("assignedTo").get("id"), assignedAdminId));
            }
            if (priority != null) {
                predicates.add(builder.equal(root.get("priority"), priority));
            }
            if (followUp != null) {
                addFollowUpPredicates(predicates, root, builder, followUp);
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
                enquiry.getAssignedTo() == null ? null : enquiry.getAssignedTo().getId(),
                enquiry.getAssignedTo() == null ? null : enquiry.getAssignedTo().getDisplayName(),
                enquiry.getPriority(),
                enquiry.getFollowUpAt(),
                isOverdue(enquiry),
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

    private String normalizeNote(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        return notes.trim();
    }

    private UUID resolveAssignment(String assignment, UUID actorId) {
        if (assignment == null || assignment.isBlank() || "unassigned".equalsIgnoreCase(assignment)) {
            return null;
        }
        if ("mine".equalsIgnoreCase(assignment)) {
            return actorId;
        }
        try {
            return UUID.fromString(assignment);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The staff assignment filter is invalid.");
        }
    }

    private AdminEnquiryMetricsResponse metrics(UUID actorId) {
        var unassigned = enquiryRepository.count((root, query, builder) -> builder.and(
                activeStatus(root, builder), builder.isNull(root.get("assignedTo"))
        ));
        var dueToday = enquiryRepository.count((root, query, builder) -> {
            var bounds = todayBounds();
            return builder.and(activeStatus(root, builder),
                    builder.greaterThanOrEqualTo(root.get("followUpAt"), bounds[0]),
                    builder.lessThan(root.get("followUpAt"), bounds[1]));
        });
        var overdue = enquiryRepository.count((root, query, builder) -> builder.and(
                activeStatus(root, builder),
                builder.lessThan(root.get("followUpAt"), OffsetDateTime.now(BUSINESS_TIME_ZONE))
        ));
        var mine = enquiryRepository.count((root, query, builder) -> builder.and(
                activeStatus(root, builder), builder.equal(root.get("assignedTo").get("id"), actorId)
        ));
        return new AdminEnquiryMetricsResponse(unassigned, dueToday, overdue, mine);
    }

    private void addFollowUpPredicates(
            ArrayList<Predicate> predicates,
            jakarta.persistence.criteria.Root<Enquiry> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            EnquiryFollowUpFilter followUp
    ) {
        predicates.add(activeStatus(root, builder));
        var bounds = todayBounds();
        switch (followUp) {
            case OVERDUE -> predicates.add(builder.lessThan(root.get("followUpAt"), OffsetDateTime.now(BUSINESS_TIME_ZONE)));
            case TODAY -> {
                predicates.add(builder.greaterThanOrEqualTo(root.get("followUpAt"), bounds[0]));
                predicates.add(builder.lessThan(root.get("followUpAt"), bounds[1]));
            }
            case UPCOMING -> predicates.add(builder.greaterThanOrEqualTo(root.get("followUpAt"), bounds[1]));
        }
    }

    private Predicate activeStatus(
            jakarta.persistence.criteria.Root<Enquiry> root,
            jakarta.persistence.criteria.CriteriaBuilder builder
    ) {
        return root.get("status").in(
                EnquiryStatus.NEW, EnquiryStatus.IN_REVIEW, EnquiryStatus.CONTACTED, EnquiryStatus.QUOTED
        );
    }

    private OffsetDateTime[] todayBounds() {
        var today = LocalDate.now(BUSINESS_TIME_ZONE);
        return new OffsetDateTime[]{
                OffsetDateTime.ofInstant(today.atStartOfDay(BUSINESS_TIME_ZONE).toInstant(), BUSINESS_TIME_ZONE),
                OffsetDateTime.ofInstant(today.plusDays(1).atStartOfDay(BUSINESS_TIME_ZONE).toInstant(), BUSINESS_TIME_ZONE)
        };
    }

    private boolean isOverdue(Enquiry enquiry) {
        return enquiry.getFollowUpAt() != null
                && isActive(enquiry.getStatus())
                && enquiry.getFollowUpAt().isBefore(OffsetDateTime.now(BUSINESS_TIME_ZONE));
    }

    private boolean isActive(EnquiryStatus status) {
        return status == EnquiryStatus.NEW || status == EnquiryStatus.IN_REVIEW
                || status == EnquiryStatus.CONTACTED || status == EnquiryStatus.QUOTED;
    }

    private String priorityLabel(EnquiryPriority priority) {
        return priority.name().substring(0, 1) + priority.name().substring(1).toLowerCase(Locale.ROOT);
    }

    private boolean isNotificationActivity(EnquiryActivityType type) {
        return type == EnquiryActivityType.NOTIFICATION_SENT
                || type == EnquiryActivityType.NOTIFICATION_FAILED;
    }

    private String statusLabel(EnquiryStatus status) {
        var words = status.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }
}
