import { useCallback, useEffect, useRef, useState } from 'react'
import type { ChangeEvent, DragEvent, FormEvent } from 'react'
import { AlertCircle, Building2, Check, Fence, Hammer, House, ImagePlus, Mail, MapPin, PaintBucket, Paintbrush, Palette, Phone, RotateCcw, X } from 'lucide-react'
import { FaFacebookF, FaInstagram } from 'react-icons/fa'
import { completeEnquiry, createEnquiry, EnquiryApiError, uploadEnquiryPhoto } from './api/enquiries'
import { articles, heroSlides, projects, serviceAreas, services } from './data/site'
import ColourStudio from './ColourStudio'
import './App.css'

type QuoteFormState = {
  firstName: string
  lastName: string
  email: string
  phone: string
  location: string
  service: string
  message: string
}

const initialQuoteForm: QuoteFormState = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  location: '',
  service: '',
  message: '',
}

const serviceIcons = [Paintbrush, House, Building2, PaintBucket, Hammer, Fence]
const maximumPhotoCount = 4
const maximumPhotoSize = 5 * 1024 * 1024
const acceptedPhotoTypes = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/heic', 'image/heif'])

type SelectedPhoto = {
  id: string
  file: File
  previewUrl: string
  status: 'ready' | 'uploading' | 'uploaded' | 'failed'
  error?: string
}

type UploadContext = {
  enquiryId: string
  uploadToken: string
}

type QuoteFormErrors = Partial<Record<keyof QuoteFormState, string>>

const fieldLabels: Record<keyof QuoteFormState, string> = {
  firstName: 'First name',
  lastName: 'Last name',
  email: 'Email',
  phone: 'Phone',
  location: 'Property location',
  service: 'Service required',
  message: 'Project description',
}

const fieldIds: Record<keyof QuoteFormState, string> = {
  firstName: 'quote-first-name',
  lastName: 'quote-last-name',
  email: 'quote-email',
  phone: 'quote-phone',
  location: 'quote-location',
  service: 'quote-service',
  message: 'quote-message',
}

function validateQuoteField(field: keyof QuoteFormState, value: string): string {
  const trimmed = value.trim()
  if (!trimmed) {
    const requiredMessages: Record<keyof QuoteFormState, string> = {
      firstName: 'Enter your first name.',
      lastName: 'Enter your last name.',
      email: 'Enter your email address.',
      phone: 'Enter your phone number.',
      location: 'Enter the property location.',
      service: 'Select the painting service you need.',
      message: 'Tell us about your painting project.',
    }
    return requiredMessages[field]
  }
  if (field === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
    return 'Enter a valid email address, for example name@example.co.nz.'
  }
  if (field === 'phone') {
    const digitCount = trimmed.replace(/\D/g, '').length
    if (!/^[+\d][\d\s().-]*$/.test(trimmed) || digitCount < 7 || digitCount > 15) {
      return 'Enter a valid phone number using 7 to 15 digits.'
    }
  }
  return ''
}

function validateQuoteForm(form: QuoteFormState): QuoteFormErrors {
  return (Object.keys(form) as Array<keyof QuoteFormState>).reduce<QuoteFormErrors>((errors, field) => {
    const error = validateQuoteField(field, form[field])
    if (error) errors[field] = error
    return errors
  }, {})
}

