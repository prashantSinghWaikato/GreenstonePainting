import { useEffect, useState } from 'react'
import { ArrowRight, Building2, Check, Fence, Hammer, House, PaintBucket, Paintbrush } from 'lucide-react'
import { PublicFooter, PublicHeader } from './Blog'
import { getPublishedServices, type PublishedService } from './api/services'
import { services } from './data/site'
import './Services.css'

const serviceDetails = [
  {
    slug: 'interior-painting',
    label: 'Interior environments',
    title: 'Interior Painting',
    description: 'A controlled interior painting service for occupied homes, empty properties, renovations, and new spaces. We plan protection, preparation, product selection, and sequencing around the surfaces and how the property is used.',
    inclusions: ['Walls and ceilings', 'Doors, trim, and skirtings', 'Surface repairs and preparation', 'Colour and sheen guidance'],
    note: 'Suitable for single-room refreshes through to complete interior repaints.',
    image: '/images/greenstone-bedroom.webp',
    imageAlt: 'Freshly painted residential bedroom interior',
  },
  {
    slug: 'exterior-painting',
    label: 'Exterior protection',
    title: 'Exterior Painting',
    description: 'Exterior finishes need to suit the substrate, exposure, existing coating condition, and local environment. We assess the property before recommending preparation and a coating approach for the work.',
    inclusions: ['Weatherboards and cladding', 'Eaves, fascia, and exterior trim', 'Cleaning and surface preparation', 'Compatible exterior coating systems'],
    note: 'Designed for residential exteriors and selected commercial properties across Waikato.',
    image: '/images/greenstone-exterior.webp',
    imageAlt: 'Professionally finished modern house exterior',
  },
  {
    slug: 'commercial-painting',
    label: 'Commercial delivery',
    title: 'Commercial Painting',
    description: 'Clear scope, scheduling, and communication are essential in operational spaces. We coordinate painting work around access, other trades, business requirements, and the surfaces included in the agreed project.',
    inclusions: ['Offices and retail spaces', 'Property and facility refreshes', 'Interior and exterior surfaces', 'Planned staging and handover'],
    note: 'Project timing and product requirements are confirmed during assessment.',
    image: '/images/greenstone-kitchen.webp',
    imageAlt: 'Clean modern interior showing precise painted finishes',
  },
  {
    slug: 'roof-painting',
    label: 'Roof coating systems',
    title: 'Roof Painting',
    description: 'Roof painting begins with checking the roof material, condition, access, and whether repairs or specialist work are needed before coating. The final scope is based on a site inspection rather than appearance alone.',
    inclusions: ['Condition and access review', 'Cleaning and preparation', 'Coating compatibility checks', 'Planned application process'],
    note: 'Availability depends on roof condition, material, pitch, and safe access.',
    image: '/images/greenstone-before-after.jpg',
    imageAlt: 'Residential exterior painting transformation',
  },
  {
    slug: 'new-builds-renovations',
    label: 'Coordinated project work',
    title: 'New Builds & Renovations',
    description: 'Painting for a build or renovation needs to integrate with the wider programme. We clarify surfaces, finish levels, sequencing, access, and touch-up expectations so the painting package can move cleanly toward handover.',
    inclusions: ['New residential interiors', 'Renovation painting packages', 'Coordination with builders and trades', 'Final review and touch-ups'],
    note: 'Scope can be tailored to the build stage and agreed finish schedule.',
    image: '/images/greenstone-kitchen.webp',
    imageAlt: 'Finished kitchen and living area in a new residential build',
  },
  {
    slug: 'deck-fence-staining',
    label: 'Timber care',
    title: 'Deck & Fence Staining',
    description: 'Timber condition, species, previous finishes, and exposure affect how a stain will look and perform. We assess these factors before selecting preparation and a suitable transparent, semi-transparent, or solid finish.',
    inclusions: ['Decks and exterior timber', 'Fences and screening', 'Cleaning and preparation', 'Stain colour and finish selection'],
    note: 'A test area may be recommended because timber absorbs stain differently.',
    image: '/images/greenstone-exterior.webp',
    imageAlt: 'Residential exterior with finished timber elements',
  },
]

const fallbackPublishedServices: PublishedService[] = serviceDetails.map((detail, index) => ({
  ...detail,
  summary: services.find((service) => service.slug === detail.slug)?.description ?? detail.description,
  displayOrder: index + 1,
}))

