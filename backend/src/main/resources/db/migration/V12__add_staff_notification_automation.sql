ALTER TABLE admin_users ADD COLUMN assignment_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE admin_users ADD COLUMN follow_up_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE admin_users ADD COLUMN daily_digest_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE enquiry_activities DROP CONSTRAINT chk_enquiry_activity_type;
ALTER TABLE enquiry_activities
    ADD CONSTRAINT chk_enquiry_activity_type
        CHECK (activity_type IN (
            'STATUS_CHANGED',
            'NOTE_UPDATED',
            'NOTE_ADDED',
            'ASSIGNMENT_CHANGED',
            'PRIORITY_CHANGED',
            'FOLLOW_UP_CHANGED',
            'NOTIFICATION_SENT',
            'NOTIFICATION_FAILED'
        ));

CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY,
    enquiry_id UUID,
    recipient_admin_id UUID NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    deduplication_key VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_notification_delivery_enquiry
        FOREIGN KEY (enquiry_id) REFERENCES enquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_delivery_recipient
        FOREIGN KEY (recipient_admin_id) REFERENCES admin_users (id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_delivery_type
        CHECK (notification_type IN ('ASSIGNMENT', 'FOLLOW_UP', 'DAILY_DIGEST')),
    CONSTRAINT chk_notification_delivery_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT chk_notification_delivery_attempts CHECK (attempt_count >= 0),
    CONSTRAINT uq_notification_delivery_dedup
        UNIQUE (recipient_admin_id, notification_type, deduplication_key)
);

CREATE INDEX idx_notification_deliveries_retry
    ON notification_deliveries (status, last_attempt_at);
CREATE INDEX idx_notification_deliveries_enquiry
    ON notification_deliveries (enquiry_id, created_at DESC);
