ALTER TABLE enquiries
    ADD COLUMN assigned_admin_id UUID;

ALTER TABLE enquiries
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE enquiries
    ADD COLUMN follow_up_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE enquiries
    ADD CONSTRAINT fk_enquiry_assigned_admin
        FOREIGN KEY (assigned_admin_id) REFERENCES admin_users (id) ON DELETE SET NULL;

ALTER TABLE enquiries
    ADD CONSTRAINT chk_enquiry_priority
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'));

ALTER TABLE enquiry_activities
    ADD COLUMN note_body VARCHAR(3000);

ALTER TABLE enquiry_activities
    DROP CONSTRAINT chk_enquiry_activity_type;

ALTER TABLE enquiry_activities
    ADD CONSTRAINT chk_enquiry_activity_type
        CHECK (activity_type IN (
            'STATUS_CHANGED',
            'NOTE_UPDATED',
            'NOTE_ADDED',
            'ASSIGNMENT_CHANGED',
            'PRIORITY_CHANGED',
            'FOLLOW_UP_CHANGED'
        ));

CREATE INDEX idx_enquiries_assigned_admin ON enquiries (assigned_admin_id);
CREATE INDEX idx_enquiries_priority ON enquiries (priority);
CREATE INDEX idx_enquiries_follow_up ON enquiries (follow_up_at);
