ALTER TABLE enquiries
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE enquiry_activities (
    id UUID PRIMARY KEY,
    enquiry_id UUID NOT NULL,
    actor_admin_id UUID NOT NULL,
    activity_type VARCHAR(30) NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30),
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_enquiry_activity_enquiry
        FOREIGN KEY (enquiry_id) REFERENCES enquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_enquiry_activity_actor
        FOREIGN KEY (actor_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_enquiry_activity_type
        CHECK (activity_type IN ('STATUS_CHANGED', 'NOTE_UPDATED')),
    CONSTRAINT chk_enquiry_activity_previous_status
        CHECK (previous_status IS NULL OR previous_status IN ('NEW', 'IN_REVIEW', 'CONTACTED', 'QUOTED', 'WON', 'LOST', 'CLOSED')),
    CONSTRAINT chk_enquiry_activity_new_status
        CHECK (new_status IS NULL OR new_status IN ('NEW', 'IN_REVIEW', 'CONTACTED', 'QUOTED', 'WON', 'LOST', 'CLOSED'))
);

CREATE INDEX idx_enquiry_activities_enquiry_created
    ON enquiry_activities (enquiry_id, created_at DESC);

CREATE INDEX idx_enquiry_activities_actor
    ON enquiry_activities (actor_admin_id);
