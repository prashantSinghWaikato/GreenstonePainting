-- Retain custom uploaded photographs; update only the previous bundled defaults.
UPDATE service_offerings
SET featured_image_object_key = NULL, featured_image_filename = NULL,
    featured_image_alt = NULL, featured_image_content_type = NULL,
    featured_image_size_bytes = NULL, version = version + 1
WHERE slug = 'roof-painting'
  AND featured_image_object_key = 'static:/images/greenstone-before-after.jpg';

UPDATE service_offerings
SET featured_image_object_key = 'static:/images/projects/staining-07.webp',
    featured_image_filename = 'staining-07.webp',
    featured_image_alt = 'Timber deck during stain application',
    featured_image_content_type = 'image/webp', featured_image_size_bytes = 0,
    version = version + 1
WHERE slug = 'deck-fence-staining'
  AND featured_image_object_key IN ('static:/images/projects/staining-11.webp', 'static:/images/greenstone-exterior.webp');
