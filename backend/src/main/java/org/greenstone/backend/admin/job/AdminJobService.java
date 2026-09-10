package org.greenstone.backend.admin.job;

import org.greenstone.backend.admin.enquiry.AdminStaffOptionResponse;
import org.greenstone.backend.enquiry.AttachmentDownload;
import org.greenstone.backend.persistence.entity.*;
import org.greenstone.backend.persistence.repository.*;
import org.greenstone.backend.storage.FileStorageService;
import org.greenstone.backend.web.*;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.greenstone.backend.notification.JobChangedEvent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminJobService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Pacific/Auckland");
    private static final int MAX_PHOTOS = 30;
    private final PaintingJobRepository jobRepository;
    private final JobPhotoRepository photoRepository;
    private final JobActivityRepository activityRepository;
    private final QuoteRepository quoteRepository;
    private final AdminUserRepository userRepository;
    private final JobChecklistItemRepository checklistRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileStorageService storage;
    private final ApplicationEventPublisher events;

    public AdminJobService(PaintingJobRepository jobs, JobPhotoRepository photos, JobActivityRepository activities,
                           QuoteRepository quotes, AdminUserRepository users, JobChecklistItemRepository checklist,
                           InvoiceRepository invoices, FileStorageService storage, ApplicationEventPublisher events) {
        jobRepository = jobs; photoRepository = photos; activityRepository = activities;
        quoteRepository = quotes; userRepository = users; checklistRepository = checklist; invoiceRepository = invoices; this.storage = storage; this.events = events;
    }

    @Transactional(readOnly = true)
    public AdminJobPageResponse list(String query, JobStatus status, String assignment, String actorEmail) {
        var actor = actor(actorEmail);
        var normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        var items = jobRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(job -> status == null || job.getStatus() == status)
                .filter(job -> assignment == null || assignment.isBlank()
                        || assignment.equals("mine") && job.getAssignedTo() != null && job.getAssignedTo().getId().equals(actor.getId())
                        || assignment.equals("unassigned") && job.getAssignedTo() == null
                        || job.getAssignedTo() != null && job.getAssignedTo().getId().toString().equals(assignment))
                .filter(job -> normalized.isBlank() || searchable(job).contains(normalized))
                .sorted(Comparator.comparing(PaintingJob::getScheduledStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(PaintingJob::getCreatedAt, Comparator.reverseOrder()))
                .map(this::summary).toList();
        return new AdminJobPageResponse(items, staff(), metrics(jobRepository.findAll()));
    }

    @Transactional(readOnly = true)
    public AdminJobOverviewResponse overview() {
        var jobs = jobRepository.findAll();
        var today = LocalDate.now(BUSINESS_ZONE);
        var upcoming = jobs.stream()
                .filter(job -> job.getStatus() != JobStatus.COMPLETED && job.getStatus() != JobStatus.CANCELLED)
                .filter(job -> job.getScheduledStartDate() != null && !job.getScheduledStartDate().isBefore(today))
                .sorted(Comparator.comparing(PaintingJob::getScheduledStartDate)).limit(5).map(this::summary).toList();
        return new AdminJobOverviewResponse(metrics(jobs), upcoming);
    }

    @Transactional
    public AdminJobDetailResponse createFromQuote(UUID quoteId, String actorEmail) {
        var existing = jobRepository.findByQuoteId(quoteId);
        if (existing.isPresent()) return detail(existing.get());
        var quote = quoteRepository.findById(quoteId).orElseThrow(() -> new ResourceNotFoundException("Quote was not found."));
        if (quote.getStatus() != QuoteStatus.ACCEPTED) throw new WorkflowConflictException("Only an accepted quote can be converted into a job.");
        var actor = actor(actorEmail);
        var job = new PaintingJob(quote, actor, jobNumber());
        var enquiry = quote.getEnquiry();
        job.setCustomerName(quote.getCustomerName()); job.setCustomerEmail(quote.getCustomerEmail());
        job.setCustomerPhone(enquiry.getPhone()); job.setPropertyAddress(quote.getPropertyAddress());
        job.setTitle(quote.getTitle()); job.setScope(quote.getScope());
        job.setServiceTitle(enquiry.getService() == null ? null : enquiry.getService().getTitle());
        job.setAssignedTo(enquiry.getAssignedTo());
        job.setScheduledStartDate(quote.getEstimatedStartDate()); job.setScheduledEndDate(quote.getEstimatedEndDate());
        if (quote.getEstimatedStartDate() != null && quote.getEstimatedEndDate() != null) job.setStatus(JobStatus.SCHEDULED);
        jobRepository.saveAndFlush(job);
        var defaults = List.of("Site protection and access confirmed", "Surfaces prepared and defects addressed", "Specified coating system completed", "Final touch-ups and detail check completed", "Site cleaned and materials removed", "Customer walkthrough completed");
        for (int i = 0; i < defaults.size(); i++) checklistRepository.save(new JobChecklistItem(job, defaults.get(i), i));
        activityRepository.save(new JobActivity(job, actor, JobActivityType.CREATED,
                "Job created from accepted quote " + quote.getQuoteNumber() + ".", null));
        if (job.getAssignedTo() != null) {
            events.publishEvent(new JobChangedEvent(job.getId(), job.getAssignedTo().getId(), NotificationType.JOB_ASSIGNMENT, job.getId() + ":assignment:" + job.getVersion()));
            if (job.getScheduledStartDate() != null) events.publishEvent(new JobChangedEvent(job.getId(), job.getAssignedTo().getId(), NotificationType.JOB_SCHEDULE, job.getId() + ":schedule:" + job.getScheduledStartDate() + ":" + job.getScheduledEndDate()));
        }
        return detail(job);
    }

    @Transactional(readOnly = true)
    public AdminJobDetailResponse find(UUID id) { return detail(job(id)); }

    @Transactional
    public AdminJobDetailResponse update(UUID id, UpdateJobRequest request, String actorEmail) {
        var job = job(id);
        if (job.getVersion() != request.version()) throw new WorkflowConflictException("This job was updated by another staff member. Reload it before saving again.");
        if ((job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) && request.status() != job.getStatus())
            throw new WorkflowConflictException("Completed or cancelled jobs cannot be reopened.");
        validate(request);
        if (request.status() == JobStatus.COMPLETED && (!allChecklistComplete(job.getId()) || job.getCustomerSignoffAt() == null))
            throw new WorkflowConflictException("Complete the site checklist and record customer sign-off before completing this job.");
        var actor = actor(actorEmail);
        var oldStatus = job.getStatus();
        var oldAssignee = job.getAssignedTo();
        var scheduleChanged = !Objects.equals(job.getScheduledStartDate(), request.scheduledStartDate())
                || !Objects.equals(job.getScheduledEndDate(), request.scheduledEndDate());
        var detailsChanged = !Objects.equals(normalize(job.getSiteInstructions()), normalize(request.siteInstructions()))
                || !Objects.equals(normalize(job.getInternalNotes()), normalize(request.internalNotes()));
        var assignee = request.assignedAdminId() == null ? null : userRepository.findById(request.assignedAdminId())
                .filter(AdminUser::isEnabled).orElseThrow(() -> new JobValidationException("Choose an active staff member."));

        job.setStatus(request.status()); job.setAssignedTo(assignee); job.setScheduledStartDate(request.scheduledStartDate());
        job.setScheduledEndDate(request.scheduledEndDate()); job.setSiteInstructions(normalize(request.siteInstructions()));
        job.setInternalNotes(normalize(request.internalNotes())); job.setUpdatedBy(actor);
        var now = OffsetDateTime.now(ZoneOffset.UTC);
        if (oldStatus != JobStatus.IN_PROGRESS && request.status() == JobStatus.IN_PROGRESS && job.getActualStartedAt() == null) job.setActualStartedAt(now);
        if (oldStatus != JobStatus.COMPLETED && request.status() == JobStatus.COMPLETED) job.setCompletedAt(now);
        jobRepository.flush();
        if (oldStatus != request.status()) activity(job, actor, JobActivityType.STATUS_CHANGED, "Status changed from " + label(oldStatus) + " to " + label(request.status()) + ".", null);
        if (!sameUser(oldAssignee, assignee)) activity(job, actor, JobActivityType.ASSIGNMENT_CHANGED, assignee == null ? "Job unassigned." : "Assigned to " + assignee.getDisplayName() + ".", null);
        if (scheduleChanged) activity(job, actor, JobActivityType.SCHEDULE_CHANGED, "Job schedule updated.", null);
        if (detailsChanged) activity(job, actor, JobActivityType.DETAILS_UPDATED, "Site instructions or internal notes updated.", normalize(request.internalNotes()));
        if (assignee != null) {
            if (!sameUser(oldAssignee, assignee)) events.publishEvent(new JobChangedEvent(job.getId(), assignee.getId(), NotificationType.JOB_ASSIGNMENT, job.getId() + ":assignment:" + job.getVersion()));
            if (scheduleChanged) events.publishEvent(new JobChangedEvent(job.getId(), assignee.getId(), NotificationType.JOB_SCHEDULE, job.getId() + ":schedule:" + request.scheduledStartDate() + ":" + request.scheduledEndDate() + ":" + job.getVersion()));
            if (oldStatus != request.status()) events.publishEvent(new JobChangedEvent(job.getId(), assignee.getId(), NotificationType.JOB_STATUS, job.getId() + ":status:" + request.status() + ":" + job.getVersion()));
        }
        return detail(job);
    }

    @Transactional
    public AdminJobDetailResponse addPhoto(UUID id, JobPhotoPhase phase, MultipartFile file, String actorEmail) {
        var job = job(id); var actor = actor(actorEmail);
        if (photoRepository.countByJobId(id) >= MAX_PHOTOS) throw new JobValidationException("A job can include up to 30 photos.");
        var stored = storage.storeJobPhoto(id, file);
        try {
            photoRepository.saveAndFlush(new JobPhoto(job, phase, stored.objectKey(), safeName(file.getOriginalFilename()), stored.contentType(), stored.sizeBytes()));
            activity(job, actor, JobActivityType.PHOTO_ADDED, label(phase) + " photo added.", null);
            return detail(job);
        } catch (RuntimeException exception) { storage.delete(stored.objectKey()); throw exception; }
    }

    @Transactional(readOnly = true)
    public AttachmentDownload photo(UUID jobId, UUID photoId) {
        var photo = photoRepository.findByIdAndJobId(photoId, jobId).orElseThrow(() -> new ResourceNotFoundException("Job photo was not found."));
        return new AttachmentDownload(storage.load(photo.getObjectKey()), photo.getOriginalFilename(), photo.getContentType(), photo.getSizeBytes());
    }

    @Transactional
    public AdminJobDetailResponse updateChecklist(UUID jobId, UUID itemId, UpdateChecklistRequest request, String actorEmail) {
        var job = job(jobId); var actor = actor(actorEmail);
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) throw new WorkflowConflictException("The checklist is locked for completed or cancelled jobs.");
        var item = checklistRepository.findByIdAndJobId(itemId, jobId).orElseThrow(() -> new ResourceNotFoundException("Checklist item was not found."));
        item.setCompleted(request.completed(), actor, OffsetDateTime.now(ZoneOffset.UTC)); checklistRepository.flush();
        activity(job, actor, JobActivityType.CHECKLIST_UPDATED, (request.completed() ? "Completed: " : "Reopened: ") + item.getLabel(), null);
        return detail(job);
    }

    @Transactional
    public AdminJobDetailResponse recordSignoff(UUID jobId, RecordSignoffRequest request, String actorEmail) {
        var job = job(jobId); var actor = actor(actorEmail);
        if (!allChecklistComplete(jobId)) throw new WorkflowConflictException("Complete every checklist item before recording customer sign-off.");
        if (job.getStatus() == JobStatus.CANCELLED) throw new WorkflowConflictException("A cancelled job cannot be signed off.");
        job.setCustomerSignoffName(request.customerName().trim()); job.setCustomerSignoffAt(OffsetDateTime.now(ZoneOffset.UTC)); job.setUpdatedBy(actor); jobRepository.flush();
        activity(job, actor, JobActivityType.SIGNOFF_RECORDED, "Customer sign-off recorded for " + job.getCustomerSignoffName() + ".", null);
        return detail(job);
    }

    private void validate(UpdateJobRequest request) {
        if (request.scheduledStartDate() != null && request.scheduledEndDate() != null && request.scheduledEndDate().isBefore(request.scheduledStartDate()))
            throw new JobValidationException("Scheduled completion cannot be before the start date.");
        if (request.status() == JobStatus.SCHEDULED && (request.scheduledStartDate() == null || request.scheduledEndDate() == null))
            throw new JobValidationException("Scheduled jobs need both a start and completion date.");
    }
    private AdminJobMetricsResponse metrics(List<PaintingJob> jobs) { return new AdminJobMetricsResponse(count(jobs, JobStatus.PLANNED), count(jobs, JobStatus.SCHEDULED), count(jobs, JobStatus.IN_PROGRESS), count(jobs, JobStatus.COMPLETED)); }
    private long count(List<PaintingJob> jobs, JobStatus status) { return jobs.stream().filter(job -> job.getStatus() == status).count(); }
    private List<AdminStaffOptionResponse> staff() { return userRepository.findAllByOrderByDisplayNameAsc().stream().map(u -> new AdminStaffOptionResponse(u.getId(), u.getDisplayName(), u.getEmail(), u.isEnabled())).toList(); }
    private String searchable(PaintingJob job) { return String.join(" ", job.getJobNumber(), job.getCustomerName(), Objects.toString(job.getPropertyAddress(), ""), job.getTitle()).toLowerCase(Locale.ROOT); }
    private PaintingJob job(UUID id) { return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job was not found.")); }
    private AdminUser actor(String email) { return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("Staff account is unavailable.")); }
    private boolean sameUser(AdminUser a, AdminUser b) { return Objects.equals(a == null ? null : a.getId(), b == null ? null : b.getId()); }
    private boolean allChecklistComplete(UUID jobId) { var items = checklistRepository.findAllByJobIdOrderByPositionAsc(jobId); return !items.isEmpty() && items.stream().allMatch(JobChecklistItem::isCompleted); }
    private void activity(PaintingJob job, AdminUser actor, JobActivityType type, String summary, String note) { activityRepository.save(new JobActivity(job, actor, type, summary, note)); }
    private String label(Enum<?> value) { return value.name().toLowerCase(Locale.ROOT).replace('_', ' '); }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String jobNumber() { return "JOB-" + LocalDate.now(BUSINESS_ZONE).getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT); }
    private String safeName(String value) { var name = value == null || value.isBlank() ? "job-photo" : value.replaceAll("[\\x00-\\x1f\\x7f]", "_"); name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1); return name.length() <= 255 ? name : name.substring(name.length() - 255); }

    private AdminJobSummaryResponse summary(PaintingJob job) { return new AdminJobSummaryResponse(job.getId(), job.getJobNumber(), job.getStatus(), job.getCustomerName(), job.getTitle(), job.getServiceTitle(), job.getPropertyAddress(), job.getAssignedTo() == null ? null : job.getAssignedTo().getId(), job.getAssignedTo() == null ? null : job.getAssignedTo().getDisplayName(), job.getScheduledStartDate(), job.getScheduledEndDate(), photoRepository.countByJobId(job.getId()), job.getQuote().getId(), job.getQuote().getQuoteNumber(), job.getQuote().getTotal(), job.getUpdatedAt()); }
    private AdminJobDetailResponse detail(PaintingJob job) {
        var photos = photoRepository.findAllByJobIdOrderByCreatedAtDesc(job.getId()).stream().map(p -> new AdminJobPhotoResponse(p.getId(), p.getPhase(), p.getOriginalFilename(), p.getContentType(), p.getSizeBytes(), p.getCreatedAt())).toList();
        var checklist = checklistRepository.findAllByJobIdOrderByPositionAsc(job.getId()).stream().map(i -> new AdminJobChecklistResponse(i.getId(), i.getLabel(), i.getPosition(), i.isCompleted(), i.getCompletedAt(), i.getCompletedBy() == null ? null : i.getCompletedBy().getDisplayName())).toList();
        var invoice = invoiceRepository.findByJobId(job.getId()).map(i -> new AdminJobInvoiceSummary(i.getId(), i.getInvoiceNumber(), i.getStatus(), i.getTotal(), i.getAmountPaid(), i.getTotal().subtract(i.getAmountPaid()))).orElse(null);
        var activities = activityRepository.findAllByJobIdOrderByCreatedAtDesc(job.getId()).stream().map(a -> new AdminJobActivityResponse(a.getId(), a.getType(), a.getSummary(), a.getNoteBody(), a.getActor() == null ? "System" : a.getActor().getDisplayName(), a.getCreatedAt())).toList();
        return new AdminJobDetailResponse(job.getId(), job.getQuote().getId(), job.getEnquiry().getId(), job.getJobNumber(), job.getVersion(), job.getStatus(), job.getCustomerName(), job.getCustomerEmail(), job.getCustomerPhone(), job.getPropertyAddress(), job.getTitle(), job.getServiceTitle(), job.getScope(), job.getSiteInstructions(), job.getInternalNotes(), job.getAssignedTo() == null ? null : job.getAssignedTo().getId(), job.getAssignedTo() == null ? null : job.getAssignedTo().getDisplayName(), job.getScheduledStartDate(), job.getScheduledEndDate(), job.getActualStartedAt(), job.getCompletedAt(), job.getCustomerSignoffName(), job.getCustomerSignoffAt(), job.getQuote().getQuoteNumber(), job.getQuote().getTotal(), invoice, job.getCreatedAt(), job.getUpdatedAt(), photos, checklist, activities);
    }
}
