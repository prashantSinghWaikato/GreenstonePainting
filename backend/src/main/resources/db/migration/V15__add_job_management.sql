CREATE TABLE painting_jobs (
    id UUID PRIMARY KEY,
    quote_id UUID NOT NULL UNIQUE,
    enquiry_id UUID NOT NULL,
    job_number VARCHAR(40) NOT NULL UNIQUE,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    assigned_admin_id UUID,
    created_by_admin_id UUID NOT NULL,
    updated_by_admin_id UUID NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(254) NOT NULL,
    customer_phone VARCHAR(40),
    property_address VARCHAR(300),
    title VARCHAR(200) NOT NULL,
    service_title VARCHAR(200),
    scope TEXT NOT NULL,
    site_instructions TEXT,
    internal_notes TEXT,
    scheduled_start_date DATE,
    scheduled_end_date DATE,
    actual_started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_job_quote FOREIGN KEY (quote_id) REFERENCES quotes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_job_enquiry FOREIGN KEY (enquiry_id) REFERENCES enquiries (id) ON DELETE RESTRICT,
    CONSTRAINT fk_job_assignee FOREIGN KEY (assigned_admin_id) REFERENCES admin_users (id) ON DELETE SET NULL,
    CONSTRAINT fk_job_created_by FOREIGN KEY (created_by_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_job_updated_by FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_job_status CHECK (status IN ('PLANNED', 'SCHEDULED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_job_dates CHECK (scheduled_end_date IS NULL OR scheduled_start_date IS NULL OR scheduled_end_date >= scheduled_start_date)
);

CREATE TABLE job_photos (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    phase VARCHAR(20) NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_job_photo_job FOREIGN KEY (job_id) REFERENCES painting_jobs (id) ON DELETE CASCADE,
    CONSTRAINT chk_job_photo_phase CHECK (phase IN ('BEFORE', 'PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_job_photo_size CHECK (size_bytes > 0)
);

CREATE TABLE job_activities (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    actor_admin_id UUID,
    activity_type VARCHAR(30) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    note_body TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_job_activity_job FOREIGN KEY (job_id) REFERENCES painting_jobs (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_activity_actor FOREIGN KEY (actor_admin_id) REFERENCES admin_users (id) ON DELETE SET NULL,
    CONSTRAINT chk_job_activity_type CHECK (activity_type IN ('CREATED', 'STATUS_CHANGED', 'SCHEDULE_CHANGED', 'ASSIGNMENT_CHANGED', 'DETAILS_UPDATED', 'PHOTO_ADDED'))
);

CREATE INDEX idx_jobs_status_schedule ON painting_jobs (status, scheduled_start_date);
CREATE INDEX idx_jobs_assignee_schedule ON painting_jobs (assigned_admin_id, scheduled_start_date);
CREATE INDEX idx_job_photos_job_created ON job_photos (job_id, created_at DESC);
CREATE INDEX idx_job_activities_job_created ON job_activities (job_id, created_at DESC);
