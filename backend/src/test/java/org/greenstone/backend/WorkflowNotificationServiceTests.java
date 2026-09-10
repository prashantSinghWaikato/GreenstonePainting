package org.greenstone.backend;

import org.greenstone.backend.notification.EnquiryAssignedEvent;
import org.greenstone.backend.notification.EnquiryNotifier;
import org.greenstone.backend.notification.WorkflowNotificationService;
import org.greenstone.backend.notification.JobChangedEvent;
import org.greenstone.backend.notification.JobNotifier;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryActivityType;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.entity.NotificationDeliveryStatus;
import org.greenstone.backend.persistence.entity.NotificationType;
import org.greenstone.backend.persistence.entity.Quote;
import org.greenstone.backend.persistence.entity.QuoteStatus;
import org.greenstone.backend.persistence.entity.PaintingJob;
import org.greenstone.backend.persistence.entity.JobActivityType;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.EnquiryActivityRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.NotificationDeliveryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.persistence.repository.QuoteRepository;
import org.greenstone.backend.persistence.repository.PaintingJobRepository;
import org.greenstone.backend.persistence.repository.JobActivityRepository;
import org.greenstone.backend.web.NotificationDeliveryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorkflowNotificationServiceTests {

    @Autowired
    private WorkflowNotificationService service;
    @Autowired
    private AdminUserRepository userRepository;
    @Autowired
    private EnquiryRepository enquiryRepository;
    @Autowired
    private ServiceOfferingRepository serviceRepository;
    @Autowired
    private NotificationDeliveryRepository deliveryRepository;
    @Autowired
    private EnquiryActivityRepository activityRepository;
    @MockitoBean
    private EnquiryNotifier notifier;
    @MockitoBean
    private JobNotifier jobNotifier;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private PaintingJobRepository jobRepository;
    @Autowired private JobActivityRepository jobActivityRepository;

    private AdminUser recipient;
    private Enquiry enquiry;

    @BeforeEach
    void setUp() {
        recipient = userRepository.save(new AdminUser(
                "painter@greenstonepainting.co.nz", "test-password-hash", "Assigned Painter"
        ));
        enquiry = new Enquiry(
                EnquiryType.QUOTE_REQUEST, "Aroha", "Williams", "aroha@example.com", "Interior repaint"
        );
        enquiry.setService(serviceRepository.findBySlug("interior-painting").orElseThrow());
        enquiry.setAssignedTo(recipient);
        enquiry.setFollowUpAt(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        enquiry = enquiryRepository.saveAndFlush(enquiry);
    }

    @Test
    void sendsAnAssignmentOnlyOnceAndRecordsTheDelivery() {
        var event = new EnquiryAssignedEvent(enquiry.getId(), recipient.getId(), enquiry.getVersion());

        service.deliverAssignment(event);
        service.deliverAssignment(event);

        verify(notifier, times(1)).sendAssignmentNotification(enquiry, recipient);
        assertThat(deliveryRepository.findAll()).singleElement()
                .extracting(delivery -> delivery.getStatus())
                .isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(activityRepository.findAllByEnquiryIdOrderByCreatedAtDesc(enquiry.getId()))
                .extracting(activity -> activity.getActivityType())
                .containsExactly(EnquiryActivityType.NOTIFICATION_SENT);
    }

    @Test
    void recordsFailuresWithoutThrowingAwayTheWorkflowChange() {
        doThrow(new NotificationDeliveryException("SMTP unavailable", new RuntimeException("offline")))
                .when(notifier).sendAssignmentNotification(any(), any());

        service.deliverAssignment(new EnquiryAssignedEvent(enquiry.getId(), recipient.getId(), enquiry.getVersion()));

        assertThat(deliveryRepository.findAll()).singleElement()
                .satisfies(delivery -> {
                    assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
                    assertThat(delivery.getAttemptCount()).isEqualTo(1);
                    assertThat(delivery.getErrorMessage()).isEqualTo("SMTP unavailable");
                });
        assertThat(activityRepository.findAllByEnquiryIdOrderByCreatedAtDesc(enquiry.getId()))
                .extracting(activity -> activity.getActivityType())
                .containsExactly(EnquiryActivityType.NOTIFICATION_FAILED);
    }

    @Test
    void sendsDueFollowUpsOnlyOnce() {
        service.deliverDueFollowUps();
        service.deliverDueFollowUps();

        verify(notifier, times(1)).sendFollowUpNotification(enquiry, recipient, true);
        assertThat(deliveryRepository.findAll()).hasSize(1);
    }

    @Test
    void failedFollowUpsWaitForTheRetryBackoffInsteadOfRetryingEverySchedulerTick() {
        doThrow(new NotificationDeliveryException("SMTP unavailable", new RuntimeException("offline")))
                .when(notifier).sendFollowUpNotification(any(), any(), anyBoolean());

        service.deliverDueFollowUps();
        service.deliverDueFollowUps();

        verify(notifier, times(1)).sendFollowUpNotification(enquiry, recipient, true);
        assertThat(deliveryRepository.findAll()).singleElement()
                .satisfies(delivery -> {
                    assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
                    assertThat(delivery.getAttemptCount()).isEqualTo(1);
                });
    }

    @Test
    void sendsJobUpdatesOnlyOnceAndRecordsThemOnTheJobTimeline() {
        var quote = new Quote(enquiry, recipient, "GST-" + UUID.randomUUID().toString().substring(0, 8), 1);
        quote.setStatus(QuoteStatus.ACCEPTED); quote.setCustomerName("Aroha Williams"); quote.setCustomerEmail("aroha@example.com");
        quote.setTitle("Interior repaint"); quote.setScope("Prepare and paint walls."); quote.setTerms("Standard terms.");
        quote.setSubtotal(new BigDecimal("1000")); quote.setGstAmount(new BigDecimal("150")); quote.setTotal(new BigDecimal("1150")); quote.setValidUntil(LocalDate.now().plusDays(30));
        quoteRepository.saveAndFlush(quote);
        var job = new PaintingJob(quote, recipient, "JOB-TEST-" + UUID.randomUUID().toString().substring(0, 6));
        job.setCustomerName("Aroha Williams"); job.setCustomerEmail("aroha@example.com"); job.setTitle("Interior repaint"); job.setScope("Prepare and paint walls."); job.setAssignedTo(recipient);
        job = jobRepository.saveAndFlush(job);
        var jobId = job.getId();
        var event = new JobChangedEvent(job.getId(), recipient.getId(), NotificationType.JOB_ASSIGNMENT, job.getId() + ":assigned");

        service.deliverJobEvent(event);
        service.deliverJobEvent(event);

        verify(jobNotifier, times(1)).send(job, recipient, NotificationType.JOB_ASSIGNMENT);
        assertThat(deliveryRepository.findAll()).singleElement().satisfies(delivery -> {
            assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
            assertThat(delivery.getJob().getId()).isEqualTo(jobId);
        });
        assertThat(jobActivityRepository.findAllByJobIdOrderByCreatedAtDesc(job.getId()))
                .extracting(activity -> activity.getType()).containsExactly(JobActivityType.NOTIFICATION_SENT);
    }
}
