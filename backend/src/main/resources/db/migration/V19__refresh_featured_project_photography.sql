-- Replace only the bundled default; preserve any image uploaded through the admin area.
UPDATE project_images
SET object_key = 'static:/images/projects/featured-townhouse-exterior.webp',
    alt_text = 'White and charcoal multi-unit townhouse exterior',
    original_filename = 'featured-townhouse-exterior.webp',
    content_type = 'image/webp',
    size_bytes = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE project_id = (
    SELECT id FROM portfolio_projects WHERE slug = 'contemporary-exterior-renewal'
)
AND object_key = 'static:/images/greenstone-exterior.webp';

UPDATE project_images
SET object_key = 'static:/images/projects/featured-interior-bedroom.webp',
    alt_text = 'Freshly painted white bedroom with decorative ceiling panels',
    original_filename = 'featured-interior-bedroom.webp',
    content_type = 'image/webp',
    size_bytes = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE project_id = (
    SELECT id FROM portfolio_projects WHERE slug = 'new-build-interior-package'
)
AND object_key = 'static:/images/greenstone-kitchen.webp';

UPDATE project_images
SET object_key = 'static:/images/projects/featured-twilight-exterior.webp',
    alt_text = 'Twilight view of two freshly painted modern homes',
    phase = 'AFTER',
    original_filename = 'featured-twilight-exterior.webp',
    content_type = 'image/webp',
    size_bytes = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE project_id = (
    SELECT id FROM portfolio_projects WHERE slug = 'residential-transformation'
)
AND object_key = 'static:/images/greenstone-before-after.jpg';

-- Classify the bedroom project under interior painting while preserving later admin changes.
UPDATE portfolio_projects
SET service_id = (
        SELECT id FROM service_offerings WHERE slug = 'interior-painting'
    ),
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'new-build-interior-package'
AND service_id = (
    SELECT id FROM service_offerings WHERE slug = 'new-builds-renovations'
);

UPDATE portfolio_projects
SET service_id = (
        SELECT id FROM service_offerings WHERE slug = 'new-builds-renovations'
    ),
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'residential-transformation'
AND service_id = (
    SELECT id FROM service_offerings WHERE slug = 'exterior-painting'
);
