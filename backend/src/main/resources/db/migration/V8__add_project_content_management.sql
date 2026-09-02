ALTER TABLE portfolio_projects ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE portfolio_projects ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE project_images ADD COLUMN original_filename VARCHAR(255) NOT NULL DEFAULT 'project-image';
ALTER TABLE project_images ADD COLUMN content_type VARCHAR(150) NOT NULL DEFAULT 'image/jpeg';
ALTER TABLE project_images ADD COLUMN size_bytes BIGINT NOT NULL DEFAULT 0;

ALTER TABLE project_images
    ADD CONSTRAINT chk_project_image_size CHECK (size_bytes >= 0);

CREATE TABLE project_activities (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    actor_admin_id UUID NOT NULL,
    activity_type VARCHAR(40) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_project_activity_project
        FOREIGN KEY (project_id) REFERENCES portfolio_projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_activity_actor
        FOREIGN KEY (actor_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_project_activity_type
        CHECK (activity_type IN ('CREATED', 'UPDATED', 'IMAGE_ADDED', 'IMAGE_REMOVED', 'PUBLISHED', 'UNPUBLISHED'))
);

CREATE INDEX idx_project_activities_project_created
    ON project_activities (project_id, created_at DESC);

INSERT INTO portfolio_projects (
    id, service_id, slug, title, summary, description, location, completed_on,
    status, featured, version, published_at, created_at, updated_at
)
SELECT
    '11000000-0000-0000-0000-000000000001',
    (SELECT id FROM service_offerings WHERE slug = 'exterior-painting'),
    'contemporary-exterior-renewal',
    'Contemporary Exterior Renewal',
    'A precise multi-surface exterior finish designed to complement modern architectural lines.',
    'Multi-surface exterior work' || CHR(10) || 'Preparation-led finish' || CHR(10) || 'Modern residential outcome',
    'Hamilton, Waikato', NULL, 'PUBLISHED', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM portfolio_projects WHERE slug = 'contemporary-exterior-renewal');

INSERT INTO portfolio_projects (
    id, service_id, slug, title, summary, description, location, completed_on,
    status, featured, version, published_at, created_at, updated_at
)
SELECT
    '11000000-0000-0000-0000-000000000002',
    (SELECT id FROM service_offerings WHERE slug = 'new-builds-renovations'),
    'new-build-interior-package',
    'New Build Interior Package',
    'A coordinated interior package with clean transitions across walls, ceilings, trim, and cabinetry.',
    'Coordinated interior package' || CHR(10) || 'Walls, ceilings, and trim' || CHR(10) || 'Consistent new-build finish',
    'Waikato', NULL, 'PUBLISHED', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM portfolio_projects WHERE slug = 'new-build-interior-package');

INSERT INTO portfolio_projects (
    id, service_id, slug, title, summary, description, location, completed_on,
    status, featured, version, published_at, created_at, updated_at
)
SELECT
    '11000000-0000-0000-0000-000000000003',
    (SELECT id FROM service_offerings WHERE slug = 'exterior-painting'),
    'residential-transformation',
    'Residential Transformation',
    'A complete exterior refresh that modernised the property while improving surface protection.',
    'Complete exterior refresh' || CHR(10) || 'Updated colour direction' || CHR(10) || 'Before-and-after transformation',
    'Hamilton', NULL, 'PUBLISHED', TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM portfolio_projects WHERE slug = 'residential-transformation');

INSERT INTO project_images (
    id, project_id, object_key, alt_text, phase, display_order,
    original_filename, content_type, size_bytes, created_at, updated_at
)
SELECT '12000000-0000-0000-0000-000000000001', id, 'static:/images/greenstone-exterior.webp',
       'Freshly painted modern residential exterior', 'AFTER', 0,
       'greenstone-exterior.webp', 'image/webp', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM portfolio_projects WHERE slug = 'contemporary-exterior-renewal'
  AND NOT EXISTS (SELECT 1 FROM project_images WHERE object_key = 'static:/images/greenstone-exterior.webp');

INSERT INTO project_images (
    id, project_id, object_key, alt_text, phase, display_order,
    original_filename, content_type, size_bytes, created_at, updated_at
)
SELECT '12000000-0000-0000-0000-000000000002', id, 'static:/images/greenstone-kitchen.webp',
       'Modern kitchen and living area with crisp painted surfaces', 'AFTER', 0,
       'greenstone-kitchen.webp', 'image/webp', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM portfolio_projects WHERE slug = 'new-build-interior-package'
  AND NOT EXISTS (SELECT 1 FROM project_images WHERE object_key = 'static:/images/greenstone-kitchen.webp');

INSERT INTO project_images (
    id, project_id, object_key, alt_text, phase, display_order,
    original_filename, content_type, size_bytes, created_at, updated_at
)
SELECT '12000000-0000-0000-0000-000000000003', id, 'static:/images/greenstone-before-after.jpg',
       'Before and after exterior painting transformation', 'BEFORE', 0,
       'greenstone-before-after.jpg', 'image/jpeg', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM portfolio_projects WHERE slug = 'residential-transformation'
  AND NOT EXISTS (SELECT 1 FROM project_images WHERE object_key = 'static:/images/greenstone-before-after.jpg');
