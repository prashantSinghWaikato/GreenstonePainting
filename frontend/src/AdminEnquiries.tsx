import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { AlertTriangle, ArrowLeft, ArrowRight, CheckCircle2, ChevronLeft, ChevronRight, Download, ExternalLink, History, Image, Inbox, LoaderCircle, Mail, MapPin, Phone, RefreshCw, Save, Search, SlidersHorizontal, StickyNote, UserRound, X } from 'lucide-react'
import {
  AdminEnquiryApiError,
  getAdminEnquiry,
  getAdminEnquiryPhoto,
  listAdminEnquiries,
  updateAdminEnquiryWorkflow,
  type AdminEnquiryAttachment,
  type AdminEnquiryDetail,
  type AdminEnquiryFilters,
  type AdminEnquiryPage,
  type EnquiryStatus,
} from './api/adminEnquiries'
import './AdminEnquiries.css'

const emptyFilters: AdminEnquiryFilters = { query: '', status: '', service: '', from: '', to: '' }

const statusLabels: Record<EnquiryStatus, string> = {
  NEW: 'New',
  IN_REVIEW: 'In review',
  CONTACTED: 'Contacted',
  QUOTED: 'Quoted',
  WON: 'Won',
  LOST: 'Lost',
  CLOSED: 'Closed',
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en-NZ', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Pacific/Auckland',
  }).format(new Date(value))
}

function formatBytes(bytes: number) {
  return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function StatusBadge({ status }: { status: EnquiryStatus }) {
  return <span className={`enquiry-status enquiry-status--${status.toLowerCase()}`}>{statusLabels[status]}</span>
}

function ProjectPhoto({ enquiryId, attachment }: { enquiryId: string; attachment: AdminEnquiryAttachment }) {
  const [url, setUrl] = useState('')
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    let objectUrl = ''
    getAdminEnquiryPhoto(enquiryId, attachment.id)
      .then((blob) => {
        if (!active) return
        objectUrl = URL.createObjectURL(blob)
        setUrl(objectUrl)
      })
      .catch(() => {
        if (active) setFailed(true)
      })
    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [attachment.id, enquiryId])

  const browserPreview = ['image/jpeg', 'image/png', 'image/webp'].includes(attachment.contentType)

  return (
    <article className="enquiry-photo-card">
      <div className="enquiry-photo-preview">
        {!url && !failed && <LoaderCircle className="admin-spinner" aria-hidden="true" />}
        {url && browserPreview && <img src={url} alt={`Project upload: ${attachment.filename}`} />}
        {url && !browserPreview && <><Image aria-hidden="true" /><span>Preview unavailable</span></>}
        {failed && <><Image aria-hidden="true" /><span>Photo unavailable</span></>}
      </div>
      <div className="enquiry-photo-caption">
        <div><strong>{attachment.filename}</strong><span>{formatBytes(attachment.sizeBytes)}</span></div>
        {url && <a href={url} download={attachment.filename} aria-label={`Download ${attachment.filename}`}><Download aria-hidden="true" /></a>}
      </div>
    </article>
  )
}

