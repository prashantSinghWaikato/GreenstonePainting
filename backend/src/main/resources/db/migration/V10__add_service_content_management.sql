ALTER TABLE service_offerings ADD COLUMN label VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE service_offerings ADD COLUMN inclusions VARCHAR(3000) NOT NULL DEFAULT '';
ALTER TABLE service_offerings ADD COLUMN service_note VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE service_offerings ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE service_offerings ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE service_offerings ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE service_offerings ADD COLUMN featured_image_object_key VARCHAR(500);
ALTER TABLE service_offerings ADD COLUMN featured_image_alt VARCHAR(250);
ALTER TABLE service_offerings ADD COLUMN featured_image_filename VARCHAR(255);
ALTER TABLE service_offerings ADD COLUMN featured_image_content_type VARCHAR(150);
ALTER TABLE service_offerings ADD COLUMN featured_image_size_bytes BIGINT;

UPDATE service_offerings
SET status = CASE WHEN active THEN 'PUBLISHED' ELSE 'DRAFT' END,
    published_at = CASE WHEN active THEN CURRENT_TIMESTAMP ELSE NULL END;

UPDATE service_offerings SET
    label = 'Interior environments',
    description = 'A controlled interior painting service for occupied homes, empty properties, renovations, and new spaces. We plan protection, preparation, product selection, and sequencing around the surfaces and how the property is used.',
    inclusions = 'Walls and ceilings' || CHR(10) || 'Doors, trim, and skirtings' || CHR(10) || 'Surface repairs and preparation' || CHR(10) || 'Colour and sheen guidance',
    service_note = 'Suitable for single-room refreshes through to complete interior repaints.',
    featured_image_object_key = 'static:/images/greenstone-bedroom.webp',
    featured_image_alt = 'Freshly painted residential bedroom interior',
    featured_image_filename = 'greenstone-bedroom.webp',
    featured_image_content_type = 'image/webp',
    featured_image_size_bytes = 0
WHERE slug = 'interior-painting';

UPDATE service_offerings SET
    label = 'Exterior protection',
    description = 'Exterior finishes need to suit the substrate, exposure, existing coating condition, and local environment. We assess the property before recommending preparation and a coating approach for the work.',
    inclusions = 'Weatherboards and cladding' || CHR(10) || 'Eaves, fascia, and exterior trim' || CHR(10) || 'Cleaning and surface preparation' || CHR(10) || 'Compatible exterior coating systems',
    service_note = 'Designed for residential exteriors and selected commercial properties across Waikato.',
    featured_image_object_key = 'static:/images/greenstone-exterior.webp',
    featured_image_alt = 'Professionally finished modern house exterior',
    featured_image_filename = 'greenstone-exterior.webp',
    featured_image_content_type = 'image/webp',
    featured_image_size_bytes = 0
WHERE slug = 'exterior-painting';

UPDATE service_offerings SET
    label = 'Commercial delivery',
    description = 'Clear scope, scheduling, and communication are essential in operational spaces. We coordinate painting work around access, other trades, business requirements, and the surfaces included in the agreed project.',
    inclusions = 'Offices and retail spaces' || CHR(10) || 'Property and facility refreshes' || CHR(10) || 'Interior and exterior surfaces' || CHR(10) || 'Planned staging and handover',
    service_note = 'Project timing and product requirements are confirmed during assessment.',
    featured_image_object_key = 'static:/images/greenstone-kitchen.webp',
    featured_image_alt = 'Clean modern interior showing precise painted finishes',
    featured_image_filename = 'greenstone-kitchen.webp',
    featured_image_content_type = 'image/webp',
    featured_image_size_bytes = 0
WHERE slug = 'commercial-painting';

UPDATE service_offerings SET
    label = 'Roof coating systems',
    description = 'Roof painting begins with checking the roof material, condition, access, and whether repairs or specialist work are needed before coating. The final scope is based on a site inspection rather than appearance alone.',
    inclusions = 'Condition and access review' || CHR(10) || 'Cleaning and preparation' || CHR(10) || 'Coating compatibility checks' || CHR(10) || 'Planned application process',
    service_note = 'Availability depends on roof condition, material, pitch, and safe access.',
    featured_image_object_key = 'static:/images/greenstone-before-after.jpg',
    featured_image_alt = 'Residential exterior painting transformation',
    featured_image_filename = 'greenstone-before-after.jpg',
    featured_image_content_type = 'image/jpeg',
    featured_image_size_bytes = 0
WHERE slug = 'roof-painting';

UPDATE service_offerings SET
    label = 'Coordinated project work',
    description = 'Painting for a build or renovation needs to integrate with the wider programme. We clarify surfaces, finish levels, sequencing, access, and touch-up expectations so the painting package can move cleanly toward handover.',
    inclusions = 'New residential interiors' || CHR(10) || 'Renovation painting packages' || CHR(10) || 'Coordination with builders and trades' || CHR(10) || 'Final review and touch-ups',
    service_note = 'Scope can be tailored to the build stage and agreed finish schedule.',
    featured_image_object_key = 'static:/images/greenstone-kitchen.webp',
    featured_image_alt = 'Finished kitchen and living area in a new residential build',
    featured_image_filename = 'greenstone-kitchen.webp',
    featured_image_content_type = 'image/webp',
    featured_image_size_bytes = 0
WHERE slug = 'new-builds-renovations';

UPDATE service_offerings SET
    label = 'Timber care',
    description = 'Timber condition, species, previous finishes, and exposure affect how a stain will look and perform. We assess these factors before selecting preparation and a suitable transparent, semi-transparent, or solid finish.',
    inclusions = 'Decks and exterior timber' || CHR(10) || 'Fences and screening' || CHR(10) || 'Cleaning and preparation' || CHR(10) || 'Stain colour and finish selection',
    service_note = 'A test area may be recommended because timber absorbs stain differently.',
    featured_image_object_key = 'static:/images/greenstone-exterior.webp',
    featured_image_alt = 'Residential exterior with finished timber elements',
    featured_image_filename = 'greenstone-exterior.webp',
    featured_image_content_type = 'image/webp',
    featured_image_size_bytes = 0
WHERE slug = 'deck-fence-staining';

ALTER TABLE service_offerings ADD CONSTRAINT chk_service_offering_status
    CHECK (status IN ('DRAFT', 'PUBLISHED'));
ALTER TABLE service_offerings ADD CONSTRAINT chk_service_display_order
    CHECK (display_order BETWEEN 1 AND 100);
ALTER TABLE service_offerings ADD CONSTRAINT chk_service_image_size
    CHECK (featured_image_size_bytes IS NULL OR featured_image_size_bytes >= 0);

CREATE TABLE service_offering_activities (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    actor_admin_id UUID NOT NULL,
    activity_type VARCHAR(40) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_service_activity_service FOREIGN KEY (service_id) REFERENCES service_offerings (id) ON DELETE CASCADE,
    CONSTRAINT fk_service_activity_actor FOREIGN KEY (actor_admin_id) REFERENCES admin_users (id),
    CONSTRAINT chk_service_activity_type CHECK (activity_type IN ('UPDATED', 'IMAGE_CHANGED', 'IMAGE_REMOVED', 'PUBLISHED', 'UNPUBLISHED'))
);

CREATE INDEX idx_service_offerings_status_order ON service_offerings (status, display_order);
CREATE INDEX idx_service_activity_service_created ON service_offering_activities (service_id, created_at);
