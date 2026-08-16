CREATE TABLE service_offerings (
    id UUID PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    description VARCHAR(10000) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE portfolio_projects (
    id UUID PRIMARY KEY,
    service_id UUID,
    slug VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    summary VARCHAR(600) NOT NULL,
    description VARCHAR(10000) NOT NULL,
    location VARCHAR(150),
    completed_on DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_portfolio_project_service
        FOREIGN KEY (service_id) REFERENCES service_offerings (id) ON DELETE SET NULL,
    CONSTRAINT chk_portfolio_project_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE project_images (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    alt_text VARCHAR(250) NOT NULL,
    phase VARCHAR(20) NOT NULL DEFAULT 'GALLERY',
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_project_image_project
        FOREIGN KEY (project_id) REFERENCES portfolio_projects (id) ON DELETE CASCADE,
    CONSTRAINT chk_project_image_phase
        CHECK (phase IN ('BEFORE', 'AFTER', 'GALLERY')),
    CONSTRAINT uq_project_image_object_key UNIQUE (object_key)
);

CREATE TABLE enquiries (
    id UUID PRIMARY KEY,
    service_id UUID,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(40),
    contact_preference VARCHAR(20) NOT NULL DEFAULT 'EITHER',
    property_address VARCHAR(300),
    suburb VARCHAR(100),
    message VARCHAR(10000) NOT NULL,
    estimated_budget NUMERIC(12, 2),
    desired_start_date DATE,
    internal_notes VARCHAR(10000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_enquiry_service
        FOREIGN KEY (service_id) REFERENCES service_offerings (id) ON DELETE SET NULL,
    CONSTRAINT chk_enquiry_type
        CHECK (type IN ('GENERAL', 'QUOTE_REQUEST')),
    CONSTRAINT chk_enquiry_status
        CHECK (status IN ('NEW', 'IN_REVIEW', 'CONTACTED', 'QUOTED', 'WON', 'LOST', 'CLOSED')),
    CONSTRAINT chk_enquiry_contact_preference
        CHECK (contact_preference IN ('EMAIL', 'PHONE', 'EITHER')),
    CONSTRAINT chk_enquiry_budget
        CHECK (estimated_budget IS NULL OR estimated_budget >= 0)
);

CREATE TABLE enquiry_attachments (
    id UUID PRIMARY KEY,
    enquiry_id UUID NOT NULL,
    object_key VARCHAR(500) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_enquiry_attachment_enquiry
        FOREIGN KEY (enquiry_id) REFERENCES enquiries (id) ON DELETE CASCADE,
    CONSTRAINT chk_enquiry_attachment_size
        CHECK (size_bytes >= 0)
);

CREATE TABLE testimonials (
    id UUID PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    location VARCHAR(150),
    rating INTEGER NOT NULL,
    quote_text VARCHAR(3000) NOT NULL,
    source_url VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_testimonial_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_testimonial_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_service_offerings_active_order
    ON service_offerings (active, display_order);
CREATE INDEX idx_portfolio_projects_status_completed
    ON portfolio_projects (status, completed_on);
CREATE INDEX idx_project_images_project_order
    ON project_images (project_id, display_order);
CREATE INDEX idx_enquiries_status_created
    ON enquiries (status, created_at);
CREATE INDEX idx_enquiries_email
    ON enquiries (email);
CREATE INDEX idx_enquiry_attachments_enquiry
    ON enquiry_attachments (enquiry_id);
CREATE INDEX idx_testimonials_status_featured
    ON testimonials (status, featured);
