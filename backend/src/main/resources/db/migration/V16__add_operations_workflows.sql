ALTER TABLE admin_users ADD COLUMN job_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE painting_jobs ADD COLUMN customer_signoff_name VARCHAR(200);
ALTER TABLE painting_jobs ADD COLUMN customer_signoff_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE job_checklist_items (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    label VARCHAR(300) NOT NULL,
    position INTEGER NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,
    completed_by_admin_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_job_checklist_job FOREIGN KEY (job_id) REFERENCES painting_jobs (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_checklist_admin FOREIGN KEY (completed_by_admin_id) REFERENCES admin_users (id) ON DELETE SET NULL,
    CONSTRAINT chk_job_checklist_position CHECK (position >= 0)
);
CREATE INDEX idx_job_checklist_job ON job_checklist_items (job_id, position);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL UNIQUE,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    subtotal NUMERIC(12,2) NOT NULL,
    gst_amount NUMERIC(12,2) NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    amount_paid NUMERIC(12,2) NOT NULL DEFAULT 0,
    due_date DATE NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,
    payment_reference VARCHAR(200),
    notes TEXT,
    created_by_admin_id UUID NOT NULL,
    updated_by_admin_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_invoice_job FOREIGN KEY (job_id) REFERENCES painting_jobs (id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_created_by FOREIGN KEY (created_by_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_updated_by FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_invoice_status CHECK (status IN ('DRAFT','SENT','PART_PAID','PAID','VOID')),
    CONSTRAINT chk_invoice_amounts CHECK (subtotal >= 0 AND gst_amount >= 0 AND total >= 0 AND amount_paid >= 0 AND amount_paid <= total)
);
CREATE INDEX idx_invoices_status_due ON invoices (status, due_date);

ALTER TABLE notification_deliveries ADD COLUMN job_id UUID;
ALTER TABLE notification_deliveries ADD CONSTRAINT fk_notification_delivery_job
    FOREIGN KEY (job_id) REFERENCES painting_jobs (id) ON DELETE CASCADE;
ALTER TABLE notification_deliveries DROP CONSTRAINT chk_notification_delivery_type;
ALTER TABLE notification_deliveries ADD CONSTRAINT chk_notification_delivery_type
    CHECK (notification_type IN ('ASSIGNMENT','FOLLOW_UP','DAILY_DIGEST','JOB_ASSIGNMENT','JOB_SCHEDULE','JOB_STATUS','JOB_REMINDER'));
CREATE INDEX idx_notification_deliveries_job ON notification_deliveries (job_id, created_at DESC);

ALTER TABLE job_activities DROP CONSTRAINT chk_job_activity_type;
ALTER TABLE job_activities ADD CONSTRAINT chk_job_activity_type CHECK (activity_type IN (
    'CREATED','STATUS_CHANGED','SCHEDULE_CHANGED','ASSIGNMENT_CHANGED','DETAILS_UPDATED','PHOTO_ADDED',
    'CHECKLIST_UPDATED','SIGNOFF_RECORDED','INVOICE_CREATED','NOTIFICATION_SENT','NOTIFICATION_FAILED'
));
