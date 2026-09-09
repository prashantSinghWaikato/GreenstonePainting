package org.greenstone.backend.notification;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryActivity;
import org.greenstone.backend.persistence.entity.EnquiryActivityType;
import org.greenstone.backend.persistence.entity.NotificationDelivery;
import org.greenstone.backend.persistence.entity.NotificationDeliveryStatus;
import org.greenstone.backend.persistence.entity.NotificationType;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.EnquiryActivityRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.NotificationDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class WorkflowNotificationService {

    private static final int MAXIMUM_ATTEMPTS = 3;
    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Pacific/Auckland");

    private final EnquiryRepository enquiryRepository;
    private final AdminUserRepository userRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final EnquiryActivityRepository activityRepository;
    private final EnquiryNotifier notifier;

    public WorkflowNotificationService(
            EnquiryRepository enquiryRepository,
            AdminUserRepository userRepository,
            NotificationDeliveryRepository deliveryRepository,
            EnquiryActivityRepository activityRepository,
            EnquiryNotifier notifier
    ) {
        this.enquiryRepository = enquiryRepository;
        this.userRepository = userRepository;
        this.deliveryRepository = deliveryRepository;
        this.activityRepository = activityRepository;
        this.notifier = notifier;
    }

    @Transactional
    public void deliverAssignment(EnquiryAssignedEvent event) {
        var enquiry = enquiryRepository.findById(event.enquiryId()).orElse(null);
        var recipient = userRepository.findById(event.recipientId()).orElse(null);
        if (enquiry == null || recipient == null || !recipient.isEnabled()
                || !recipient.isAssignmentNotificationsEnabled()) return;

        var key = enquiry.getId() + ":" + recipient.getId() + ":" + event.enquiryVersion();
        attempt(enquiry, recipient, NotificationType.ASSIGNMENT, key,
                () -> notifier.sendAssignmentNotification(enquiry, recipient), "Assignment");
    }

    @Transactional
    public void deliverDueFollowUps() {
        var now = OffsetDateTime.now(BUSINESS_TIME_ZONE);
        for (var enquiry : enquiryRepository.findAllDueForNotification(now)) {
            deliverFollowUp(enquiry, enquiry.getAssignedTo(), now);
        }
    }

    @Transactional
    public void deliverDailyDigests() {
        var now = OffsetDateTime.now(BUSINESS_TIME_ZONE);
        var key = LocalDate.now(BUSINESS_TIME_ZONE).toString();
        var unassignedCount = enquiryRepository.countActiveUnassigned();
        for (var recipient : userRepository.findAllByEnabledTrueAndDailyDigestEnabledTrueOrderByDisplayNameAsc()) {
            var overdue = enquiryRepository.findAllOverdueForAdmin(recipient.getId(), now);
            attempt(null, recipient, NotificationType.DAILY_DIGEST, key,
                    () -> notifier.sendDailyDigest(recipient, unassignedCount, overdue), "Daily digest");
        }
    }

    @Transactional
    public void retryFailures() {
        var retryBefore = OffsetDateTime.now(BUSINESS_TIME_ZONE).minusMinutes(10);
        var failures = deliveryRepository.findAllByStatusAndAttemptCountLessThanAndLastAttemptAtBefore(
                NotificationDeliveryStatus.FAILED, MAXIMUM_ATTEMPTS, retryBefore
        );
        for (var delivery : failures) {
            retry(delivery);
        }
    }

    private void deliverFollowUp(Enquiry enquiry, AdminUser recipient, OffsetDateTime now) {
        if (recipient == null || !recipient.isEnabled() || !recipient.isFollowUpNotificationsEnabled()
                || enquiry.getFollowUpAt() == null) return;
        var key = enquiry.getId() + ":" + enquiry.getFollowUpAt().toInstant();
        attempt(enquiry, recipient, NotificationType.FOLLOW_UP, key,
                () -> notifier.sendFollowUpNotification(enquiry, recipient, enquiry.getFollowUpAt().isBefore(now)),
                "Follow-up");
    }

    private void retry(NotificationDelivery delivery) {
        var enquiry = delivery.getEnquiry();
        var recipient = delivery.getRecipient();
        if (!recipient.isEnabled()) return;
        switch (delivery.getType()) {
            case ASSIGNMENT -> {
                if (enquiry != null && recipient.isAssignmentNotificationsEnabled()
                        && enquiry.getAssignedTo() != null
                        && enquiry.getAssignedTo().getId().equals(recipient.getId())) {
                    attemptExisting(delivery,
                            () -> notifier.sendAssignmentNotification(enquiry, recipient), "Assignment");
                }
            }
            case FOLLOW_UP -> {
                if (enquiry != null && enquiry.getFollowUpAt() != null && recipient.isFollowUpNotificationsEnabled()
                        && isActive(enquiry)) {
                    attemptExisting(delivery, () -> notifier.sendFollowUpNotification(
                            enquiry, recipient, enquiry.getFollowUpAt().isBefore(OffsetDateTime.now(BUSINESS_TIME_ZONE))
                    ), "Follow-up");
                }
            }
            case DAILY_DIGEST -> {
                if (recipient.isDailyDigestEnabled()) {
                    var now = OffsetDateTime.now(BUSINESS_TIME_ZONE);
                    attemptExisting(delivery, () -> notifier.sendDailyDigest(
                            recipient,
                            enquiryRepository.countActiveUnassigned(),
                            enquiryRepository.findAllOverdueForAdmin(recipient.getId(), now)
                    ), "Daily digest");
                }
            }
        }
    }

    private void attempt(
            Enquiry enquiry,
            AdminUser recipient,
            NotificationType type,
            String key,
            DeliveryAction action,
            String label
    ) {
        var existing = deliveryRepository.findByRecipientIdAndTypeAndDeduplicationKey(recipient.getId(), type, key);
        if (existing.isPresent() && existing.get().getStatus() == NotificationDeliveryStatus.FAILED) {
            return;
        }
        var delivery = existing.orElseGet(
                () -> deliveryRepository.save(new NotificationDelivery(enquiry, recipient, type, key))
        );
        attemptExisting(delivery, action, label);
    }

    private void attemptExisting(NotificationDelivery delivery, DeliveryAction action, String label) {
        if (delivery.getStatus() == NotificationDeliveryStatus.SENT
                || delivery.getAttemptCount() >= MAXIMUM_ATTEMPTS) return;
        var now = OffsetDateTime.now(BUSINESS_TIME_ZONE);
        try {
            action.send();
            delivery.recordSent(now);
            recordActivity(delivery, EnquiryActivityType.NOTIFICATION_SENT,
                    label + " email sent to " + delivery.getRecipient().getDisplayName() + ".");
        } catch (RuntimeException exception) {
            delivery.recordFailure(now, safeMessage(exception));
            recordActivity(delivery, EnquiryActivityType.NOTIFICATION_FAILED,
                    label + " email could not be sent to " + delivery.getRecipient().getDisplayName()
                            + " (attempt " + delivery.getAttemptCount() + " of " + MAXIMUM_ATTEMPTS + ").");
        }
        deliveryRepository.save(delivery);
    }

    private void recordActivity(NotificationDelivery delivery, EnquiryActivityType type, String summary) {
        if (delivery.getEnquiry() == null) return;
        activityRepository.save(new EnquiryActivity(
                delivery.getEnquiry(), delivery.getRecipient(), type, null, null, summary
        ));
    }

    private String safeMessage(RuntimeException exception) {
        var message = exception.getMessage();
        return message == null || message.isBlank() ? "Mail delivery failed." : message;
    }

    private boolean isActive(Enquiry enquiry) {
        return switch (enquiry.getStatus()) {
            case NEW, IN_REVIEW, CONTACTED, QUOTED -> true;
            case WON, LOST, CLOSED -> false;
        };
    }

    @FunctionalInterface
    private interface DeliveryAction {
        void send();
    }
}