function EnquiryDetail({
  enquiryId,
  onBack,
  onSessionExpired,
  onWorkflowUpdated,
}: {
  enquiryId: string
  onBack: () => void
  onSessionExpired: () => void
  onWorkflowUpdated: () => void
}) {
  const [enquiry, setEnquiry] = useState<AdminEnquiryDetail | null>(null)
  const [error, setError] = useState('')
  const [draftStatus, setDraftStatus] = useState<EnquiryStatus>('NEW')
  const [draftNotes, setDraftNotes] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const [savedMessage, setSavedMessage] = useState('')
  const [conflict, setConflict] = useState(false)
  const [confirmingTerminalStatus, setConfirmingTerminalStatus] = useState(false)

  const applyDetail = useCallback((detail: AdminEnquiryDetail) => {
    setEnquiry(detail)
    setDraftStatus(detail.status)
    setDraftNotes(detail.internalNotes ?? '')
    setConflict(false)
  }, [])

  useEffect(() => {
    let active = true
    getAdminEnquiry(enquiryId)
      .then((detail) => { if (active) applyDetail(detail) })
      .catch((caught) => {
        if (!active) return
        if (caught instanceof AdminEnquiryApiError && caught.status === 401) onSessionExpired()
        else setError(caught instanceof Error ? caught.message : 'The enquiry could not be loaded.')
      })
    return () => { active = false }
  }, [applyDetail, enquiryId, onSessionExpired])

  async function reloadLatest() {
    setSaving(true)
    setSaveError('')
    try {
      applyDetail(await getAdminEnquiry(enquiryId))
      setSavedMessage('Latest enquiry version loaded.')
      onWorkflowUpdated()
    } catch (caught) {
      if (caught instanceof AdminEnquiryApiError && caught.status === 401) onSessionExpired()
      else setSaveError(caught instanceof Error ? caught.message : 'The latest enquiry could not be loaded.')
    } finally {
      setSaving(false)
    }
  }

  if (error) {
    return <div className="enquiry-state"><div className="admin-form-error" role="alert">{error}</div><button type="button" onClick={onBack} className="enquiry-secondary-button">Back to inbox</button></div>
  }
  if (!enquiry) {
    return <div className="enquiry-state"><LoaderCircle className="admin-spinner" aria-hidden="true" /><span>Loading request…</span></div>
  }

  const customerName = `${enquiry.firstName} ${enquiry.lastName}`
  const normalizedNotes = draftNotes.trim()
  const workflowChanged = draftStatus !== enquiry.status || normalizedNotes !== (enquiry.internalNotes ?? '')

  async function saveWorkflow(event?: FormEvent<HTMLFormElement>, terminalConfirmed = false) {
    event?.preventDefault()
    if (!enquiry || !workflowChanged) return
    const terminalStatus = draftStatus === 'LOST' || draftStatus === 'CLOSED'
    if (terminalStatus && draftStatus !== enquiry.status && !terminalConfirmed) {
      setConfirmingTerminalStatus(true)
      return
    }

    setSaving(true)
    setSaveError('')
    setSavedMessage('')
    setConflict(false)
    try {
      const updated = await updateAdminEnquiryWorkflow(enquiry.id, {
        status: draftStatus,
        internalNotes: normalizedNotes || null,
        version: enquiry.version,
      })
      applyDetail(updated)
      setSavedMessage('Workflow changes saved successfully.')
      onWorkflowUpdated()
    } catch (caught) {
      if (caught instanceof AdminEnquiryApiError && caught.status === 401) {
        onSessionExpired()
      } else if (caught instanceof AdminEnquiryApiError && caught.status === 409) {
        setConflict(true)
        setSaveError(caught.message)
      } else {
        setSaveError(caught instanceof Error ? caught.message : 'The workflow changes could not be saved.')
      }
    } finally {
      setSaving(false)
      setConfirmingTerminalStatus(false)
    }
  }

  return (
    <section className="enquiry-detail">
      <button type="button" onClick={onBack} className="enquiry-back"><ArrowLeft aria-hidden="true" /> Back to enquiries</button>
      <header className="enquiry-detail-header">
        <div>
          <span className="admin-eyebrow">Quote request {enquiry.reference}</span>
          <h1>{customerName}</h1>
          <p>Submitted {formatDate(enquiry.createdAt)}</p>
        </div>
        <StatusBadge status={enquiry.status} />
      </header>

      <div className="enquiry-detail-layout">
        <div className="enquiry-detail-primary">
          <section className="enquiry-detail-card">
            <div className="enquiry-card-heading"><span>Project brief</span><strong>{enquiry.serviceTitle || 'General painting enquiry'}</strong></div>
            <p className="enquiry-message">{enquiry.message}</p>
          </section>

          <section className="enquiry-detail-card">
            <div className="enquiry-card-heading"><span>Project photos</span><strong>{enquiry.attachments.length || 'None supplied'}</strong></div>
            {enquiry.attachments.length > 0 ? (
              <div className="enquiry-photo-grid">
                {enquiry.attachments.map((attachment) => <ProjectPhoto key={attachment.id} enquiryId={enquiry.id} attachment={attachment} />)}
              </div>
            ) : <div className="enquiry-no-photos"><Image aria-hidden="true" /><p>No project photos were attached to this request.</p></div>}
          </section>

          <section className="enquiry-detail-card enquiry-activity-card">
            <div className="enquiry-card-heading"><span>Activity timeline</span><strong>{enquiry.activities.length + 1} events</strong></div>
            <div className="enquiry-timeline">
              {enquiry.activities.map((activity) => (
                <article key={activity.id} className="enquiry-timeline-item">
                  <div className="enquiry-timeline-icon">{activity.type === 'STATUS_CHANGED' ? <RefreshCw aria-hidden="true" /> : <StickyNote aria-hidden="true" />}</div>
                  <div>
                    <strong>{activity.summary}</strong>
                    {activity.type === 'STATUS_CHANGED' && activity.previousStatus && activity.newStatus && (
                      <div className="enquiry-timeline-statuses"><StatusBadge status={activity.previousStatus} /><ArrowRight aria-hidden="true" /><StatusBadge status={activity.newStatus} /></div>
                    )}
                    <span>{activity.actorDisplayName} · {formatDate(activity.createdAt)}</span>
                  </div>
                </article>
              ))}
              <article className="enquiry-timeline-item">
                <div className="enquiry-timeline-icon"><Inbox aria-hidden="true" /></div>
                <div><strong>Quote request received.</strong><span>Website form · {formatDate(enquiry.createdAt)}</span></div>
              </article>
            </div>
          </section>
        </div>

        <aside className="enquiry-detail-sidebar">
          <section className="enquiry-workflow-card">
            <div className="enquiry-workflow-heading"><span className="admin-eyebrow">Enquiry workflow</span><History aria-hidden="true" /></div>
            <form onSubmit={saveWorkflow}>
              <label><span>Status</span><select value={draftStatus} onChange={(event) => { setDraftStatus(event.target.value as EnquiryStatus); setSavedMessage(''); setSaveError(''); setConflict(false) }}>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
              <label><span>Private internal notes</span><textarea value={draftNotes} onChange={(event) => { setDraftNotes(event.target.value); setSavedMessage(''); setSaveError(''); setConflict(false) }} maxLength={10000} rows={6} placeholder="Add follow-up details for staff only…" /><small>{draftNotes.length.toLocaleString('en-NZ')} / 10,000</small></label>
              {savedMessage && <div className="enquiry-workflow-success" role="status"><CheckCircle2 aria-hidden="true" /> {savedMessage}</div>}
              {saveError && <div className={`enquiry-workflow-error${conflict ? ' enquiry-workflow-error--conflict' : ''}`} role="alert"><AlertTriangle aria-hidden="true" /><span>{saveError}</span>{conflict && <button type="button" onClick={reloadLatest} disabled={saving}><RefreshCw aria-hidden="true" /> Reload latest</button>}</div>}
              <button type="submit" className="enquiry-save-button" disabled={!workflowChanged || saving}>{saving ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <Save aria-hidden="true" />}{saving ? 'Saving…' : workflowChanged ? 'Save workflow' : 'No changes to save'}</button>
            </form>
            <p className="enquiry-workflow-help">Changes are private to staff and recorded in the activity timeline.</p>
          </section>
          <section className="enquiry-contact-card">
            <span className="admin-eyebrow">Customer contact</span>
            <h2>{customerName}</h2>
            <a href={`mailto:${enquiry.email}`}><Mail aria-hidden="true" /><span>{enquiry.email}</span><ExternalLink aria-hidden="true" /></a>
            {enquiry.phone && <a href={`tel:${enquiry.phone.replace(/[^+\d]/g, '')}`}><Phone aria-hidden="true" /><span>{enquiry.phone}</span><ExternalLink aria-hidden="true" /></a>}
            {enquiry.propertyAddress && <div><MapPin aria-hidden="true" /><span>{enquiry.propertyAddress}</span></div>}
          </section>
          <section className="enquiry-meta-card">
            <dl>
              <div><dt>Reference</dt><dd>{enquiry.reference}</dd></div>
              <div><dt>Service</dt><dd>{enquiry.serviceTitle || 'Not specified'}</dd></div>
              <div><dt>Preferred contact</dt><dd>{enquiry.contactPreference === 'EITHER' ? 'Email or phone' : enquiry.contactPreference.toLowerCase()}</dd></div>
              <div><dt>Team notification</dt><dd>{enquiry.notificationSentAt ? `Sent ${formatDate(enquiry.notificationSentAt)}` : 'Not confirmed'}</dd></div>
            </dl>
          </section>
        </aside>
      </div>

      {confirmingTerminalStatus && (
        <div className="enquiry-confirm-overlay" role="presentation">
          <section className="enquiry-confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="terminal-status-title">
            <AlertTriangle aria-hidden="true" />
            <span className="admin-eyebrow">Confirm status change</span>
            <h2 id="terminal-status-title">Mark this enquiry as {statusLabels[draftStatus]}?</h2>
            <p>This will move the request out of the active follow-up workflow. The change remains visible in the activity timeline.</p>
            <div><button type="button" onClick={() => setConfirmingTerminalStatus(false)} className="enquiry-dialog-cancel">Cancel</button><button type="button" onClick={() => void saveWorkflow(undefined, true)} className="enquiry-dialog-confirm">Confirm change</button></div>
          </section>
        </div>
      )}
    </section>
  )
}

