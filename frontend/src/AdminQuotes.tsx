import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { AlertTriangle, CheckCircle2, Clock3, Download, FilePlus2, LoaderCircle, Mail, Plus, RefreshCw, Save, Send, Trash2, X } from 'lucide-react'
import {
  AdminQuoteApiError,
  createAdminQuote,
  createQuoteRevision,
  downloadAdminQuotePdf,
  getAdminQuote,
  listAdminQuotes,
  saveAdminQuote,
  sendAdminQuote,
  type AdminQuoteDetail,
  type AdminQuoteSummary,
  type QuoteItemCategory,
  type SaveAdminQuote,
} from './api/adminQuotes'
import './AdminQuotes.css'

type Draft = Omit<SaveAdminQuote, 'version'>

const categoryLabels: Record<QuoteItemCategory, string> = {
  LABOUR: 'Labour',
  MATERIALS: 'Materials',
  PREPARATION: 'Preparation',
  OPTIONAL: 'Optional',
  OTHER: 'Other',
}

const statusLabels = {
  DRAFT: 'Draft', SENT: 'Sent', ACCEPTED: 'Accepted', DECLINED: 'Declined', EXPIRED: 'Expired', SUPERSEDED: 'Superseded',
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('en-NZ', { style: 'currency', currency: 'NZD' }).format(value)
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('en-NZ', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Pacific/Auckland' }).format(new Date(value))
}

function draftFromQuote(quote: AdminQuoteDetail): Draft {
  return {
    customerName: quote.customerName,
    customerEmail: quote.customerEmail,
    propertyAddress: quote.propertyAddress,
    title: quote.title,
    scope: quote.scope,
    terms: quote.terms,
    validUntil: quote.validUntil,
    estimatedStartDate: quote.estimatedStartDate,
    estimatedEndDate: quote.estimatedEndDate,
    items: quote.items.map(({ category, description, quantity, unit, unitPrice, optional }) => ({ category, description, quantity, unit, unitPrice, optional })),
  }
}

export default function AdminQuotes({ enquiryId, onSessionExpired, onQuoteChanged }: { enquiryId: string; onSessionExpired: () => void; onQuoteChanged: () => void }) {
  const [history, setHistory] = useState<AdminQuoteSummary[]>([])
  const [quote, setQuote] = useState<AdminQuoteDetail | null>(null)
  const [draft, setDraft] = useState<Draft | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [confirmSend, setConfirmSend] = useState(false)

  const handleError = useCallback((caught: unknown, fallback: string) => {
    if (caught instanceof AdminQuoteApiError && caught.status === 401) onSessionExpired()
    else setError(caught instanceof Error ? caught.message : fallback)
  }, [onSessionExpired])

  const applyQuote = useCallback((next: AdminQuoteDetail) => {
    setQuote(next)
    setDraft(draftFromQuote(next))
    setError('')
  }, [])

  const refreshHistory = useCallback(async (selectedId?: string) => {
    const summaries = await listAdminQuotes(enquiryId)
    setHistory(summaries)
    const id = selectedId ?? summaries[0]?.id
    if (id) applyQuote(await getAdminQuote(id))
    else { setQuote(null); setDraft(null) }
  }, [applyQuote, enquiryId])

  useEffect(() => {
    let active = true
    listAdminQuotes(enquiryId)
      .then(async (summaries) => {
        if (!active) return
        setHistory(summaries)
        if (summaries[0]) applyQuote(await getAdminQuote(summaries[0].id))
      })
      .catch((caught) => { if (active) handleError(caught, 'Quote history could not be loaded.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [applyQuote, enquiryId, handleError])

  const totals = useMemo(() => {
    const included = draft?.items.filter((item) => !item.optional && item.category !== 'OPTIONAL') ?? []
    const optional = draft?.items.filter((item) => item.optional || item.category === 'OPTIONAL') ?? []
    const subtotal = included.reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.unitPrice || 0), 0)
    return { subtotal, gst: subtotal * 0.15, total: subtotal * 1.15, optional: optional.reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.unitPrice || 0), 0) }
  }, [draft])

  const dirty = Boolean(quote && draft && JSON.stringify(draft) !== JSON.stringify(draftFromQuote(quote)))
  const editable = quote?.status === 'DRAFT'

  function update<K extends keyof Draft>(key: K, value: Draft[K]) {
    if (!draft) return
    setDraft({ ...draft, [key]: value })
    setError('')
    setSuccess('')
  }

  function updateItem(index: number, changes: Partial<Draft['items'][number]>) {
    if (!draft) return
    update('items', draft.items.map((item, itemIndex) => itemIndex === index ? { ...item, ...changes } : item))
  }

  function validate() {
    if (!draft) return 'Quote details are unavailable.'
    if (!draft.customerName.trim() || !/^\S+@\S+\.\S+$/.test(draft.customerEmail)) return 'Enter the customer name and a valid email address.'
    if (!draft.title.trim() || !draft.scope.trim() || !draft.terms.trim()) return 'Add a title, scope of work, and quote terms.'
    if (!draft.validUntil) return 'Choose when the quote expires.'
    if (draft.estimatedStartDate && draft.estimatedEndDate && draft.estimatedEndDate < draft.estimatedStartDate) return 'Estimated completion cannot be before the start date.'
    if (!draft.items.length || draft.items.some((item) => !item.description.trim() || !item.unit.trim() || Number(item.quantity) <= 0 || Number(item.unitPrice) < 0)) return 'Complete every line item with a description, positive quantity, unit, and valid price.'
    return ''
  }

  async function save(event?: FormEvent) {
    event?.preventDefault()
    if (!quote || !draft || !editable) return null
    const validation = validate()
    if (validation) { setError(validation); return null }
    setBusy('save'); setError(''); setSuccess('')
    try {
      const updated = await saveAdminQuote(quote.id, { ...draft, version: quote.version })
      applyQuote(updated)
      await refreshHistory(updated.id)
      setSuccess('Quote draft saved.')
      return updated
    } catch (caught) {
      handleError(caught, 'The quote could not be saved.')
      return null
    } finally { setBusy('') }
  }

  async function create() {
    setBusy('create'); setError(''); setSuccess('')
    try {
      const created = await createAdminQuote(enquiryId)
      applyQuote(created)
      await refreshHistory(created.id)
      setSuccess('Quote draft created from this enquiry.')
    } catch (caught) { handleError(caught, 'The quote draft could not be created.') }
    finally { setBusy('') }
  }

  async function send() {
    if (!quote || dirty || totals.total <= 0) return
    setBusy('send'); setError(''); setSuccess(''); setConfirmSend(false)
    try {
      const sent = await sendAdminQuote(quote.id)
      applyQuote(sent)
      await refreshHistory(sent.id)
      setSuccess(`Quote emailed to ${sent.customerEmail}.`)
      onQuoteChanged()
    } catch (caught) { handleError(caught, 'The quote could not be emailed.') }
    finally { setBusy('') }
  }

  async function revise() {
    if (!quote) return
    setBusy('revision'); setError(''); setSuccess('')
    try {
      const revision = await createQuoteRevision(quote.id)
      applyQuote(revision)
      await refreshHistory(revision.id)
      setSuccess(`Revision ${revision.revisionNumber} is ready to edit.`)
    } catch (caught) { handleError(caught, 'A new revision could not be created.') }
    finally { setBusy('') }
  }

  async function downloadPdf() {
    if (!quote) return
    setBusy('pdf'); setError('')
    try {
      const blob = await downloadAdminQuotePdf(quote.id)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = `${quote.quoteNumber}.pdf`
      anchor.click()
      window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch (caught) { handleError(caught, 'The quote PDF could not be generated.') }
    finally { setBusy('') }
  }

  if (loading) return <section className="quote-panel quote-panel-state"><LoaderCircle className="admin-spinner" aria-hidden="true" /> Loading quote workspace…</section>

  if (!quote || !draft) return (
    <section className="quote-panel quote-empty">
      <div className="quote-empty-icon"><FilePlus2 aria-hidden="true" /></div><div><span className="admin-eyebrow">Sales workflow</span><h2>Create a customer quote</h2><p>Turn this enquiry into a priced, branded proposal with GST, PDF delivery, and customer acceptance tracking.</p></div>
      {error && <div className="quote-alert quote-alert--error"><AlertTriangle aria-hidden="true" />{error}</div>}
      <button type="button" onClick={create} disabled={Boolean(busy)}>{busy ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <Plus aria-hidden="true" />} Create quote draft</button>
    </section>
  )

  return (
    <section className="quote-panel">
      <header className="quote-panel-header">
        <div><span className="admin-eyebrow">Customer quote</span><h2>{quote.quoteNumber}</h2><p>Revision {quote.revisionNumber} · <span className={`quote-status quote-status--${quote.status.toLowerCase()}`}>{statusLabels[quote.status]}</span></p></div>
        <div className="quote-header-actions">
          {history.length > 1 && <label><span>Version</span><select value={quote.id} onChange={(event) => { setBusy('load'); getAdminQuote(event.target.value).then(applyQuote).catch((caught) => handleError(caught, 'The revision could not be loaded.')).finally(() => setBusy('')) }}>{history.map((item) => <option key={item.id} value={item.id}>Rev {item.revisionNumber} · {statusLabels[item.status]}</option>)}</select></label>}
          <button type="button" onClick={downloadPdf} disabled={Boolean(busy)} className="quote-secondary"><Download aria-hidden="true" /> PDF</button>
        </div>
      </header>

      {success && <div className="quote-alert quote-alert--success"><CheckCircle2 aria-hidden="true" />{success}</div>}
      {error && <div className="quote-alert quote-alert--error"><AlertTriangle aria-hidden="true" />{error}</div>}

      <form onSubmit={save} className="quote-editor">
        <fieldset disabled={!editable || Boolean(busy)}>
          <div className="quote-form-grid">
            <label><span>Customer name</span><input value={draft.customerName} onChange={(event) => update('customerName', event.target.value)} maxLength={200} /></label>
            <label><span>Customer email</span><input type="email" value={draft.customerEmail} onChange={(event) => update('customerEmail', event.target.value)} maxLength={254} /></label>
            <label className="quote-span"><span>Property address</span><input value={draft.propertyAddress ?? ''} onChange={(event) => update('propertyAddress', event.target.value || null)} maxLength={300} /></label>
            <label className="quote-span"><span>Quote title</span><input value={draft.title} onChange={(event) => update('title', event.target.value)} maxLength={200} /></label>
            <label><span>Valid until</span><input type="date" value={draft.validUntil} onChange={(event) => update('validUntil', event.target.value)} /></label>
            <label><span>Estimated start</span><input type="date" value={draft.estimatedStartDate ?? ''} onChange={(event) => update('estimatedStartDate', event.target.value || null)} /></label>
            <label><span>Estimated completion</span><input type="date" value={draft.estimatedEndDate ?? ''} onChange={(event) => update('estimatedEndDate', event.target.value || null)} /></label>
            <label className="quote-span"><span>Scope of work</span><textarea rows={6} value={draft.scope} onChange={(event) => update('scope', event.target.value)} maxLength={10000} /></label>
          </div>

          <div className="quote-items-heading"><div><span className="admin-eyebrow">Pricing</span><h3>Line items</h3></div>{editable && <button type="button" onClick={() => update('items', [...draft.items, { category: 'LABOUR', description: '', quantity: 1, unit: 'item', unitPrice: 0, optional: false }])}><Plus aria-hidden="true" /> Add item</button>}</div>
          <div className="quote-items">
            {draft.items.map((item, index) => <div className="quote-item" key={index}>
              <label><span>Category</span><select value={item.category} onChange={(event) => updateItem(index, { category: event.target.value as QuoteItemCategory, optional: event.target.value === 'OPTIONAL' ? true : item.optional })}>{Object.entries(categoryLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
              <label className="quote-item-description"><span>Description</span><input value={item.description} onChange={(event) => updateItem(index, { description: event.target.value })} maxLength={500} /></label>
              <label><span>Quantity</span><input type="number" min="0.01" step="0.01" value={item.quantity} onChange={(event) => updateItem(index, { quantity: Number(event.target.value) })} /></label>
              <label><span>Unit</span><input value={item.unit} onChange={(event) => updateItem(index, { unit: event.target.value })} maxLength={40} /></label>
              <label><span>Unit price</span><input type="number" min="0" step="0.01" value={item.unitPrice} onChange={(event) => updateItem(index, { unitPrice: Number(event.target.value) })} /></label>
              <label className="quote-optional"><input type="checkbox" checked={item.optional} onChange={(event) => updateItem(index, { optional: event.target.checked })} /><span>Optional</span></label>
              <strong>{formatMoney(Number(item.quantity || 0) * Number(item.unitPrice || 0))}</strong>
              {editable && <button type="button" aria-label={`Remove ${item.description || 'line item'}`} onClick={() => update('items', draft.items.filter((_, itemIndex) => itemIndex !== index))} disabled={draft.items.length === 1}><Trash2 aria-hidden="true" /></button>}
            </div>)}
          </div>

          <div className="quote-totals"><div><span>Subtotal</span><strong>{formatMoney(totals.subtotal)}</strong></div><div><span>GST (15%)</span><strong>{formatMoney(totals.gst)}</strong></div><div className="quote-total-main"><span>Total including GST</span><strong>{formatMoney(totals.total)}</strong></div>{totals.optional > 0 && <div><span>Optional additions</span><strong>{formatMoney(totals.optional)}</strong></div>}</div>
          <label className="quote-terms"><span>Terms and conditions</span><textarea rows={5} value={draft.terms} onChange={(event) => update('terms', event.target.value)} maxLength={10000} /></label>
        </fieldset>

        <div className="quote-editor-actions">
          {editable ? <><button type="submit" disabled={!dirty || Boolean(busy)} className="quote-secondary">{busy === 'save' ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <Save aria-hidden="true" />}{dirty ? 'Save draft' : 'Draft saved'}</button><button type="button" onClick={() => setConfirmSend(true)} disabled={dirty || totals.total <= 0 || Boolean(busy)} className="quote-send"><Send aria-hidden="true" /> Email quote to customer</button></> : quote.status !== 'ACCEPTED' && quote.status !== 'SUPERSEDED' && <button type="button" onClick={revise} disabled={Boolean(busy)} className="quote-send">{busy === 'revision' ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <RefreshCw aria-hidden="true" />} Create new revision</button>}
        </div>
      </form>

      <div className="quote-history"><div className="quote-items-heading"><div><span className="admin-eyebrow">Audit history</span><h3>Quote activity</h3></div></div>{quote.activities.map((activity) => <article key={activity.id}><Clock3 aria-hidden="true" /><div><strong>{activity.summary}</strong><span>{activity.actorDisplayName} · {formatDateTime(activity.createdAt)}</span></div></article>)}</div>

      {confirmSend && <div className="quote-confirm"><section role="dialog" aria-modal="true" aria-labelledby="send-quote-title"><button type="button" className="quote-confirm-close" onClick={() => setConfirmSend(false)} aria-label="Close"><X aria-hidden="true" /></button><Mail aria-hidden="true" /><span className="admin-eyebrow">Final check</span><h2 id="send-quote-title">Email {formatMoney(totals.total)} to {draft.customerName}?</h2><p>The PDF and secure accept-or-decline link will be sent to <strong>{draft.customerEmail}</strong>. This revision becomes locked after sending.</p><div><button type="button" className="quote-secondary" onClick={() => setConfirmSend(false)}>Cancel</button><button type="button" className="quote-send" onClick={send}><Send aria-hidden="true" /> Send quote</button></div></section></div>}
    </section>
  )
}
