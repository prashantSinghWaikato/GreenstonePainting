export type BlogSection = {
  heading: string
  paragraphs?: string[]
  bullets?: string[]
}

export type BlogPost = {
  path: string
  title: string
  shortTitle: string
  topic: string
  date: string
  readTime: string
  image: string
  imageAlt: string
  excerpt: string
  introduction: string[]
  sections: BlogSection[]
}

export const blogPosts: BlogPost[] = [
  {
    path: '/how-painters-prepare-your-home-for-a-smooth-paint-job/',
    title: 'How Painters Prepare Your Home for a Smooth Paint Job',
    shortTitle: 'Preparation for a Smooth Paint Job',
    topic: 'Preparation',
    date: '19 September 2025',
    readTime: '6 min read',
    image: '/images/greenstone-bedroom.webp',
    imageAlt: 'Freshly painted bedroom with carefully finished walls and trim',
    excerpt: 'A durable, even finish starts well before the first coat. Here is how professional preparation protects your home and improves the final result.',
    introduction: [
      'Fresh colour may be the most visible part of a painting project, but preparation is what makes the finish look consistent and last. Before paint is opened, the room and every surface need to be inspected, protected, cleaned, repaired, and prepared for the selected coating system.',
      'A professional process also reduces disruption. Clear planning, careful protection, and a final pre-paint check help keep the project controlled from the first day through to completion.',
    ],
    sections: [
      {
        heading: 'Inspecting and planning the work',
        paragraphs: ['The first step is to assess walls, ceilings, trim, and exterior surfaces for cracks, peeling coatings, moisture damage, stains, and previous repairs. This inspection defines the preparation required and helps the painter plan materials, access, drying time, and the order of work.'],
      },
      {
        heading: 'Protecting floors, furniture, and fittings',
        paragraphs: ['Furniture is moved or covered, floors are protected, and fittings that should not be painted are removed or carefully masked. Good protection is not an afterthought—it allows the team to work efficiently while respecting the rest of the property.'],
      },
      {
        heading: 'Cleaning the surfaces',
        paragraphs: ['Dust, cooking residue, oils, mould, and exterior contaminants can prevent coatings from bonding correctly. Surfaces are cleaned using methods appropriate to the material and allowed to dry before further preparation begins.'],
      },
      {
        heading: 'Repairing cracks and imperfections',
        paragraphs: ['Nail holes, dents, cracks, loose material, and uneven previous repairs are filled or repaired, then sanded to create a stable, smooth surface. Older properties may need additional attention where coatings have failed or substrates have moved over time.'],
      },
      {
        heading: 'Sanding and priming',
        paragraphs: ['Sanding removes rough edges and improves adhesion. Primer is then selected where needed to seal porous areas, control stains, support adhesion, or create a consistent base for the finishing coats. The correct primer depends on the surface and its condition.'],
      },
      {
        heading: 'The final check before painting',
        paragraphs: ['Before applying finish coats, painters check masking, protection, repaired areas, surface dryness, and the smoothness of prepared walls and trim. Once this groundwork is complete, the finishing coats can be applied evenly and methodically.'],
      },
      {
        heading: 'Why preparation matters in Waikato homes',
        paragraphs: ['Preparation improves appearance, adhesion, and durability. Skipping it can leave visible defects and contribute to early peeling, cracking, bubbling, or uneven colour. A site assessment is the best way to determine what your property needs before repainting.'],
      },
    ],
  },
  {
    path: '/822-2/',
    title: 'A Guide to Compliance-Friendly Paint Options for Safe and Healthy Facilities',
    shortTitle: 'Compliance-Friendly Paint Options',
    topic: 'Materials',
    date: '24 August 2025',
    readTime: '7 min read',
    image: '/images/greenstone-kitchen.webp',
    imageAlt: 'Clean modern interior finished with durable painted surfaces',
    excerpt: 'Schools, healthcare spaces, hospitality venues, and other facilities need coatings selected for safety, hygiene, cleaning, and operational demands.',
    introduction: [
      'Paint selection in a public or regulated environment involves more than colour. The coating system should suit how the facility is used, how frequently it is cleaned, who occupies the space, and any standards that apply to the site.',
      'Requirements vary between projects. Product data, the substrate, the building specification, and current New Zealand requirements should all be reviewed before a final coating system is approved.',
    ],
    sections: [
      {
        heading: 'Why paint choice matters',
        paragraphs: ['Schools, healthcare settings, commercial kitchens, and high-traffic workplaces each place different demands on painted surfaces. The right specification supports indoor air quality, cleaning routines, durability, and the safe operation of the facility.'],
        bullets: ['Occupant sensitivity and ventilation', 'Cleaning frequency and sanitation methods', 'Moisture, mould, and abrasion exposure', 'Required product documentation and project specifications'],
      },
      {
        heading: 'Low-odour and low-VOC options',
        paragraphs: ['Low- or zero-VOC products can help reduce odour and emissions during application and curing. They may be particularly useful where a facility remains occupied, but the complete product specification and ventilation plan still need to be considered.'],
      },
      {
        heading: 'Washable, durable finishes',
        paragraphs: ['High-use spaces benefit from coatings designed to withstand repeated cleaning and contact. The appropriate sheen and resin system depend on the surface, traffic level, desired appearance, and cleaning products used by the facility.'],
      },
      {
        heading: 'Hygiene-focused coating systems',
        paragraphs: ['Some environments may call for mould-resistant, antimicrobial, moisture-resistant, or specialised protective coatings. These products should be chosen against the facility brief and manufacturer documentation rather than treated as a universal solution.'],
      },
      {
        heading: 'Selecting by facility type',
        bullets: ['Healthcare: low-odour, highly cleanable systems selected around infection-control requirements.', 'Education: durable, scuff-resistant coatings suited to occupied learning environments.', 'Hospitality and food areas: moisture- and grease-resistant systems appropriate to the exact application area.', 'Offices and retail: durable finishes that balance appearance, maintenance, and downtime.'],
      },
      {
        heading: 'A practical specification checklist',
        bullets: ['Confirm the substrate and existing coating condition.', 'Review product technical and safety data.', 'Check cleaning, hygiene, and durability requirements.', 'Plan ventilation, access, curing time, and operational downtime.', 'Confirm any regulatory or project-specific requirements with the responsible professional.'],
      },
      {
        heading: 'Get project-specific advice',
        paragraphs: ['This guide is general information, not a compliance determination. For a commercial or sensitive facility, the coating specification should be confirmed for the particular building, use, and current requirements before work starts.'],
      },
    ],
  },
  {
    path: '/wood-staining-benefits-you-need-to-take-advantage-of/',
    title: 'Wood Staining Benefits You Need to Take Advantage Of',
    shortTitle: 'The Benefits of Wood Staining',
    topic: 'Care guide',
    date: '14 August 2025',
    readTime: '5 min read',
    image: '/images/greenstone-exterior.webp',
    imageAlt: 'Finished residential exterior with protected timber details',
    excerpt: 'Stain can enhance timber grain while helping protect suitable wood surfaces from moisture, sunlight, and everyday exposure.',
    introduction: [
      'Timber brings warmth and natural variation to decks, fences, doors, joinery, and architectural features. A suitable stain can enrich that character while adding protection appropriate to the location and timber species.',
      'The best result depends on preparation, product compatibility, exposure, and the condition of the wood. A small test area is useful when colour and absorption need to be confirmed.',
    ],
    sections: [
      {
        heading: 'What wood staining does',
        paragraphs: ['Stain adds colour while allowing the natural grain to remain visible to varying degrees. Products range from clear and semi-transparent finishes to more solid colour systems, with different options for interior and exterior use.'],
      },
      {
        heading: 'A typical staining process',
        bullets: ['Assess the timber species, condition, moisture, and existing coating.', 'Clean the surface and remove failed finishes or contaminants.', 'Sand or prepare the timber as required for even absorption.', 'Apply the selected stain in controlled coats following product guidance.', 'Use a compatible protective finish where the coating system requires it.'],
      },
      {
        heading: 'Keeps the grain visible',
        paragraphs: ['Unlike an opaque paint system, many stains allow grain and texture to remain part of the finished appearance. This makes staining a useful option where the natural character of timber is a key design feature.'],
      },
      {
        heading: 'Supports weather protection',
        paragraphs: ['Exterior timber is exposed to ultraviolet light, rain, and changing moisture levels. A correctly selected and maintained exterior stain can help reduce weathering and moisture uptake, although it cannot repair timber that is already structurally unsound.'],
      },
      {
        heading: 'Can simplify future maintenance',
        paragraphs: ['Some penetrating stain systems weather gradually rather than forming a thick film. Depending on the product and condition, maintenance may be more straightforward than removing a heavily peeling coating. Regular inspection is still important.'],
      },
      {
        heading: 'Choosing the right finish',
        paragraphs: ['Timber species absorb stain differently, and shaded surfaces age differently from areas exposed to full sun and rain. Product type, colour, surface preparation, and maintenance expectations should be decided together for a consistent result.'],
      },
      {
        heading: 'Plan before you stain',
        paragraphs: ['Professional assessment helps identify whether timber should be stained, painted, repaired, or left untreated. Greenstone Painting can review the surface and recommend a practical preparation and coating approach for your project.'],
      },
    ],
  },
]

export const findBlogPost = (pathname: string) => blogPosts.find((post) => post.path === pathname)
