-- Only replace original bundled defaults; retain staff-uploaded or edited images.
UPDATE service_offerings SET featured_image_object_key = 'static:/images/projects/feature-walls-02.webp', featured_image_filename = 'feature-walls-02.webp', featured_image_alt = 'Bedroom with a dark painted panel feature wall', version = version + 1
WHERE slug = 'interior-painting' AND featured_image_object_key = 'static:/images/greenstone-bedroom.webp';

UPDATE service_offerings SET featured_image_object_key = 'static:/images/projects/renovation-03.webp', featured_image_filename = 'renovation-03.webp', featured_image_alt = 'Weatherboard home during exterior renovation', version = version + 1
WHERE slug = 'exterior-painting' AND featured_image_object_key = 'static:/images/greenstone-exterior.webp';

UPDATE service_offerings SET featured_image_object_key = 'static:/images/projects/commercial-01.webp', featured_image_filename = 'commercial-01.webp', featured_image_alt = 'Blue and white commercial building exterior', version = version + 1
WHERE slug = 'commercial-painting' AND featured_image_object_key = 'static:/images/greenstone-kitchen.webp';

UPDATE service_offerings SET featured_image_object_key = 'static:/images/projects/new-builds-01.webp', featured_image_filename = 'new-builds-01.webp', featured_image_alt = 'Open-plan living area with white walls', version = version + 1
WHERE slug = 'new-builds-renovations' AND featured_image_object_key = 'static:/images/greenstone-kitchen.webp';

UPDATE service_offerings SET featured_image_object_key = 'static:/images/projects/staining-11.webp', featured_image_filename = 'staining-11.webp', featured_image_alt = 'Stained timber deck and balustrade', version = version + 1
WHERE slug = 'deck-fence-staining' AND featured_image_object_key = 'static:/images/greenstone-exterior.webp';