export default function AdminEnquiries({ onSessionExpired }: { onSessionExpired: () => void }) {
  const [draftFilters, setDraftFilters] = useState<AdminEnquiryFilters>(emptyFilters)
  const [filters, setFilters] = useState<AdminEnquiryFilters>(emptyFilters)
  const [pageNumber, setPageNumber] = useState(0)
  const [data, setData] = useState<AdminEnquiryPage | null>(null)
  const [selectedId, setSelectedId] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [workflowRevision, setWorkflowRevision] = useState(0)

  useEffect(() => {
    let active = true
    listAdminEnquiries(filters, pageNumber)
      .then((response) => { if (active) setData(response) })
      .catch((caught) => {
        if (!active) return
        if (caught instanceof AdminEnquiryApiError && caught.status === 401) onSessionExpired()
        else setError(caught instanceof Error ? caught.message : 'The enquiry inbox could not be loaded.')
      })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [filters, pageNumber, onSessionExpired, workflowRevision])

  function applyFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setPageNumber(0)
    setFilters({ ...draftFilters })
  }

  function clearFilters() {
    setLoading(true)
    setError('')
    setDraftFilters(emptyFilters)
    setFilters(emptyFilters)
    setPageNumber(0)
  }

  function goToPage(nextPage: number) {
    setLoading(true)
    setError('')
    setPageNumber(nextPage)
  }

  function retry() {
    setLoading(true)
    setError('')
    setFilters({ ...filters })
  }

  if (selectedId) {
    return <EnquiryDetail enquiryId={selectedId} onBack={() => setSelectedId('')} onSessionExpired={onSessionExpired} onWorkflowUpdated={() => setWorkflowRevision((revision) => revision + 1)} />
  }

  const allCount = data ? Object.values(data.statusCounts).reduce((sum, count) => sum + count, 0) : 0
  const activeCount = data ? data.statusCounts.NEW + data.statusCounts.IN_REVIEW + data.statusCounts.CONTACTED : 0
  const hasFilters = Object.values(filters).some(Boolean)

  return (
    <section className="enquiry-inbox">
      <header className="enquiry-inbox-heading">
        <div><span className="admin-eyebrow">Customer enquiries</span><h1>Quote request inbox</h1><p>Find and review every website request in one secure place.</p></div>
        <span className="enquiry-readonly-label">Workflow enabled</span>
      </header>

      <div className="enquiry-metrics" aria-label="Enquiry summary">
        <article><Inbox aria-hidden="true" /><span>New requests</span><strong>{data?.statusCounts.NEW ?? '—'}</strong></article>
        <article><SlidersHorizontal aria-hidden="true" /><span>Active follow-up</span><strong>{data ? activeCount : '—'}</strong></article>
        <article><UserRound aria-hidden="true" /><span>All enquiries</span><strong>{data ? allCount : '—'}</strong></article>
      </div>

      <form className="enquiry-filters" onSubmit={applyFilters}>
        <label className="enquiry-search-field"><span>Search</span><div><Search aria-hidden="true" /><input value={draftFilters.query} onChange={(event) => setDraftFilters({ ...draftFilters, query: event.target.value })} placeholder="Name, email, phone, address or reference" /></div></label>
        <label><span>Status</span><select value={draftFilters.status} onChange={(event) => setDraftFilters({ ...draftFilters, status: event.target.value as AdminEnquiryFilters['status'] })}><option value="">All statuses</option>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
        <label><span>Service</span><select value={draftFilters.service} onChange={(event) => setDraftFilters({ ...draftFilters, service: event.target.value })}><option value="">All services</option>{data?.services.map((service) => <option key={service.slug} value={service.slug}>{service.title}</option>)}</select></label>
        <label><span>From</span><div className="enquiry-date-field"><input type="date" value={draftFilters.from} onChange={(event) => setDraftFilters({ ...draftFilters, from: event.target.value })} /></div></label>
        <label><span>To</span><div className="enquiry-date-field"><input type="date" value={draftFilters.to} onChange={(event) => setDraftFilters({ ...draftFilters, to: event.target.value })} /></div></label>
        <div className="enquiry-filter-actions"><button type="submit">Apply filters</button>{hasFilters && <button type="button" onClick={clearFilters} className="clear"><X aria-hidden="true" /> Clear</button>}</div>
      </form>

      <div className="enquiry-results-bar"><p>{loading ? 'Loading enquiries…' : `${data?.totalElements ?? 0} ${data?.totalElements === 1 ? 'request' : 'requests'} found`}</p>{hasFilters && <span>Filtered results</span>}</div>

      {error && <div className="enquiry-state"><div className="admin-form-error" role="alert">{error}</div><button type="button" onClick={retry} className="enquiry-secondary-button">Try again</button></div>}
      {!error && loading && <div className="enquiry-state"><LoaderCircle className="admin-spinner" aria-hidden="true" /><span>Loading quote requests…</span></div>}
      {!error && !loading && data?.items.length === 0 && <div className="enquiry-empty"><Inbox aria-hidden="true" /><h2>No enquiries found</h2><p>{hasFilters ? 'Try removing one or more filters.' : 'New website quote requests will appear here.'}</p>{hasFilters && <button type="button" onClick={clearFilters} className="enquiry-secondary-button">Clear filters</button>}</div>}

      {!error && !loading && data && data.items.length > 0 && (
        <>
          <div className="enquiry-table-wrap">
            <table className="enquiry-table">
              <thead><tr><th>Customer</th><th>Service</th><th>Submitted</th><th>Photos</th><th>Status</th><th><span className="sr-only">View</span></th></tr></thead>
              <tbody>{data.items.map((item) => (
                <tr key={item.id}>
                  <td><button type="button" onClick={() => setSelectedId(item.id)}><strong>{item.firstName} {item.lastName}</strong><span>{item.email}</span><small>Ref {item.reference}</small></button></td>
                  <td><strong>{item.serviceTitle || 'General enquiry'}</strong><span>{item.propertyAddress || 'Location not supplied'}</span></td>
                  <td>{formatDate(item.createdAt)}</td>
                  <td><span className="enquiry-photo-count"><Image aria-hidden="true" /> {item.attachmentCount}</span></td>
                  <td><StatusBadge status={item.status} /></td>
                  <td><button type="button" className="enquiry-open" onClick={() => setSelectedId(item.id)} aria-label={`View enquiry from ${item.firstName} ${item.lastName}`}><ArrowRight aria-hidden="true" /></button></td>
                </tr>
              ))}</tbody>
            </table>
          </div>

          <div className="enquiry-mobile-list">{data.items.map((item) => (
            <button type="button" key={item.id} onClick={() => setSelectedId(item.id)} className="enquiry-mobile-card">
              <span className="enquiry-mobile-top"><small>Ref {item.reference}</small><StatusBadge status={item.status} /></span>
              <strong>{item.firstName} {item.lastName}</strong><span>{item.serviceTitle || 'General enquiry'}</span>
              <span className="enquiry-mobile-meta"><time>{formatDate(item.createdAt)}</time><span><Image aria-hidden="true" /> {item.attachmentCount}</span></span>
            </button>
          ))}</div>

          <nav className="enquiry-pagination" aria-label="Enquiry pages">
            <button type="button" disabled={data.page === 0} onClick={() => goToPage(Math.max(0, data.page - 1))}><ChevronLeft aria-hidden="true" /> Previous</button>
            <span>Page {data.page + 1} of {Math.max(1, data.totalPages)}</span>
            <button type="button" disabled={data.page + 1 >= data.totalPages} onClick={() => goToPage(data.page + 1)}>Next <ChevronRight aria-hidden="true" /></button>
          </nav>
        </>
      )}
    </section>
  )
}
