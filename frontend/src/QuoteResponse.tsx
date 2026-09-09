import { useEffect, useState } from 'react'
import { AlertTriangle, Check, CheckCircle2, Download, LoaderCircle, Mail, MapPin, Paintbrush, Phone, Send, X } from 'lucide-react'
import { downloadPublicQuotePdf, getPublicQuote, respondToQuote, type PublicQuote } from './api/quotes'
import './QuoteResponse.css'

function money(value: number) {
  return new Intl.NumberFormat('en-NZ', { style: 'currency', currency: 'NZD' }).format(value)
}

function date(value: string | null) {
  if (!value) return 'To be confirmed'
  return new Intl.DateTimeFormat('en-NZ', { dateStyle: 'long', timeZone: 'Pacific/Auckland' }).format(new Date(`${value}T00:00:00`))
}

export default function QuoteResponse() {
  const token = new URLSearchParams(window.location.search).get('token') ?? ''
  const [quote, setQuote] = useState<PublicQuote | null>(null)
  const [loading, setLoading] = useState(Boolean(token))
  const [busy, setBusy] = useState('')
  const [error, setError] = useState(token ? '' : 'This quote link is incomplete. Please use the full link from your email.')
  const [dialog, setDialog] = useState<'ACCEPT' | 'DECLINE' | ''>('')
  const [reason, setReason] = useState('')

  useEffect(() => {
    if (!token) return
    getPublicQuote(token).then(setQuote).catch((caught) => setError(caught instanceof Error ? caught.message : 'This quote could not be loaded.')).finally(() => setLoading(false))
  }, [token])

  async function respond(decision: 'ACCEPT' | 'DECLINE') {
    setBusy(decision); setError('')
    try {
      setQuote(await respondToQuote(token, decision, reason))
      setDialog('')
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'Your response could not be saved.') }
    finally { setBusy('') }
  }

  async function download() {
    if (!quote) return
    setBusy('PDF'); setError('')
    try {
      const blob = await downloadPublicQuotePdf(token)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url; anchor.download = `${quote.quoteNumber}.pdf`; anchor.click()
      window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'The PDF could not be downloaded.') }
    finally { setBusy('') }
  }

  if (loading) return <main className="customer-quote-state"><LoaderCircle className="admin-spinner" aria-hidden="true" /><span>Loading your Greenstone Painting quote…</span></main>
  if (!quote) return <main className="customer-quote-state"><AlertTriangle aria-hidden="true" /><h1>Quote unavailable</h1><p>{error}</p><a href="mailto:info@greenstonepainting.co.nz">Contact Greenstone Painting</a></main>

  const included = quote.items.filter((item) => !item.optional)
  const optional = quote.items.filter((item) => item.optional)
  const open = quote.status === 'SENT'

  return (
    <main className="customer-quote-page">
      <header className="customer-quote-header"><a href="/" aria-label="Greenstone Painting home"><img src="/images/greenstone-logo.png" alt="Greenstone Painting" /></a><div><a href="tel:+642108383831"><Phone aria-hidden="true" />021 083 83831</a><a href="mailto:info@greenstonepainting.co.nz"><Mail aria-hidden="true" />info@greenstonepainting.co.nz</a></div></header>

      <section className="customer-quote-hero"><div><span>Quote {quote.quoteNumber} · Revision {quote.revisionNumber}</span><h1>Kia ora, {quote.customerName}.</h1><p>Your Greenstone Painting proposal is ready to review.</p></div><div className="customer-quote-price"><span>Total including GST</span><strong>{money(quote.total)}</strong><small>Valid until {date(quote.validUntil)}</small></div></section>

      {quote.status === 'ACCEPTED' && <div className="customer-quote-result accepted"><CheckCircle2 aria-hidden="true" /><div><strong>Thank you—this quote is accepted.</strong><span>Our team will contact you to confirm scheduling and next steps.</span></div></div>}
      {quote.status === 'DECLINED' && <div className="customer-quote-result declined"><X aria-hidden="true" /><div><strong>Your response has been recorded.</strong><span>Thank you for letting us know.</span></div></div>}
      {quote.status === 'EXPIRED' && <div className="customer-quote-result expired"><AlertTriangle aria-hidden="true" /><div><strong>This quote has expired.</strong><span>Please contact our team if you would like an updated proposal.</span></div></div>}
      {quote.status === 'SUPERSEDED' && <div className="customer-quote-result expired"><AlertTriangle aria-hidden="true" /><div><strong>A newer revision is available.</strong><span>Please use the latest quote email from Greenstone Painting.</span></div></div>}
      {error && <div className="customer-quote-error" role="alert"><AlertTriangle aria-hidden="true" />{error}</div>}

      <div className="customer-quote-layout">
        <div className="customer-quote-main">
          <section><span className="customer-kicker">Project</span><h2>{quote.title}</h2>{quote.propertyAddress && <p className="customer-address"><MapPin aria-hidden="true" />{quote.propertyAddress}</p>}<div className="customer-scope">{quote.scope}</div></section>
          <section><span className="customer-kicker">Investment</span><h2>Quote breakdown</h2><div className="customer-line-items">{included.map((item) => <article key={item.id}><div><strong>{item.description}</strong><span>{item.quantity} {item.unit} × {money(item.unitPrice)}</span></div><strong>{money(item.lineTotal)}</strong></article>)}</div><div className="customer-totals"><p><span>Subtotal</span><strong>{money(quote.subtotal)}</strong></p><p><span>GST (15%)</span><strong>{money(quote.gstAmount)}</strong></p><p><span>Total including GST</span><strong>{money(quote.total)}</strong></p></div></section>
          {optional.length > 0 && <section><span className="customer-kicker">Optional additions</span><h2>Available extras</h2><p>These items are not included in the quoted total above.</p><div className="customer-line-items">{optional.map((item) => <article key={item.id}><div><strong>{item.description}</strong><span>{item.quantity} {item.unit}</span></div><strong>{money(item.lineTotal)}</strong></article>)}</div></section>}
          <section><span className="customer-kicker">Terms</span><h2>Scope and conditions</h2><div className="customer-scope">{quote.terms}</div></section>
        </div>

        <aside className="customer-quote-sidebar"><section><Paintbrush aria-hidden="true" /><span className="customer-kicker">Indicative timing</span><dl><div><dt>Start</dt><dd>{date(quote.estimatedStartDate)}</dd></div><div><dt>Completion</dt><dd>{date(quote.estimatedEndDate)}</dd></div></dl></section><button type="button" onClick={download} disabled={Boolean(busy)} className="customer-download"><Download aria-hidden="true" /> Download PDF</button>{open && <div className="customer-response-actions"><button type="button" onClick={() => setDialog('ACCEPT')} className="customer-accept"><Check aria-hidden="true" /> Accept quote</button><button type="button" onClick={() => setDialog('DECLINE')} className="customer-decline">Decline quote</button></div>}<p>Questions before deciding? Call <a href="tel:+642108383831">021 083 83831</a> or email our team.</p></aside>
      </div>

      <footer className="customer-quote-footer"><strong>Greenstone Painting Limited</strong><span>29 Lachlan Drive, Dinsdale, Hamilton</span><span>Registered Master Painters member</span></footer>

      {dialog && <div className="customer-response-dialog"><section role="dialog" aria-modal="true" aria-labelledby="response-title"><button type="button" className="dialog-close" onClick={() => setDialog('')} aria-label="Close"><X aria-hidden="true" /></button><span className="customer-kicker">Confirm response</span><h2 id="response-title">{dialog === 'ACCEPT' ? `Accept this ${money(quote.total)} quote?` : 'Decline this quote?'}</h2><p>{dialog === 'ACCEPT' ? 'Greenstone Painting will receive your acceptance and contact you to confirm scheduling.' : 'You can optionally tell us why, which helps our team understand your decision.'}</p>{dialog === 'DECLINE' && <label><span>Optional feedback</span><textarea value={reason} onChange={(event) => setReason(event.target.value)} maxLength={1000} rows={4} /></label>}<div><button type="button" onClick={() => setDialog('')} className="customer-dialog-cancel">Go back</button><button type="button" onClick={() => void respond(dialog)} disabled={Boolean(busy)} className={dialog === 'ACCEPT' ? 'customer-accept' : 'customer-decline'}>{busy ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <Send aria-hidden="true" />} Confirm {dialog === 'ACCEPT' ? 'acceptance' : 'decline'}</button></div></section></div>}
    </main>
  )
}