const serviceIconBySlug: Record<string, typeof Paintbrush> = {
  'interior-painting': Paintbrush,
  'exterior-painting': House,
  'commercial-painting': Building2,
  'roof-painting': PaintBucket,
  'new-builds-renovations': Hammer,
  'deck-fence-staining': Fence,
}

export default function ServicesPage() {
  const [publishedServices, setPublishedServices] = useState<PublishedService[]>(fallbackPublishedServices)

  useEffect(() => {
    let active = true
    getPublishedServices().then((response) => { if (active) setPublishedServices(response) }).catch(() => undefined)
    return () => { active = false }
  }, [])

  return <div className="site-shell services-page-shell">
    <PublicHeader active="services" />
    <main id="main-content">
      <section className="services-page-hero">
        <div className="services-page-hero-media" aria-hidden="true"><img src="/images/greenstone-exterior.webp" alt="" /></div>
        <div className="services-page-hero-overlay" aria-hidden="true" />
        <div className="page-container services-page-hero-inner">
          <div><p className="eyebrow eyebrow-light">Painting services · Waikato</p><h1>Every surface needs<br /><span>the right system.</span></h1></div>
          <div className="services-hero-summary"><p>Residential and commercial painting planned around the property, substrate, programme, and finish you need.</p><a className="button button-primary" href="/#quote">Discuss Your Project <ArrowRight size={17} aria-hidden="true" /></a></div>
        </div>
        <div className="services-colour-line" aria-hidden="true"><span /><span /><span /><span /></div>
      </section>

      <section className="section service-overview" aria-labelledby="services-overview-heading">
        <div className="page-container">
          <div className="service-page-heading"><div><p className="eyebrow">Complete capability</p><h2 id="services-overview-heading">One accountable team.<br />Focused services.</h2></div><p>Choose the service closest to your project. We will confirm the exact preparation, surfaces, products, and programme after discussing the property.</p></div>
          <div className="service-overview-grid">
            {publishedServices.map((service) => {
              const Icon = serviceIconBySlug[service.slug] ?? Paintbrush
              return <a href={`#${service.slug}`} className="service-overview-card" key={service.slug}><span><Icon size={24} strokeWidth={1.7} aria-hidden="true" /></span><h3>{service.title}</h3><p>{service.summary}</p><strong>Explore service <ArrowRight size={15} aria-hidden="true" /></strong></a>
            })}
          </div>
        </div>
      </section>

      <section className="service-detail-list" aria-label="Painting service details">
        {publishedServices.map((service, index) => <article className={`service-detail ${index % 2 ? 'service-detail-reverse' : ''}`} id={service.slug} key={service.slug}>
          <div className="service-detail-image"><img src={service.image} alt={service.imageAlt} /><span>{service.label}</span></div>
          <div className="service-detail-copy"><p className="eyebrow">{service.label}</p><h2>{service.title}</h2><p>{service.description}</p><ul>{service.inclusions.map((item) => <li key={item}><Check size={16} strokeWidth={2} aria-hidden="true" />{item}</li>)}</ul><aside>{service.note}</aside><a href="/#quote">Request a quote for this service <ArrowRight size={16} aria-hidden="true" /></a></div>
        </article>)}
      </section>

      <section className="section service-process" aria-labelledby="service-process-heading"><div className="page-container"><div className="service-page-heading service-process-heading"><div><p className="eyebrow eyebrow-light">A clear working process</p><h2 id="service-process-heading">From assessment<br />to final review.</h2></div><p>Every project is different, but the operating rhythm stays disciplined.</p></div><div className="service-process-grid"><article><span>Assess</span><h3>Understand the property</h3><p>We review the surfaces, condition, access, priorities, and expected outcome.</p></article><article><span>Define</span><h3>Confirm the scope</h3><p>Preparation, inclusions, products, timing, and any exclusions are made clear.</p></article><article><span>Deliver</span><h3>Prepare and paint</h3><p>The team protects the work area and follows the agreed preparation and coating process.</p></article><article><span>Review</span><h3>Complete the handover</h3><p>We review the agreed work and address relevant finishing details before completion.</p></article></div></div></section>

      <section className="services-final-cta"><div className="page-container services-final-cta-inner"><div><p>Not sure which service fits?</p><h2>Show us the property.<br />We’ll help define the work.</h2></div><a className="button button-primary" href="/#quote">Start Your Quote <ArrowRight size={17} aria-hidden="true" /></a></div></section>
    </main>
    <PublicFooter />
  </div>
}
