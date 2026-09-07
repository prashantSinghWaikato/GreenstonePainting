import { ArrowRight, CheckCircle2, Mail, MapPin, Paintbrush, Phone, ShieldCheck, Users } from 'lucide-react'
import { PublicFooter, PublicHeader } from './Blog'
import { serviceAreas } from './data/site'
import './CompanyPages.css'

export function AboutPage() {
  return <div className="site-shell company-page-shell">
    <PublicHeader active="about" />
    <main id="main-content">
      <section className="company-hero about-hero">
        <div className="company-hero-media" aria-hidden="true"><img src="/images/greenstone-bedroom.webp" alt="" /></div><div className="company-hero-overlay" aria-hidden="true" />
        <div className="page-container company-hero-inner"><p className="eyebrow eyebrow-light">About Greenstone Painting</p><h1>Local accountability.<br /><span>Professional standards.</span></h1><p>Hamilton-based painters delivering carefully planned residential and commercial work throughout Waikato.</p></div>
      </section>
      <section className="section company-story"><div className="page-container company-story-grid"><div><p className="eyebrow">Our approach</p><h2>Good results begin with a clear way of working.</h2></div><div><p>Greenstone Painting approaches each property as a specific project—not a generic paint job. We review the surfaces, understand how the space is used, define the preparation, and coordinate the programme before work moves forward.</p><p>That discipline supports consistent communication, appropriate coating choices, careful protection, and a finish that reflects the agreed scope.</p></div></div></section>
      <section className="section company-values"><div className="page-container"><div className="company-value-grid"><article><Paintbrush aria-hidden="true" /><h2>Workmanship</h2><p>Preparation and application are matched to the actual surfaces and selected coating system.</p></article><article><CheckCircle2 aria-hidden="true" /><h2>Reliability</h2><p>Clear scheduling, direct communication, and respect for the property and wider programme.</p></article><article><ShieldCheck aria-hidden="true" /><h2>Professional standards</h2><p>Registered Master Painters membership and a quality-controlled approach to delivery.</p></article><article><Users aria-hidden="true" /><h2>Local team</h2><p>Hamilton-based service with practical knowledge of Waikato homes and commercial properties.</p></article></div></div></section>
      <section className="company-page-cta"><div className="page-container"><div><p>Plan with confidence</p><h2>Talk with the team about your property.</h2></div><a className="button button-primary" href="/#quote">Request Your Free Quote <ArrowRight aria-hidden="true" /></a></div></section>
    </main><PublicFooter />
  </div>
}

export function ServiceAreasPage() {
  return <div className="site-shell company-page-shell">
    <PublicHeader active="areas" />
    <main id="main-content">
      <section className="company-hero areas-hero"><div className="company-hero-media" aria-hidden="true"><img src="/images/greenstone-exterior.webp" alt="" /></div><div className="company-hero-overlay" aria-hidden="true" /><div className="page-container company-hero-inner"><p className="eyebrow eyebrow-light">Current service coverage</p><h1>Painting services<br /><span>across Waikato.</span></h1><p>Our Hamilton-based team currently delivers residential and commercial painting in these locations and surrounding communities.</p></div></section>
      <section className="section company-areas"><div className="page-container"><div className="company-area-heading"><div><p className="eyebrow">Where we work</p><h2>Current coverage.</h2></div><p>If your property sits near one of these locations, contact the team and we can confirm availability for the project.</p></div><div className="company-area-grid">{serviceAreas.map((area) => <article key={area}><MapPin aria-hidden="true" /><h2>{area}</h2></article>)}</div></div></section>
      <section className="company-location-band"><div className="page-container"><div><span>Greenstone Painting office</span><h2>29 Lachlan Drive<br />Dinsdale, Hamilton</h2><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer">Open in Google Maps <ArrowRight aria-hidden="true" /></a></div><iframe src="https://www.google.com/maps?q=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand&amp;output=embed" title="Greenstone Painting office at 29 Lachlan Drive, Dinsdale" loading="lazy" referrerPolicy="no-referrer-when-downgrade" allowFullScreen /></div></section>
    </main><PublicFooter />
  </div>
}

export function ContactPage() {
  return <div className="site-shell company-page-shell">
    <PublicHeader active="contact" />
    <main id="main-content">
      <section className="company-hero contact-hero"><div className="company-hero-media" aria-hidden="true"><img src="/images/greenstone-kitchen.webp" alt="" /></div><div className="company-hero-overlay" aria-hidden="true" /><div className="page-container company-hero-inner"><p className="eyebrow eyebrow-light">Contact Greenstone Painting</p><h1>Start with a<br /><span>clear conversation.</span></h1><p>Speak with our Hamilton team about your property, painting requirements, or the right next step.</p></div></section>
      <section className="section company-contact"><div className="page-container company-contact-grid"><div><p className="eyebrow">Contact information</p><h2>Choose the most convenient way to reach us.</h2><p>For a project price, request a quote and include the property details, service, and any useful photos.</p><a className="button button-primary" href="/#quote">Complete the Quote Form <ArrowRight aria-hidden="true" /></a></div><div className="company-contact-cards"><a href="tel:+642108383831"><Phone aria-hidden="true" /><span><small>Call our team</small><strong>021 083 83831</strong></span><ArrowRight aria-hidden="true" /></a><a href="mailto:info@greenstonepainting.co.nz"><Mail aria-hidden="true" /><span><small>Email</small><strong>info@greenstonepainting.co.nz</strong></span><ArrowRight aria-hidden="true" /></a><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer"><MapPin aria-hidden="true" /><span><small>Office address</small><strong>29 Lachlan Drive, Dinsdale, Hamilton</strong></span><ArrowRight aria-hidden="true" /></a></div></div></section>
      <section className="company-contact-map"><iframe src="https://www.google.com/maps?q=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand&amp;output=embed" title="Map showing Greenstone Painting in Dinsdale, Hamilton" loading="lazy" referrerPolicy="no-referrer-when-downgrade" allowFullScreen /></section>
    </main><PublicFooter />
  </div>
}

export function PrivacyPage() {
  return <div className="site-shell company-page-shell"><PublicHeader /><main id="main-content"><section className="privacy-page-hero"><div className="page-container"><p className="eyebrow eyebrow-light">Website privacy notice</p><h1>Your information stays purposeful.</h1><p>How Greenstone Painting Limited collects and uses information submitted through this website.</p></div></section><section className="section privacy-page-content"><div className="page-container"><article><h2>What we collect</h2><p>When you request a quote, we collect your contact details, property location, project description, service selection, and any project photos you choose to provide.</p></article><article><h2>How we use it</h2><p>We use this information to assess your project, contact you, arrange a site visit, prepare or follow up a quote, maintain relevant business records, and operate this service securely.</p></article><article><h2>Storage and sharing</h2><p>Access is limited to people and service providers who need the information to deliver or support the service. We do not sell personal information. Information may also be disclosed where required by New Zealand law.</p></article><article><h2>Retention and your rights</h2><p>We keep information only for as long as reasonably necessary for these purposes and applicable legal obligations. You may ask to access or correct your personal information by emailing <a href="mailto:info@greenstonepainting.co.nz">info@greenstonepainting.co.nz</a>.</p></article></div></section></main><PublicFooter /></div>
}
