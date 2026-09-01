import { useEffect, useRef, useState } from 'react'
import { ArrowRight, ChevronLeft, Mail, MapPin, MessageCircle, Paintbrush, Phone, X } from 'lucide-react'
import { serviceAreas, services } from './data/site'
import './ChatAssistant.css'

type ChatView = 'menu' | 'quote' | 'services' | 'coverage' | 'preparation' | 'contact' | 'surface-prep' | 'furniture' | 'colour'

const preparationAnswers: Record<'surface-prep' | 'furniture' | 'colour', { title: string; answer: string }> = {
  'surface-prep': { title: 'How is a surface prepared?', answer: 'Preparation depends on the surface and its condition. It may include protection, cleaning, repairs, sanding, and a compatible primer before finishing coats are applied.' },
  furniture: { title: 'Do I need to move furniture?', answer: 'Clear smaller belongings where practical. The exact plan for larger furniture, floors, fittings, and access is confirmed when the project is assessed.' },
  colour: { title: 'Can you help with colour?', answer: 'Yes. Greenstone Painting can discuss colour, sheen, samples, and coating considerations as part of planning the right finish for your space.' },
}

function navigateToQuote(serviceSlug?: string) {
  const pathname = window.location.pathname.endsWith('/') ? window.location.pathname : `${window.location.pathname}/`
  if (pathname === '/') {
    window.dispatchEvent(new CustomEvent('greenstone:open-quote', { detail: { serviceSlug } }))
    return
  }
  const query = serviceSlug ? `?service=${encodeURIComponent(serviceSlug)}` : ''
  window.location.assign(`/${query}#quote`)
}

