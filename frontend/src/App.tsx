import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Building2, Clock, Fence, Hammer, House, Mail, MapPin, PaintBucket, Paintbrush, Palette, Phone } from 'lucide-react'
import { FaFacebookF, FaInstagram } from 'react-icons/fa'
import { articles, heroSlides, projects, serviceAreas, services } from './data/site'
import './App.css'

type QuoteFormState = {
  fullName: string
  email: string
  phone: string
  location: string
  service: string
  message: string
}

const initialQuoteForm: QuoteFormState = {
  fullName: '',
  email: '',
  phone: '',
  location: '',
  service: '',
  message: '',
}

const serviceIcons = [Paintbrush, House, Building2, PaintBucket, Hammer, Fence]

function App() {
  const [menuOpen, setMenuOpen] = useState(false)
  const [activeHero, setActiveHero] = useState(0)
  const [activeProject, setActiveProject] = useState(0)
  const [quoteForm, setQuoteForm] = useState(initialQuoteForm)
  const [uploadCount, setUploadCount] = useState(0)
  const [submitted, setSubmitted] = useState(false)

  useEffect(() => {
    const timer = window.setInterval(() => {
      setActiveHero((current) => (current + 1) % heroSlides.length)
    }, 6500)
    return () => window.clearInterval(timer)
  }, [])

  const updateField = <K extends keyof QuoteFormState>(field: K, value: QuoteFormState[K]) => {
    setQuoteForm((current) => ({ ...current, [field]: value }))
    setSubmitted(false)
  }

  const submitQuote = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitted(true)
  }

  const closeMenu = () => setMenuOpen(false)
  const project = projects[activeProject]

  return (
    <div className="site-shell">
      <a className="skip-link" href="#main-content">Skip to content</a>

      <div className="utility-bar">
        <div className="page-container utility-inner">
          <p><strong>Registered Master Painters member</strong></p>
          <div className="utility-contact">
            <span className="utility-item"><MapPin size={14} strokeWidth={2} aria-hidden="true" />Hamilton, Waikato</span>
            <a className="utility-item" href="mailto:info@greenstonepainting.co.nz"><Mail size={14} strokeWidth={2} aria-hidden="true" />info@greenstonepainting.co.nz</a>
            <a className="utility-item" href="tel:+642108383831"><Phone size={14} strokeWidth={2} aria-hidden="true" />021 083 83831</a>
          </div>
        </div>
      </div>

      <header className="site-header">
        <div className="page-container header-inner">
          <a className="brand brand-logo" href="#top" aria-label="Greenstone Painting Limited home">
            <img src="/images/greenstone-logo.png" alt="Greenstone Painting Limited" />
          </a>

          <button className="menu-toggle" type="button" aria-expanded={menuOpen} aria-controls="primary-navigation" onClick={() => setMenuOpen((open) => !open)}>
            <span>{menuOpen ? 'Close' : 'Menu'}</span><span className="menu-lines" aria-hidden="true" />
          </button>

          <nav id="primary-navigation" className={menuOpen ? 'primary-nav is-open' : 'primary-nav'} aria-label="Primary navigation">
            <a href="#top" onClick={closeMenu}>Home</a>
            <a href="#services" onClick={closeMenu}>Services</a>
            <a href="#projects" onClick={closeMenu}>Projects</a>
            <a href="#about" onClick={closeMenu}>About Us</a>
            <a href="#areas" onClick={closeMenu}>Service Areas</a>
            <a href="#insights" onClick={closeMenu}>Blog</a>
            <a href="#contact" onClick={closeMenu}>Contact</a>
          </nav>

        </div>
      </header>

      <main id="main-content">
        <section className="hero-section" id="top" aria-label="Greenstone Painting introduction">
          <div className="hero-media" aria-hidden="true">
            {heroSlides.map((slide, index) => <img className={index === activeHero ? 'is-active' : ''} src={slide.image} alt="" key={slide.image} />)}
          </div>
          <div className="hero-overlay" aria-hidden="true" />
          <div className="page-container hero-content">
            <div className="hero-copy">
              <p className="hero-kicker"><span /> Colour · Craft · Protection</p>
              <h1>Professional Painting Services <span className="hero-location">Across the Waikato</span></h1>
              <p className="hero-lead">Premium residential and commercial painting delivered through clear planning, disciplined preparation, and quality-controlled workmanship.</p>
              <div className="hero-actions-row">
                <a className="button button-primary shadow-xl shadow-greenstone-950/20 transition duration-300 hover:-translate-y-0.5" href="#quote">Get a Free Quote <span aria-hidden="true">→</span></a>
                <a className="button button-outline" href="#projects">View Our Projects</a>
              </div>
              <div className="hero-trust" aria-label="Company trust indicators">
                <p><span aria-hidden="true">✓</span><strong>Professional service</strong></p>
                <p><span aria-hidden="true">✓</span><strong>Quality workmanship</strong></p>
                <p><span aria-hidden="true">✓</span><strong>Insured local team</strong></p>
              </div>
            </div>
            <aside className="hero-paint-card" aria-label="Colour consultation available">
              <div className="paint-card-heading"><span><Palette size={20} strokeWidth={1.8} /></span><p>Colour consultation</p></div>
              <strong>A finish made for your space.</strong>
              <div className="paint-swatches" aria-hidden="true"><span /><span /><span /><span /><span /></div>
              <small><Paintbrush size={14} strokeWidth={1.8} /> Samples, sheen and coating advice</small>
            </aside>
            <div className="hero-controls" aria-label="Featured project slideshow">
              <div className="hero-control-buttons">
                {heroSlides.map((slide, index) => <button className={index === activeHero ? 'is-active' : ''} type="button" onClick={() => setActiveHero(index)} aria-label={`Show ${slide.label}`} key={slide.image} />)}
              </div>
              <p>{heroSlides[activeHero].label}</p>
            </div>
          </div>
        </section>

        <div className="paint-spectrum" aria-hidden="true"><span /><span /><span /><span /><span /><span /><span /></div>

        <section className="proof-bar" aria-label="Greenstone Painting capabilities">
          <div className="page-container proof-grid">
            <div><span>Residential &amp; commercial</span></div>
            <div><span>Detailed project scoping</span></div>
            <div><span>Premium coating systems</span></div>
            <div><span>Waikato-wide delivery</span></div>
          </div>
        </section>

        <section className="section services-section" id="services">
          <div className="page-container">
            <div className="section-heading compact-heading">
              <div><p className="eyebrow">Complete painting capability</p><h2>One team. Every surface.<br />One accountable result.</h2></div>
              <div><p>Structured painting services for homes, businesses, builders, and property teams throughout the Waikato.</p><a className="inline-link" href="#quote">Discuss your requirements →</a></div>
            </div>
            <div className="services-grid">
              {services.map((service, index) => {
                const ServiceIcon = serviceIcons[index]
                return (
                  <article className="service-card group rounded-card ring-1 ring-slate-950/5 transition duration-300 hover:-translate-y-1 hover:shadow-enterprise" key={service.slug}>
                    <div className="service-top"><span className="service-code transition duration-300 group-hover:scale-110 group-hover:bg-greenstone-500 group-hover:text-white" aria-hidden="true"><ServiceIcon size={21} strokeWidth={1.8} /></span></div>
                    <h3>{service.title}</h3><p>{service.description}</p>
                    <a href="#quote" aria-label={`Learn more about ${service.title}`}>Learn More <span aria-hidden="true">↗</span></a>
                  </article>
                )
              })}
            </div>
          </div>
        </section>

        <section className="section why-section" id="about">
          <div className="page-container">
            <div className="section-heading why-heading"><div><p className="eyebrow eyebrow-light">Why Greenstone</p><h2>Enterprise discipline.<br />Local accountability.</h2></div><p>Every project follows a clear operating standard—from the first site visit to final quality review.</p></div>
            <div className="why-grid">
              <article className="group transition duration-300 hover:-translate-y-1"><h3>Professional Workmanship</h3><p>Experienced painters, careful preparation, and controlled application at every stage.</p></article>
              <article className="group transition duration-300 hover:-translate-y-1"><h3>Reliable &amp; On-Time</h3><p>Clear scheduling, proactive communication, and respect for your property and programme.</p></article>
              <article className="group transition duration-300 hover:-translate-y-1"><h3>Quality Materials</h3><p>Proven coating systems selected for the surface, environment, and expected performance.</p></article>
              <article className="group transition duration-300 hover:-translate-y-1"><h3>Local Waikato Team</h3><p>Hamilton-based service with practical local knowledge and direct accountability.</p></article>
            </div>
          </div>
        </section>

        <section className="section projects-section" id="projects">
          <div className="page-container">
            <div className="section-heading project-heading"><div><p className="eyebrow">Featured projects</p><h2>Work that performs<br />as well as it looks.</h2></div><p>Explore selected residential painting outcomes from Hamilton and across the Waikato region.</p></div>
            <div className="project-showcase rounded-card shadow-enterprise ring-1 ring-slate-950/5">
              <div className="project-stage">
                <img src={project.image} alt={project.alt} />
                <div className="project-badge">{project.category}</div>
                <div className="project-stage-copy"><span>Featured case study</span><h3>{project.title}</h3><p>{project.location}</p></div>
              </div>
              <div className="project-panel">
                <h3>{project.title}</h3><p>{project.summary}</p>
                <dl><div><dt>Location</dt><dd>{project.location}</dd></div><div><dt>Scope</dt><dd>{project.category}</dd></div></dl>
                <a className="button button-dark" href="#quote">Plan a Similar Project →</a>
                <div className="project-selector">
                  {projects.map((item, index) => <button className={index === activeProject ? 'is-active' : ''} type="button" onClick={() => setActiveProject(index)} aria-label={`Show ${item.title}`} key={item.title}><img src={item.image} alt="" /></button>)}
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="section quote-section" id="quote">
          <div className="page-container quote-layout">
            <div className="quote-intro"><p className="eyebrow eyebrow-light">Online project request</p><h2>Start with a clearer quote.</h2><p>Tell us about your property, upload reference photos, and we will arrange the right next step.</p><div className="quote-assurance"><span>What happens next</span><ol><li>We review your project details</li><li>Our team contacts you</li><li>We arrange a site visit if required</li></ol></div></div>
            <form className="quote-form rounded-card shadow-enterprise ring-1 ring-white/10" onSubmit={submitQuote}>
              <div className="form-heading"><div><span>Request form</span><h3>Project details</h3></div><p>Fields marked * are required</p></div>
              <div className="form-row"><label>Full name *<input required autoComplete="name" value={quoteForm.fullName} onChange={(event) => updateField('fullName', event.target.value)} /></label><label>Email *<input required type="email" autoComplete="email" value={quoteForm.email} onChange={(event) => updateField('email', event.target.value)} /></label></div>
              <div className="form-row"><label>Phone *<input required type="tel" autoComplete="tel" value={quoteForm.phone} onChange={(event) => updateField('phone', event.target.value)} /></label><label>Property location *<input required autoComplete="street-address" value={quoteForm.location} onChange={(event) => updateField('location', event.target.value)} /></label></div>
              <label>Service required *<select required value={quoteForm.service} onChange={(event) => updateField('service', event.target.value)}><option value="">Select a service</option>{services.map((service) => <option value={service.slug} key={service.slug}>{service.title}</option>)}</select></label>
              <label>Project description *<textarea required rows={4} placeholder="Property type, surfaces to paint, preferred timing, and anything else we should know." value={quoteForm.message} onChange={(event) => updateField('message', event.target.value)} /></label>
              <label className="upload-field"><input type="file" accept="image/jpeg,image/png,image/webp" multiple onChange={(event) => setUploadCount(event.target.files?.length ?? 0)} /><span className="upload-icon" aria-hidden="true">＋</span><strong>{uploadCount ? `${uploadCount} photo${uploadCount > 1 ? 's' : ''} selected` : 'Upload project photos'}</strong><small>JPG, PNG or WebP · Add multiple files</small></label>
              <div className="form-submit"><button className="button button-primary" type="submit">Submit Quote Request →</button><p>Your details will only be used to respond to this request.</p></div>
              {submitted && <div className="form-success" role="status"><strong>Thanks, {quoteForm.fullName}.</strong><span>Your form is ready for backend connection in the next phase.</span></div>}
            </form>
          </div>
        </section>

        <section className="section reviews-section" id="reviews">
          <div className="page-container"><div className="section-heading review-heading"><div><p className="eyebrow">Customer confidence</p><h2>Feedback that reflects<br />the finished work.</h2></div><div className="rating-summary"><strong>5.0</strong><span>★★★★★</span><small>Current verified Bark review</small></div></div>
            <div className="reviews-grid">
              <article className="review-card featured-review rounded-card ring-1 ring-slate-950/5 transition duration-300 hover:-translate-y-1 hover:shadow-enterprise"><div className="stars" aria-label="5 out of 5 stars">★★★★★</div><blockquote>“Great service. Good price. Great job.”</blockquote><footer><strong>Clare</strong><span>Verified Bark customer · Interior painting</span></footer></article>
              <article className="review-card review-placeholder rounded-card ring-1 ring-slate-950/5 transition duration-300 hover:-translate-y-1 hover:shadow-enterprise"><span className="review-mark">G</span><h3>Google Reviews</h3><p>Verified Google review content will be connected here before production launch.</p><span className="integration-label">Live integration planned</span></article>
              <article className="review-card review-placeholder rounded-card ring-1 ring-slate-950/5 transition duration-300 hover:-translate-y-1 hover:shadow-enterprise"><span className="review-mark">G</span><h3>Customer feedback</h3><p>The final interface will surface current review ratings and recent customer experiences.</p><span className="integration-label">Dynamic content</span></article>
            </div>
          </div>
        </section>

        <section className="section areas-section" id="areas">
          <div className="page-container">
            <div className="section-heading coverage-heading"><div><p className="eyebrow">Service coverage</p><h2>Current coverage across the Waikato.</h2></div><p>Our team currently delivers residential and commercial painting services throughout these locations and surrounding communities.</p></div>
            <div className="coverage-grid">{serviceAreas.map((area) => <div className="coverage-item" key={area}><strong>{area}</strong></div>)}</div>
          </div>
        </section>

        <section className="section contact-location-section" id="contact">
          <div className="page-container contact-location-layout">
            <div className="contact-info-panel"><p className="eyebrow eyebrow-light">Contact info</p><h2>Visit or speak with our Hamilton team.</h2><p>Contact Greenstone Painting to discuss your property, arrange a site visit, or request a project quote.</p><div className="contact-details"><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer"><MapPin size={20} strokeWidth={1.8} aria-hidden="true" /><span><small>Office address</small><strong>29 Lachlan Drive<br />Dinsdale, Hamilton</strong></span></a><a href="tel:+642108383831"><Phone size={20} strokeWidth={1.8} aria-hidden="true" /><span><small>Phone</small><strong>021 083 83831</strong></span></a><a href="mailto:info@greenstonepainting.co.nz"><Mail size={20} strokeWidth={1.8} aria-hidden="true" /><span><small>Email</small><strong>info@greenstonepainting.co.nz</strong></span></a><div><Clock size={20} strokeWidth={1.8} aria-hidden="true" /><span><small>Business hours</small><strong>8:00am–5:00pm</strong></span></div></div></div>
            <div className="map-card google-map-card rounded-card shadow-enterprise ring-1 ring-white/10"><iframe src="https://www.google.com/maps?q=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand&amp;output=embed" title="Google Map showing Greenstone Painting at 29 Lachlan Drive, Dinsdale" loading="lazy" referrerPolicy="no-referrer-when-downgrade" allowFullScreen /><div className="map-footer"><span>Greenstone Painting office</span><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer">Open in Google Maps ↗</a></div></div>
          </div>
        </section>

        <section className="section insights-section" id="insights">
          <div className="page-container"><div className="section-heading insights-heading"><div><p className="eyebrow">Advice &amp; insights</p><h2>Better decisions before<br />the first coat.</h2></div><a className="inline-link" href="https://greenstonepainting.co.nz/blog/">View all articles →</a></div><div className="articles-grid">{articles.map((article) => <article key={article.title}><span>{article.topic}</span><h3>{article.title}</h3><div><time>{article.date}</time><a href="https://greenstonepainting.co.nz/blog/" aria-label={`Read ${article.title}`}>Read article ↗</a></div></article>)}</div></div>
        </section>

        <section className="final-cta"><div className="final-cta-shade" aria-hidden="true" /><div className="page-container final-cta-inner"><div><span>Ready when you are</span><h2>Planning Your Next<br />Painting Project?</h2></div><a className="button button-primary shadow-xl shadow-greenstone-950/25 transition duration-300 hover:-translate-y-0.5" href="#quote">Request Your Free Quote →</a></div></section>
      </main>

      <footer className="site-footer" id="footer">
        <div className="page-container footer-main">
          <div className="footer-brand"><a className="brand brand-logo brand-logo-footer" href="#top" aria-label="Greenstone Painting Limited home"><img src="/images/greenstone-logo.png" alt="Greenstone Painting Limited" /></a><p>Professional residential and commercial painting throughout the Waikato region.</p><a className="footer-call" href="tel:+642108383831">021 083 83831</a></div>
          <div className="footer-column"><h2>Services</h2>{services.slice(0, 5).map((service) => <a href="#services" key={service.slug}>{service.title}</a>)}</div>
          <div className="footer-column"><h2>Company</h2><a href="#about">About Us</a><a href="#projects">Projects</a><a href="#areas">Service Areas</a><a href="#insights">Blog</a><a href="#quote">Get a Quote</a></div>
          <div className="footer-column"><h2>Contact</h2><a href="mailto:info@greenstonepainting.co.nz">info@greenstonepainting.co.nz</a><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer">29 Lachlan Drive<br />Dinsdale, Hamilton</a><span>8:00am–5:00pm</span><div className="social-row"><a className="social-instagram" href="https://www.instagram.com/greenstonepainting.nz/" target="_blank" rel="noreferrer" aria-label="Follow Greenstone Painting on Instagram"><FaInstagram size={16} aria-hidden="true" /></a><a className="social-facebook" href="https://www.facebook.com/greenstonepainting/" target="_blank" rel="noreferrer" aria-label="Visit Greenstone Painting on Facebook"><FaFacebookF size={15} aria-hidden="true" /></a></div></div>
        </div>
        <div className="page-container footer-bottom"><span>© {new Date().getFullYear()} Greenstone Painting Limited</span><span>Privacy · Terms · Website accessibility</span></div>
      </footer>
    </div>
  )
}

export default App
