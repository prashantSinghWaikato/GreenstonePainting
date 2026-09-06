CREATE TABLE blog_articles (
    id UUID PRIMARY KEY,
    slug VARCHAR(160) NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    short_title VARCHAR(120) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    excerpt VARCHAR(600) NOT NULL,
    body VARCHAR(20000) NOT NULL,
    read_time_minutes INTEGER NOT NULL DEFAULT 5,
    featured_image_object_key VARCHAR(500),
    featured_image_alt VARCHAR(250),
    featured_image_filename VARCHAR(255),
    featured_image_content_type VARCHAR(150),
    featured_image_size_bytes BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_blog_article_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT chk_blog_article_read_time CHECK (read_time_minutes BETWEEN 1 AND 60),
    CONSTRAINT chk_blog_article_image_size CHECK (featured_image_size_bytes IS NULL OR featured_image_size_bytes >= 0)
);

CREATE TABLE blog_article_activities (
    id UUID PRIMARY KEY,
    article_id UUID NOT NULL,
    actor_admin_id UUID NOT NULL,
    activity_type VARCHAR(40) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_blog_activity_article
        FOREIGN KEY (article_id) REFERENCES blog_articles (id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_activity_actor
        FOREIGN KEY (actor_admin_id) REFERENCES admin_users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_blog_activity_type
        CHECK (activity_type IN ('CREATED', 'UPDATED', 'IMAGE_CHANGED', 'IMAGE_REMOVED', 'PUBLISHED', 'UNPUBLISHED'))
);

CREATE INDEX idx_blog_articles_status_published
    ON blog_articles (status, published_at DESC);

CREATE INDEX idx_blog_article_activities_article_created
    ON blog_article_activities (article_id, created_at DESC);

INSERT INTO blog_articles (
    id, slug, title, short_title, topic, excerpt, body, read_time_minutes,
    featured_image_object_key, featured_image_alt, featured_image_filename,
    featured_image_content_type, featured_image_size_bytes, status, published_at,
    version, created_at, updated_at
) VALUES (
    '21000000-0000-0000-0000-000000000001',
    'how-painters-prepare-your-home-for-a-smooth-paint-job',
    'How Painters Prepare Your Home for a Smooth Paint Job',
    'Preparation for a Smooth Paint Job',
    'Preparation',
    'A durable, even finish starts well before the first coat. Here is how professional preparation protects your home and improves the final result.',
    'Fresh colour may be the most visible part of a painting project, but preparation is what makes the finish look consistent and last. Before paint is opened, every surface needs to be inspected, protected, cleaned, repaired, and prepared for the selected coating system.' || CHR(10) || CHR(10) ||
    'A professional process also reduces disruption. Clear planning and careful protection help keep the project controlled from the first day through to completion.' || CHR(10) || CHR(10) ||
    '## Inspecting and planning the work' || CHR(10) ||
    'The first step is to assess walls, ceilings, trim, and exterior surfaces for cracks, peeling coatings, moisture damage, stains, and previous repairs. This defines the preparation, materials, access, drying time, and order of work.' || CHR(10) || CHR(10) ||
    '## Protecting floors, furniture, and fittings' || CHR(10) ||
    'Furniture is moved or covered, floors are protected, and fittings are removed or carefully masked. Good protection allows the team to work efficiently while respecting the rest of the property.' || CHR(10) || CHR(10) ||
    '## Cleaning and repairing the surfaces' || CHR(10) ||
    'Dust, residue, oils, mould, and exterior contaminants can prevent coatings from bonding. Surfaces are cleaned, repaired, and allowed to dry before further preparation begins.' || CHR(10) || CHR(10) ||
    '## Sanding and priming' || CHR(10) ||
    'Sanding removes rough edges and improves adhesion. Primer is selected where needed to seal porous areas, control stains, support adhesion, or create a consistent base for finishing coats.' || CHR(10) || CHR(10) ||
    '## Why preparation matters in Waikato homes' || CHR(10) ||
    'Preparation improves appearance, adhesion, and durability. Skipping it can leave visible defects and contribute to early peeling, cracking, bubbling, or uneven colour.',
    6, 'static:/images/greenstone-bedroom.webp',
    'Freshly painted bedroom with carefully finished walls and trim',
    'greenstone-bedroom.webp', 'image/webp', 0, 'PUBLISHED',
    '2025-09-19 00:00:00+00', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO blog_articles (
    id, slug, title, short_title, topic, excerpt, body, read_time_minutes,
    featured_image_object_key, featured_image_alt, featured_image_filename,
    featured_image_content_type, featured_image_size_bytes, status, published_at,
    version, created_at, updated_at
) VALUES (
    '21000000-0000-0000-0000-000000000002',
    '822-2',
    'A Guide to Compliance-Friendly Paint Options for Safe and Healthy Facilities',
    'Compliance-Friendly Paint Options',
    'Materials',
    'Schools, healthcare spaces, hospitality venues, and other facilities need coatings selected for safety, hygiene, cleaning, and operational demands.',
    'Paint selection in a public or regulated environment involves more than colour. The coating system should suit how the facility is used, how frequently it is cleaned, who occupies the space, and any standards that apply to the site.' || CHR(10) || CHR(10) ||
    'Requirements vary between projects. Product data, the substrate, the building specification, and current New Zealand requirements should all be reviewed before a final coating system is approved.' || CHR(10) || CHR(10) ||
    '## Why paint choice matters' || CHR(10) ||
    'The right specification supports indoor air quality, cleaning routines, durability, and the safe operation of the facility.' || CHR(10) ||
    '- Occupant sensitivity and ventilation' || CHR(10) ||
    '- Cleaning frequency and sanitation methods' || CHR(10) ||
    '- Moisture, mould, and abrasion exposure' || CHR(10) ||
    '- Required product documentation and project specifications' || CHR(10) || CHR(10) ||
    '## Low-odour and low-VOC options' || CHR(10) ||
    'Low- or zero-VOC products can help reduce odour and emissions during application and curing. The complete product specification and ventilation plan still need to be considered.' || CHR(10) || CHR(10) ||
    '## Washable, durable finishes' || CHR(10) ||
    'High-use spaces benefit from coatings designed to withstand repeated cleaning and contact. The appropriate sheen and resin system depend on the surface, traffic level, appearance, and cleaning products.' || CHR(10) || CHR(10) ||
    '## A practical specification checklist' || CHR(10) ||
    '- Confirm the substrate and existing coating condition.' || CHR(10) ||
    '- Review product technical and safety data.' || CHR(10) ||
    '- Check cleaning, hygiene, and durability requirements.' || CHR(10) ||
    '- Plan ventilation, access, curing time, and operational downtime.' || CHR(10) || CHR(10) ||
    '## Get project-specific advice' || CHR(10) ||
    'This guide is general information, not a compliance determination. The coating specification should be confirmed for the particular building, use, and current requirements before work starts.',
    7, 'static:/images/greenstone-kitchen.webp',
    'Clean modern interior finished with durable painted surfaces',
    'greenstone-kitchen.webp', 'image/webp', 0, 'PUBLISHED',
    '2025-08-24 00:00:00+00', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO blog_articles (
    id, slug, title, short_title, topic, excerpt, body, read_time_minutes,
    featured_image_object_key, featured_image_alt, featured_image_filename,
    featured_image_content_type, featured_image_size_bytes, status, published_at,
    version, created_at, updated_at
) VALUES (
    '21000000-0000-0000-0000-000000000003',
    'wood-staining-benefits-you-need-to-take-advantage-of',
    'Wood Staining Benefits You Need to Take Advantage Of',
    'The Benefits of Wood Staining',
    'Care guide',
    'Stain can enhance timber grain while helping protect suitable wood surfaces from moisture, sunlight, and everyday exposure.',
    'Timber brings warmth and natural variation to decks, fences, doors, joinery, and architectural features. A suitable stain can enrich that character while adding protection appropriate to the location and timber species.' || CHR(10) || CHR(10) ||
    'The best result depends on preparation, product compatibility, exposure, and the condition of the wood. A small test area is useful when colour and absorption need to be confirmed.' || CHR(10) || CHR(10) ||
    '## What wood staining does' || CHR(10) ||
    'Stain adds colour while allowing the natural grain to remain visible to varying degrees. Products range from clear and semi-transparent finishes to more solid colour systems.' || CHR(10) || CHR(10) ||
    '## A typical staining process' || CHR(10) ||
    '- Assess the timber species, condition, moisture, and existing coating.' || CHR(10) ||
    '- Clean the surface and remove failed finishes or contaminants.' || CHR(10) ||
    '- Sand or prepare the timber as required for even absorption.' || CHR(10) ||
    '- Apply the selected stain in controlled coats following product guidance.' || CHR(10) ||
    '- Use a compatible protective finish where the coating system requires it.' || CHR(10) || CHR(10) ||
    '## Supports weather protection' || CHR(10) ||
    'Exterior timber is exposed to ultraviolet light, rain, and changing moisture levels. A correctly selected and maintained exterior stain can help reduce weathering and moisture uptake.' || CHR(10) || CHR(10) ||
    '## Can simplify future maintenance' || CHR(10) ||
    'Some penetrating stain systems weather gradually rather than forming a thick film. Depending on the product and condition, maintenance may be more straightforward than removing a heavily peeling coating.' || CHR(10) || CHR(10) ||
    '## Plan before you stain' || CHR(10) ||
    'Professional assessment helps identify whether timber should be stained, painted, repaired, or left untreated. Greenstone Painting can recommend a practical preparation and coating approach.',
    5, 'static:/images/greenstone-exterior.webp',
    'Finished residential exterior with protected timber details',
    'greenstone-exterior.webp', 'image/webp', 0, 'PUBLISHED',
    '2025-08-14 00:00:00+00', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