function App() {
  const [menuOpen, setMenuOpen] = useState(false)
  const [activeHero, setActiveHero] = useState(0)
  const [activeProject, setActiveProject] = useState(0)
  const [quoteForm, setQuoteForm] = useState(initialQuoteForm)
  const [submitting, setSubmitting] = useState(false)
  const [submissionReference, setSubmissionReference] = useState('')
  const [submissionError, setSubmissionError] = useState('')
  const [photos, setPhotos] = useState<SelectedPhoto[]>([])
  const [photoError, setPhotoError] = useState('')
  const [completionError, setCompletionError] = useState('')
  const [notificationSent, setNotificationSent] = useState(false)
  const [uploadContext, setUploadContext] = useState<UploadContext | null>(null)
  const [fieldErrors, setFieldErrors] = useState<QuoteFormErrors>({})
  const [privacyAccepted, setPrivacyAccepted] = useState(false)
  const [privacyError, setPrivacyError] = useState('')
  const [colourStudioOpen, setColourStudioOpen] = useState(false)
  const photosRef = useRef<SelectedPhoto[]>([])
  const quoteFormRef = useRef<HTMLFormElement>(null)

  useEffect(() => {
    const timer = window.setInterval(() => {
      setActiveHero((current) => (current + 1) % heroSlides.length)
    }, 6500)
    return () => window.clearInterval(timer)
  }, [])

  useEffect(() => {
    photosRef.current = photos
  }, [photos])

  useEffect(() => {
    const showQuoteForm = (serviceSlug?: string) => {
      if (serviceSlug && services.some((service) => service.slug === serviceSlug)) {
        setQuoteForm((current) => ({ ...current, service: serviceSlug }))
        setFieldErrors((current) => {
          if (!current.service) return current
          const next = { ...current }
          delete next.service
          return next
        })
      }
      window.requestAnimationFrame(() => document.getElementById('quote')?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
    }

    const handleAssistantQuote = (event: Event) => showQuoteForm((event as CustomEvent<{ serviceSlug?: string }>).detail?.serviceSlug)
    window.addEventListener('greenstone:open-quote', handleAssistantQuote)

    const serviceSlug = new URLSearchParams(window.location.search).get('service') ?? undefined
    if (window.location.hash === '#quote') showQuoteForm(serviceSlug)

    return () => window.removeEventListener('greenstone:open-quote', handleAssistantQuote)
  }, [])

  useEffect(() => () => photosRef.current.forEach((photo) => URL.revokeObjectURL(photo.previewUrl)), [])

  const updateField = <K extends keyof QuoteFormState>(field: K, value: QuoteFormState[K]) => {
    setQuoteForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => {
      if (!current[field]) return current
      const next = { ...current }
      delete next[field]
      return next
    })
    setSubmissionReference('')
    setSubmissionError('')
  }

  const validateFieldOnBlur = (field: keyof QuoteFormState) => {
    const error = validateQuoteField(field, quoteForm[field])
    setFieldErrors((current) => {
      const next = { ...current }
      if (error) next[field] = error
      else delete next[field]
      return next
    })
  }

  const focusFirstInvalidField = () => {
    window.requestAnimationFrame(() => {
      quoteFormRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    })
  }

  const selectPhotos = (fileList: FileList | File[]) => {
    if (uploadContext || submitting) return

    const availableSlots = maximumPhotoCount - photos.length
    const candidates = Array.from(fileList)
    const accepted: SelectedPhoto[] = []
    let validationMessage = ''

    for (const file of candidates.slice(0, availableSlots)) {
      const extension = file.name.split('.').pop()?.toLowerCase()
      const supported = acceptedPhotoTypes.has(file.type) || (!file.type && (extension === 'heic' || extension === 'heif'))
      if (!supported) {
        validationMessage = 'Use JPEG, PNG, WebP, HEIC, or HEIF photos.'
        continue
      }
      if (file.size > maximumPhotoSize) {
        validationMessage = 'Each photo must be 5 MB or smaller.'
        continue
      }
      accepted.push({ id: crypto.randomUUID(), file, previewUrl: URL.createObjectURL(file), status: 'ready' })
    }

    if (candidates.length > availableSlots) validationMessage = 'You can attach up to 4 photos.'
    setPhotos((current) => [...current, ...accepted])
    setPhotoError(validationMessage)
  }

  const handlePhotoInput = (event: ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) selectPhotos(event.target.files)
    event.target.value = ''
  }

  const handlePhotoDrop = (event: DragEvent<HTMLLabelElement>) => {
    event.preventDefault()
    selectPhotos(event.dataTransfer.files)
  }

  const removePhoto = (photoId: string) => {
    const removed = photos.find((photo) => photo.id === photoId)
    if (removed) URL.revokeObjectURL(removed.previewUrl)
    const remaining = photos.filter((photo) => photo.id !== photoId)
    setPhotos(remaining)
    setPhotoError('')
    if (uploadContext && remaining.length === 0 && notificationSent) setUploadContext(null)
  }

  const uploadPhotos = async (context: UploadContext, selectedPhotos: SelectedPhoto[]) => {
    const results = await Promise.all(selectedPhotos.map(async (photo) => {
      setPhotos((current) => current.map((item) => item.id === photo.id ? { ...item, status: 'uploading', error: undefined } : item))
      try {
        await uploadEnquiryPhoto(context.enquiryId, context.uploadToken, photo.file)
        setPhotos((current) => current.map((item) => item.id === photo.id ? { ...item, status: 'uploaded', error: undefined } : item))
        return { id: photo.id, uploaded: true }
      } catch (error) {
        const message = error instanceof Error ? error.message : `We could not upload ${photo.file.name}.`
        setPhotos((current) => current.map((item) => item.id === photo.id ? { ...item, status: 'failed', error: message } : item))
        return { id: photo.id, uploaded: false }
      }
    }))
    return results
  }

  const finishPhotoUploads = (results: { id: string; uploaded: boolean }[]) => {
    const uploadedIds = new Set(results.filter((result) => result.uploaded).map((result) => result.id))
    photos.filter((photo) => uploadedIds.has(photo.id)).forEach((photo) => URL.revokeObjectURL(photo.previewUrl))
    const failedPhotos = photos.filter((photo) => !uploadedIds.has(photo.id))
    setPhotos(failedPhotos)
    if (failedPhotos.length === 0) {
      setPhotoError('')
    } else {
      setPhotoError(`Your quote was saved, but ${failedPhotos.length} photo${failedPhotos.length === 1 ? '' : 's'} could not be uploaded. Please retry.`)
    }
    return failedPhotos
  }

  const completeRequest = async (context: UploadContext, keepUploadAccess: boolean) => {
    try {
      await completeEnquiry(context.enquiryId, context.uploadToken)
      setNotificationSent(true)
      setCompletionError('')
      if (!keepUploadAccess) setUploadContext(null)
    } catch (error) {
      setNotificationSent(false)
      setUploadContext(context)
      setCompletionError(error instanceof Error ? error.message : 'Your request was saved, but final confirmation is temporarily unavailable.')
    }
  }

  const submitQuote = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const validationErrors = validateQuoteForm(quoteForm)
    const privacyValidationError = privacyAccepted ? '' : 'Confirm that you have read the Privacy Notice.'
    if (Object.keys(validationErrors).length > 0 || privacyValidationError) {
      setFieldErrors(validationErrors)
      setPrivacyError(privacyValidationError)
      setSubmissionError('')
      setSubmissionReference('')
      if (Object.keys(validationErrors).length > 0) focusFirstInvalidField()
      else window.requestAnimationFrame(() => document.getElementById('quote-privacy')?.focus())
      return
    }

    setSubmitting(true)
    setSubmissionError('')
    setSubmissionReference('')
    setCompletionError('')
    setNotificationSent(false)

    try {
      const response = await createEnquiry({
        firstName: quoteForm.firstName,
        lastName: quoteForm.lastName,
        email: quoteForm.email,
        phone: quoteForm.phone,
        serviceSlug: quoteForm.service,
        propertyAddress: quoteForm.location,
        message: quoteForm.message,
      })
      const context = { enquiryId: response.id, uploadToken: response.uploadToken }
      setSubmissionReference(response.id.slice(0, 8).toUpperCase())
      setQuoteForm(initialQuoteForm)
      setPrivacyAccepted(false)
      setPrivacyError('')
      setFieldErrors({})
      setUploadContext(context)
      let failedPhotos: SelectedPhoto[] = []
      if (photos.length > 0) {
        const results = await uploadPhotos(context, photos)
        failedPhotos = finishPhotoUploads(results)
      }
      await completeRequest(context, failedPhotos.length > 0)
    } catch (error) {
      if (error instanceof EnquiryApiError && Object.keys(error.fieldErrors).length > 0) {
        const backendFieldMap: Record<string, keyof QuoteFormState> = {
          firstName: 'firstName', lastName: 'lastName', email: 'email', phone: 'phone',
          propertyAddress: 'location', serviceSlug: 'service', message: 'message',
        }
        const backendErrors = Object.entries(error.fieldErrors).reduce<QuoteFormErrors>((errors, [field, message]) => {
          const mappedField = backendFieldMap[field]
          if (mappedField) errors[mappedField] = message
          return errors
        }, {})
        setFieldErrors(backendErrors)
        setSubmissionError('')
        focusFirstInvalidField()
      } else {
        setSubmissionError(error instanceof Error ? error.message : 'We could not submit your request. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const retryPhotoUploads = async () => {
    if (!uploadContext || photos.length === 0) return
    setSubmitting(true)
    setPhotoError('')
    const results = await uploadPhotos(uploadContext, photos)
    const failedPhotos = finishPhotoUploads(results)
    if (failedPhotos.length === 0 && notificationSent) setUploadContext(null)
    setSubmitting(false)
  }

  const retryCompletion = async () => {
    if (!uploadContext) return
    setSubmitting(true)
    await completeRequest(uploadContext, photos.length > 0)
    setSubmitting(false)
  }

  const startAnotherQuote = () => {
    setSubmissionReference('')
    setSubmissionError('')
    setCompletionError('')
    setNotificationSent(false)
    setUploadContext(null)
    setPhotoError('')
    setFieldErrors({})
    setQuoteForm(initialQuoteForm)
    setPrivacyAccepted(false)
    setPrivacyError('')
  }

  const closeMenu = () => setMenuOpen(false)
  const closeColourStudio = useCallback(() => setColourStudioOpen(false), [])
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
            <a href="/services/" onClick={closeMenu}>Services</a>
            <a href="/projects/" onClick={closeMenu}>Projects</a>
            <a href="#about" onClick={closeMenu}>About Us</a>
            <a href="#areas" onClick={closeMenu}>Service Areas</a>
            <a href="/blog/" onClick={closeMenu}>Blog</a>
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
                <a className="button button-outline" href="/projects/">View Our Projects</a>
              </div>
              <div className="hero-trust" aria-label="Company trust indicators">
                <p><span aria-hidden="true">✓</span><strong>Professional service</strong></p>
                <p><span aria-hidden="true">✓</span><strong>Quality workmanship</strong></p>
                <p><span aria-hidden="true">✓</span><strong>Local Waikato team</strong></p>
              </div>
            </div>
            <button className="hero-paint-card" type="button" onClick={() => setColourStudioOpen(true)} aria-haspopup="dialog">
              <div className="paint-card-heading"><span><Palette size={20} strokeWidth={1.8} /></span><p>Colour consultation</p></div>
              <strong>A finish made for your space.</strong>
              <div className="paint-swatches" aria-hidden="true"><span /><span /><span /><span /><span /></div>
              <small><Paintbrush size={14} strokeWidth={1.8} /> Open the interactive colour preview</small>
            </button>
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
                    <a href={`/services/#${service.slug}`} aria-label={`Learn more about ${service.title}`}>Learn More <span aria-hidden="true">↗</span></a>
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
            <div className="section-heading project-heading"><div><p className="eyebrow">Featured projects</p><h2>Work that performs<br />as well as it looks.</h2></div><p>Explore selected residential painting outcomes from Hamilton and across Waikato.</p></div>
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
            <div className="quote-intro"><p className="eyebrow eyebrow-light">Online project request</p><h2>Start with a clearer quote.</h2><p>Tell us about your property and we will arrange the right next step.</p><div className="quote-assurance"><span>What happens next</span><ol><li>We review your project details</li><li>Our team contacts you</li><li>We arrange a site visit if required</li></ol></div></div>
            <form className="quote-form rounded-card shadow-enterprise ring-1 ring-white/10" ref={quoteFormRef} onSubmit={submitQuote} noValidate>
              <div className="form-heading"><div><span>Request form</span><h3>{submissionReference ? 'Request received' : 'Project details'}</h3></div><p>{submissionReference ? `Reference ${submissionReference}` : 'Fields marked * are required'}</p></div>
              {!submissionReference && <>
              {Object.keys(fieldErrors).length > 0 && <div className="form-validation-summary" role="alert">
                <AlertCircle size={19} aria-hidden="true" />
                <div><strong>Please check {Object.keys(fieldErrors).length === 1 ? 'this field' : 'the highlighted fields'}.</strong><ul>{(Object.entries(fieldErrors) as Array<[keyof QuoteFormState, string]>).map(([field, message]) => <li key={field}><a href={`#${fieldIds[field]}`}>{fieldLabels[field]}: {message}</a></li>)}</ul></div>
              </div>}
              <div className="form-row">
                <label htmlFor={fieldIds.firstName}>First name *<input id={fieldIds.firstName} required maxLength={100} autoComplete="given-name" value={quoteForm.firstName} aria-invalid={Boolean(fieldErrors.firstName)} aria-describedby={fieldErrors.firstName ? 'quote-first-name-error' : undefined} onBlur={() => validateFieldOnBlur('firstName')} onChange={(event) => updateField('firstName', event.target.value)} />{fieldErrors.firstName && <span className="field-error" id="quote-first-name-error"><AlertCircle size={13} aria-hidden="true" />{fieldErrors.firstName}</span>}</label>
                <label htmlFor={fieldIds.lastName}>Last name *<input id={fieldIds.lastName} required maxLength={100} autoComplete="family-name" value={quoteForm.lastName} aria-invalid={Boolean(fieldErrors.lastName)} aria-describedby={fieldErrors.lastName ? 'quote-last-name-error' : undefined} onBlur={() => validateFieldOnBlur('lastName')} onChange={(event) => updateField('lastName', event.target.value)} />{fieldErrors.lastName && <span className="field-error" id="quote-last-name-error"><AlertCircle size={13} aria-hidden="true" />{fieldErrors.lastName}</span>}</label>
              </div>
              <div className="form-row">
                <label htmlFor={fieldIds.email}>Email *<input id={fieldIds.email} required maxLength={254} type="email" autoComplete="email" value={quoteForm.email} aria-invalid={Boolean(fieldErrors.email)} aria-describedby={fieldErrors.email ? 'quote-email-error' : undefined} onBlur={() => validateFieldOnBlur('email')} onChange={(event) => updateField('email', event.target.value)} />{fieldErrors.email && <span className="field-error" id="quote-email-error"><AlertCircle size={13} aria-hidden="true" />{fieldErrors.email}</span>}</label>
                <label htmlFor={fieldIds.phone}>Phone *<input id={fieldIds.phone} required maxLength={40} type="tel" inputMode="tel" autoComplete="tel" placeholder="e.g. 021 083 83831" value={quoteForm.phone} aria-invalid={Boolean(fieldErrors.phone)} aria-describedby={fieldErrors.phone ? 'quote-phone-error' : undefined} onBlur={() => validateFieldOnBlur('phone')} onChange={(event) => updateField('phone', event.target.value)} />{fieldErrors.phone && <span className="field-error" id="quote-phone-error"><AlertCircle size={13} aria-hidden="true" />{fieldErrors.phone}</span>}</label>
              </div>
              <label htmlFor={fieldIds.location}>Property location *<input id={fieldIds.location} required maxLength={300} autoComplete="street-address" value={quoteForm.location} aria-invalid={Boolean(fieldErrors.location)} aria-describedby={fieldErrors.location ? 'quote-location-error' : undefined} onBlur={() => validateFieldOnBlur('location')} onChange={(event) => updateField('location', event.target.value)} />{fieldErrors.location && <span className="field-error" id="quote-location-error"><AlertCircle size={13} aria-hidden="true" />{fieldErrors.location}</span>}</label>
              <label htmlFor={fieldIds.service}>Service required *<select id={fieldIds.service} required value={quoteForm.service} aria-invalid={Boolean(fieldErrors.service)} aria-describedby={fieldErrors.service ? 'quote-service-error' : undefined} onBlur={() => validateFieldOnBlur('service')} onChange={(event) => updateField('service', event.target.value)}><option value="">Select a service</option>{services.map((service) => <option value={service.slug} key={service.slug}>{service.title}</option>)}</select>{fieldErrors.service && <span className="field-error" id="quote-service-error"><AlertCircle size={13} aria-hidden="true" />{fieldErrors.service}</span>}</label>
              <label htmlFor={fieldIds.message}>Project description *<textarea id={fieldIds.message} required maxLength={10000} rows={4} placeholder="Property type, surfaces to paint, preferred timing, and anything else we should know." value={quoteForm.message} aria-invalid={Boolean(fieldErrors.message)} aria-describedby={fieldErrors.message ? 'quote-message-error' : undefined} onBlur={() => validateFieldOnBlur('message')} onChange={(event) => updateField('message', event.target.value)} />{fieldErrors.message && <span className="field-error" id="quote-message-error"><AlertCircle size={13} aria-hidden="true" />{fieldErrors.message}</span>}</label>
              {!uploadContext && <label className={`upload-field ${submitting || photos.length >= maximumPhotoCount ? 'upload-field-disabled' : ''}`} onDragOver={(event) => event.preventDefault()} onDrop={handlePhotoDrop}>
                <input type="file" accept="image/jpeg,image/png,image/webp,image/heic,image/heif,.heic,.heif" multiple disabled={submitting || photos.length >= maximumPhotoCount} onChange={handlePhotoInput} />
                <span className="upload-icon" aria-hidden="true"><ImagePlus size={17} /></span>
                <strong>{photos.length >= maximumPhotoCount ? '4 photos selected' : 'Add project photos'}</strong>
                <small>Drag and drop or choose up to 4 photos · 5 MB each</small>
              </label>}
              <label className="privacy-acknowledgement" htmlFor="quote-privacy">
                <input id="quote-privacy" type="checkbox" required checked={privacyAccepted} aria-invalid={Boolean(privacyError)} aria-describedby={privacyError ? 'quote-privacy-error' : 'quote-privacy-help'} onChange={(event) => { setPrivacyAccepted(event.target.checked); setPrivacyError('') }} />
                <span>I have read the <a href="#privacy">Privacy Notice</a> and understand how my contact details, project information, and optional photos will be used.</span>
              </label>
              <small className="privacy-help" id="quote-privacy-help">Only upload photos relevant to the painting work. Avoid including people or unrelated personal information.</small>
              {privacyError && <span className="field-error" id="quote-privacy-error"><AlertCircle size={13} aria-hidden="true" />{privacyError}</span>}
              </>}
              {photos.length > 0 && <div className="photo-preview-grid" aria-label="Selected project photos">
                {photos.map((photo) => <article className={`photo-preview photo-${photo.status}`} key={photo.id}>
                  <img src={photo.previewUrl} alt="" />
                  <div className="photo-preview-shade" aria-hidden="true" />
                  <button type="button" aria-label={`Remove ${photo.file.name}`} disabled={photo.status === 'uploading'} onClick={() => removePhoto(photo.id)}><X size={15} /></button>
                  <div className="photo-meta"><strong title={photo.file.name}>{photo.file.name}</strong><span>{photo.status === 'uploading' ? 'Uploading…' : photo.status === 'uploaded' ? <><Check size={13} /> Uploaded</> : photo.status === 'failed' ? 'Upload failed' : `${(photo.file.size / 1024 / 1024).toFixed(1)} MB`}</span></div>
                </article>)}
              </div>}
              {photoError && <div className="photo-error" role="alert"><span>{photoError}</span>{uploadContext && <button type="button" onClick={retryPhotoUploads} disabled={submitting}><RotateCcw size={14} />{submitting ? 'Retrying…' : 'Retry photo uploads'}</button>}</div>}
              {submissionReference && <div className="form-success" role="status"><strong>Thanks—your request has been received.</strong><span>Reference: {submissionReference}. {notificationSent ? 'Our team has been notified and will contact you shortly.' : 'Your project details are safely recorded.'}</span></div>}
              {completionError && <div className="completion-error" role="alert"><div><strong>Your quote is safely saved.</strong><span>{completionError}</span></div><button type="button" onClick={retryCompletion} disabled={submitting}><RotateCcw size={14} />{submitting ? 'Retrying…' : 'Retry confirmation'}</button></div>}
              {!submissionReference && <div className="form-submit"><button className="button button-primary" type="submit" disabled={submitting || !privacyAccepted} aria-describedby="quote-submit-help">{submitting ? 'Submitting…' : 'Submit Quote Request →'}</button><p id="quote-submit-help">{privacyAccepted ? 'We use your details to assess and respond to this project request.' : 'Read and accept the Privacy Notice to enable submission.'}</p></div>}
              {submissionReference && notificationSent && photos.length === 0 && <div className="form-submit"><button className="button button-dark" type="button" onClick={startAnotherQuote}>Start another request →</button></div>}
              {submissionError && <div className="form-error" role="alert"><strong>Unable to submit your request.</strong><span>{submissionError}</span></div>}
            </form>
          </div>
        </section>

        <section className="section reviews-section" id="reviews">
          <div className="page-container"><div className="section-heading review-heading"><div><p className="eyebrow">Customer confidence</p><h2>Feedback that reflects<br />the finished work.</h2></div><div className="rating-summary"><strong>5.0</strong><span>★★★★★</span><small>One verified Bark review</small></div></div>
            <div className="reviews-grid reviews-grid-single">
              <article className="review-card featured-review rounded-card ring-1 ring-slate-950/5 transition duration-300 hover:-translate-y-1 hover:shadow-enterprise"><div className="stars" aria-label="5 out of 5 stars">★★★★★</div><blockquote>“Great service. Good price. Great job.”</blockquote><footer><strong>Clare</strong><span>Verified Bark customer · Interior painting</span></footer></article>
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
            <div className="contact-info-panel"><p className="eyebrow eyebrow-light">Contact info</p><h2>Visit or speak with our Hamilton team.</h2><p>Contact Greenstone Painting to discuss your property, arrange a site visit, or request a project quote.</p><div className="contact-details"><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer"><MapPin size={20} strokeWidth={1.8} aria-hidden="true" /><span><small>Office address</small><strong>29 Lachlan Drive<br />Dinsdale, Hamilton</strong></span></a><a href="tel:+642108383831"><Phone size={20} strokeWidth={1.8} aria-hidden="true" /><span><small>Phone</small><strong>021 083 83831</strong></span></a><a href="mailto:info@greenstonepainting.co.nz"><Mail size={20} strokeWidth={1.8} aria-hidden="true" /><span><small>Email</small><strong>info@greenstonepainting.co.nz</strong></span></a></div></div>
            <div className="map-card google-map-card rounded-card shadow-enterprise ring-1 ring-white/10"><iframe src="https://www.google.com/maps?q=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand&amp;output=embed" title="Google Map showing Greenstone Painting at 29 Lachlan Drive, Dinsdale" loading="lazy" referrerPolicy="no-referrer-when-downgrade" allowFullScreen /><div className="map-footer"><span>Greenstone Painting office</span><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer">Open in Google Maps ↗</a></div></div>
          </div>
        </section>

        <section className="section insights-section" id="insights">
          <div className="page-container"><div className="section-heading insights-heading"><div><p className="eyebrow">Advice &amp; insights</p><h2>Better decisions before<br />the first coat.</h2></div><a className="inline-link" href="/blog/">View all articles →</a></div><div className="articles-grid">{articles.map((article) => <article key={article.title}><span>{article.topic}</span><h3>{article.title}</h3><div><time>{article.date}</time><a href={article.path} aria-label={`Read ${article.title}`}>Read article ↗</a></div></article>)}</div></div>
        </section>

        <section className="section privacy-section" id="privacy" aria-labelledby="privacy-heading">
          <div className="page-container privacy-layout">
            <div><p className="eyebrow">Privacy notice</p><h2 id="privacy-heading">Your project information stays purposeful.</h2><p className="privacy-updated">Website privacy notice · Greenstone Painting Limited</p></div>
            <div className="privacy-content">
              <article><h3>What we collect</h3><p>When you request a quote, we collect the contact details, property location, project description, service selection, and any project photos you choose to provide.</p></article>
              <article><h3>How we use it</h3><p>We use this information to assess your project, contact you, arrange a site visit, prepare or follow up a quote, maintain relevant business records, and operate this service securely.</p></article>
              <article><h3>Storage and sharing</h3><p>We limit access to people and service providers who need the information to deliver or support the service. We do not sell your personal information. Information may also be disclosed where required by New Zealand law.</p></article>
              <article><h3>Retention and your rights</h3><p>We keep information only for as long as reasonably necessary for these purposes and applicable legal obligations. You may ask to access or correct your personal information by emailing <a href="mailto:info@greenstonepainting.co.nz">info@greenstonepainting.co.nz</a>.</p></article>
            </div>
          </div>
        </section>

        <section className="final-cta"><div className="final-cta-shade" aria-hidden="true" /><div className="page-container final-cta-inner"><div><span>Ready when you are</span><h2>Planning Your Next<br />Painting Project?</h2></div><a className="button button-primary shadow-xl shadow-greenstone-950/25 transition duration-300 hover:-translate-y-0.5" href="#quote">Request Your Free Quote →</a></div></section>
      </main>

      <footer className="site-footer" id="footer">
        <div className="page-container footer-main">
          <div className="footer-brand"><a className="brand brand-logo brand-logo-footer" href="#top" aria-label="Greenstone Painting Limited home"><img src="/images/greenstone-logo.png" alt="Greenstone Painting Limited" /></a><p>Professional residential and commercial painting throughout Waikato.</p><a className="footer-call" href="tel:+642108383831">021 083 83831</a></div>
          <div className="footer-column"><h2>Services</h2>{services.slice(0, 5).map((service) => <a href={`/services/#${service.slug}`} key={service.slug}>{service.title}</a>)}</div>
          <div className="footer-column"><h2>Company</h2><a href="#about">About Us</a><a href="/projects/">Projects</a><a href="#areas">Service Areas</a><a href="/blog/">Blog</a><a href="#quote">Get a Quote</a></div>
          <div className="footer-column"><h2>Contact</h2><a href="mailto:info@greenstonepainting.co.nz">info@greenstonepainting.co.nz</a><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer">29 Lachlan Drive<br />Dinsdale, Hamilton</a><div className="social-row"><a className="social-instagram" href="https://www.instagram.com/greenstonepainting.nz/" target="_blank" rel="noreferrer" aria-label="Follow Greenstone Painting on Instagram"><FaInstagram size={16} aria-hidden="true" /></a><a className="social-facebook" href="https://www.facebook.com/greenstonepainting/" target="_blank" rel="noreferrer" aria-label="Visit Greenstone Painting on Facebook"><FaFacebookF size={15} aria-hidden="true" /></a></div></div>
        </div>
        <div className="page-container footer-bottom"><span>© {new Date().getFullYear()} Greenstone Painting Limited</span><a href="#privacy">Privacy Notice</a></div>
      </footer>
      <ColourStudio open={colourStudioOpen} onClose={closeColourStudio} />
    </div>
  )
}

export default App
