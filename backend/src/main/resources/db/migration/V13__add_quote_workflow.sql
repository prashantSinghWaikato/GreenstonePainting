CREATE TABLE quotes (
    id UUID PRIMARY KEY,
    enquiry_id UUID NOT NULL,
    created_by_admin_id UUID NOT NULL,
    updated_by_admin_id UUID NOT NULL,
    quote_number VARCHAR(40) NOT NULL UNIQUE,
    revision_number INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(254) NOT NULL,
    property_address VARCHAR(300),
    title VARCHAR(200) NOT NULL,
    scope TEXT NOT NULL,
    terms TEXT NOT NULL,
    gst_rate NUMERIC(5, 4) NOT NULL DEFAULT 0.1500,
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    gst_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    optional_total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valid_until DATE NOT NULL,
    estimated_start_date DATE,
    estimated_end_date DATE,
    response_token_hash VARCHAR(64),
    response_token_expires_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    accepted_at TIMESTAMP WITH TIME ZONE,
    declined_at TIMESTAMP WITH TIME ZONE,
    decline_reason VARCHAR(1000),
    content_updated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_quote_enquiry FOREIGN KEY (enquiry_id) REFERENCES enquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_quote_created_by FOREIGN KEY (created_by_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_quote_updated_by FOREIGN KEY (updated_by_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT uq_quote_enquiry_revision UNIQUE (enquiry_id, revision_number),
    CONSTRAINT chk_quote_revision CHECK (revision_number > 0),
    CONSTRAINT chk_quote_status CHECK (status IN ('DRAFT', 'SENT', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'SUPERSEDED')),
    CONSTRAINT chk_quote_amounts CHECK (subtotal >= 0 AND gst_amount >= 0 AND total >= 0 AND optional_total >= 0),
    CONSTRAINT chk_quote_gst_rate CHECK (gst_rate >= 0 AND gst_rate <= 1)
);

CREATE TABLE quote_items (
    id UUID PRIMARY KEY,
    quote_id UUID NOT NULL,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit VARCHAR(40) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    optional BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_quote_item_quote FOREIGN KEY (quote_id) REFERENCES quotes (id) ON DELETE CASCADE,
    CONSTRAINT chk_quote_item_category CHECK (category IN ('LABOUR', 'MATERIALS', 'PREPARATION', 'OPTIONAL', 'OTHER')),
    CONSTRAINT chk_quote_item_values CHECK (quantity > 0 AND unit_price >= 0 AND display_order >= 0)
);

CREATE TABLE quote_activities (
    id UUID PRIMARY KEY,
    quote_id UUID NOT NULL,
    actor_admin_id UUID,
    activity_type VARCHAR(30) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_quote_activity_quote FOREIGN KEY (quote_id) REFERENCES quotes (id) ON DELETE CASCADE,
    CONSTRAINT fk_quote_activity_actor FOREIGN KEY (actor_admin_id) REFERENCES admin_users (id) ON DELETE SET NULL,
    CONSTRAINT chk_quote_activity_type CHECK (activity_type IN ('CREATED', 'UPDATED', 'SENT', 'ACCEPTED', 'DECLINED', 'REVISION_CREATED'))
);

CREATE INDEX idx_quotes_enquiry_revision ON quotes (enquiry_id, revision_number DESC);
CREATE INDEX idx_quotes_status_valid_until ON quotes (status, valid_until);
CREATE INDEX idx_quotes_response_token_hash ON quotes (response_token_hash);
CREATE INDEX idx_quote_items_quote_order ON quote_items (quote_id, display_order);
CREATE INDEX idx_quote_activities_quote_created ON quote_activities (quote_id, created_at DESC);