export default function ChatAssistant() {
  const [open, setOpen] = useState(false)
  const [view, setView] = useState<ChatView>('menu')
  const launcherRef = useRef<HTMLButtonElement>(null)
  const closeRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!open) return
    closeRef.current?.focus()
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
        setView('menu')
        launcherRef.current?.focus()
      }
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [open])

  const closeChat = () => {
    setOpen(false)
    setView('menu')
    launcherRef.current?.focus()
  }

  const chooseQuote = (serviceSlug?: string) => {
    setOpen(false)
    setView('menu')
    navigateToQuote(serviceSlug)
  }

  const goBack = () => setView(view === 'surface-prep' || view === 'furniture' || view === 'colour' ? 'preparation' : 'menu')
  const preparationAnswer = view === 'surface-prep' || view === 'furniture' || view === 'colour' ? preparationAnswers[view] : null

  return <aside className="chat-assistant" aria-label="Greenstone Painting guided assistant">
    {open && <section className="chat-panel" id="greenstone-assistant-panel" role="dialog" aria-modal="false" aria-labelledby="chat-title">
      <div className="chat-paint-line" aria-hidden="true"><span /><span /><span /><span /></div>
      <header className="chat-header">
        <div className="chat-identity"><span><Paintbrush size={20} strokeWidth={1.8} aria-hidden="true" /></span><div><strong id="chat-title">Ask Greenstone</strong><small>Guided project assistant</small></div></div>
        <button ref={closeRef} type="button" onClick={closeChat} aria-label="Close project assistant"><X size={19} /></button>
      </header>

      <div className="chat-body" aria-live="polite">
        {view !== 'menu' && <button className="chat-back" type="button" onClick={goBack}><ChevronLeft size={15} aria-hidden="true" /> Back</button>}

        {view === 'menu' && <>
          <div className="chat-message"><p>Kia ora. I can help you find the right service, prepare for a painting project, or move your details into the quote form.</p></div>
          <p className="chat-prompt">What would you like help with?</p>
          <div className="chat-options">
            <button type="button" onClick={() => setView('quote')}><strong>Request a quote</strong><span>Start with your service</span><ArrowRight size={16} aria-hidden="true" /></button>
            <button type="button" onClick={() => setView('services')}><strong>Explore services</strong><span>Find the closest match</span><ArrowRight size={16} aria-hidden="true" /></button>
            <button type="button" onClick={() => setView('coverage')}><strong>Check service coverage</strong><span>Areas currently covered</span><ArrowRight size={16} aria-hidden="true" /></button>
            <button type="button" onClick={() => setView('preparation')}><strong>Project preparation</strong><span>Common questions</span><ArrowRight size={16} aria-hidden="true" /></button>
            <button type="button" onClick={() => setView('contact')}><strong>Contact the team</strong><span>Phone, email, and office</span><ArrowRight size={16} aria-hidden="true" /></button>
          </div>
        </>}

        {view === 'quote' && <>
          <div className="chat-message"><p>Which painting service is closest to your project? You can still adjust it in the full quote form.</p></div>
          <div className="chat-service-options">{services.map((service) => <button type="button" key={service.slug} onClick={() => chooseQuote(service.slug)}>{service.title}<ArrowRight size={15} aria-hidden="true" /></button>)}</div>
          <button className="chat-secondary-action" type="button" onClick={() => chooseQuote()}>I’m not sure yet</button>
        </>}

        {view === 'services' && <>
          <div className="chat-message"><p>Greenstone Painting currently provides these residential and commercial painting services across Waikato.</p></div>
          <div className="chat-service-options">{services.map((service) => <a href={`/services/#${service.slug}`} key={service.slug}>{service.title}<ArrowRight size={15} aria-hidden="true" /></a>)}</div>
          <a className="chat-primary-action" href="/services/">View all service details <ArrowRight size={15} aria-hidden="true" /></a>
        </>}

        {view === 'coverage' && <>
          <div className="chat-message"><p>Current service coverage includes these locations and surrounding communities:</p></div>
          <div className="chat-area-list">{serviceAreas.map((area) => <span key={area}><MapPin size={13} aria-hidden="true" />{area}</span>)}</div>
          <button className="chat-primary-action" type="button" onClick={() => chooseQuote()}>Discuss your location <ArrowRight size={15} aria-hidden="true" /></button>
        </>}

        {view === 'preparation' && <>
          <div className="chat-message"><p>Choose a common preparation question. Final requirements are always confirmed for the actual property and surfaces.</p></div>
          <div className="chat-options compact"><button type="button" onClick={() => setView('surface-prep')}><strong>Surface preparation</strong><ArrowRight size={16} aria-hidden="true" /></button><button type="button" onClick={() => setView('furniture')}><strong>Furniture and protection</strong><ArrowRight size={16} aria-hidden="true" /></button><button type="button" onClick={() => setView('colour')}><strong>Colour and finish advice</strong><ArrowRight size={16} aria-hidden="true" /></button></div>
          <a className="chat-secondary-action" href="/blog/">Read painting guides</a>
        </>}

        {preparationAnswer && <>
          <div className="chat-message chat-answer"><strong>{preparationAnswer.title}</strong><p>{preparationAnswer.answer}</p></div>
          <button className="chat-primary-action" type="button" onClick={() => chooseQuote()}>Discuss your project <ArrowRight size={15} aria-hidden="true" /></button>
        </>}

        {view === 'contact' && <>
          <div className="chat-message"><p>Choose the most convenient way to speak with Greenstone Painting.</p></div>
          <div className="chat-contact-list"><a href="tel:+642108383831"><Phone size={18} aria-hidden="true" /><span><small>Call</small><strong>021 083 83831</strong></span></a><a href="mailto:info@greenstonepainting.co.nz"><Mail size={18} aria-hidden="true" /><span><small>Email</small><strong>info@greenstonepainting.co.nz</strong></span></a><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer"><MapPin size={18} aria-hidden="true" /><span><small>Office</small><strong>29 Lachlan Drive, Dinsdale</strong></span></a></div>
        </>}
      </div>

      <footer className="chat-footer"><span>This assistant provides approved general information.</span><a href="/#privacy">Privacy</a></footer>
    </section>}

    <button ref={launcherRef} className={`chat-launcher ${open ? 'is-open' : ''}`} type="button" aria-expanded={open} aria-controls="greenstone-assistant-panel" onClick={() => { setOpen((current) => !current); if (open) setView('menu') }}>
      <span className="chat-launcher-icon">{open ? <X size={21} aria-hidden="true" /> : <MessageCircle size={22} aria-hidden="true" />}</span><span className="chat-launcher-copy"><strong>{open ? 'Close' : 'Ask Greenstone'}</strong><small>{open ? 'Project assistant' : 'How can we help?'}</small></span>
    </button>
  </aside>
}
