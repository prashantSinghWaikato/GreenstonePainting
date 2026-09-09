package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_deliveries")
public class NotificationDelivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id")
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_admin_id", nullable = false)
    private AdminUser recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "deduplication_key", nullable = false, length = 200)
    private String deduplicationKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationDeliveryStatus status = NotificationDeliveryStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    protected NotificationDelivery() {
    }

    public NotificationDelivery(Enquiry enquiry, AdminUser recipient, NotificationType type, String deduplicationKey) {
        this.enquiry = enquiry;
        this.recipient = recipient;
        this.type = type;
        this.deduplicationKey = deduplicationKey;
    }

    public void recordSent(OffsetDateTime now) {
        attemptCount += 1;
        lastAttemptAt = now;
        sentAt = now;
        status = NotificationDeliveryStatus.SENT;
        errorMessage = null;
    }

    public void recordFailure(OffsetDateTime now, String message) {
        attemptCount += 1;
        lastAttemptAt = now;
        status = NotificationDeliveryStatus.FAILED;
        errorMessage = message == null ? "Mail delivery failed." : message.substring(0, Math.min(500, message.length()));
    }

    public Enquiry getEnquiry() { return enquiry; }
    public AdminUser getRecipient() { return recipient; }
    public NotificationType getType() { return type; }
    public String getDeduplicationKey() { return deduplicationKey; }
    public NotificationDeliveryStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public OffsetDateTime getLastAttemptAt() { return lastAttemptAt; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public String getErrorMessage() { return errorMessage; }
}
