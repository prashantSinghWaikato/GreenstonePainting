import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { AlertTriangle, ArrowLeft, Check, CheckCircle2, Clock3, Eye, EyeOff, ImagePlus, LoaderCircle, Paintbrush, Save, Trash2, X } from 'lucide-react'
import type { AdminSession } from './api/adminAuth'
import {
  AdminServiceApiError,
  adminServiceImageUrl,
  getAdminService,
  listAdminServices,
  removeAdminServiceImage,
  setAdminServicePublication,
  updateAdminService,
  uploadAdminServiceImage,
  type AdminServiceDetail,
  type AdminServiceSummary,
  type SaveServiceInput,
} from './api/adminServices'
import './AdminServices.css'

function dateTime(value: string | null) {
  if (!value) return 'Not yet'
  return new Intl.DateTimeFormat('en-NZ', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Pacific/Auckland' }).format(new Date(value))
}

function statusLabel(status: string) { return status.charAt(0) + status.slice(1).toLowerCase() }

function toForm(service: AdminServiceDetail): SaveServiceInput {
  return {
    title: service.title, label: service.label, summary: service.summary, description: service.description,
    inclusions: service.inclusions, note: service.note, displayOrder: service.displayOrder, version: service.version,
  }
}

function ServicePreview({ form, imageUrl, imageAlt, slug, onClose }: { form: SaveServiceInput; imageUrl: string | null; imageAlt: string; slug: string; onClose: () => void }) {
  return <div className="service-preview-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <section className="service-preview-dialog" role="dialog" aria-modal="true" aria-labelledby="service-preview-title">
      <header><div><span className="admin-eyebrow">Customer view preview</span><h2 id="service-preview-title">{form.title}</h2></div><button type="button" onClick={onClose} aria-label="Close service preview"><X aria-hidden="true" /></button></header>
      <div className="service-preview-layout">
        <div className="service-preview-media">{imageUrl ? <img src={adminServiceImageUrl(imageUrl)} alt={imageAlt} /> : <div><ImagePlus aria-hidden="true" /><span>Add a featured image before publishing</span></div>}<span>{form.label}</span></div>
        <div className="service-preview-copy"><p className="admin-eyebrow">{form.label}</p><h2>{form.title}</h2><p>{form.description}</p><ul>{form.inclusions.filter(Boolean).map((item) => <li key={item}><Check aria-hidden="true" />{item}</li>)}</ul><aside>{form.note}</aside><small>/services/#{slug}</small></div>
      </div>
    </section>
  </div>
}

export default function AdminServices({ session, onSessionExpired }: { session: AdminSession; onSessionExpired: () => void }) {
  const [services, setServices] = useState<AdminServiceSummary[]>([])
  const [selected, setSelected] = useState<AdminServiceDetail | null>(null)
  const [form, setForm] = useState<SaveServiceInput | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imageAlt, setImageAlt] = useState('')
  const [previewOpen, setPreviewOpen] = useState(false)

  const handleError = useCallback((caught: unknown, fallback: string) => {
    if (caught instanceof AdminServiceApiError && caught.status === 401) onSessionExpired()
    else setError(caught instanceof Error ? caught.message : fallback)
  }, [onSessionExpired])

  const refresh = useCallback(async () => {
    const response = await listAdminServices()
    setServices(response)
  }, [])

  useEffect(() => {
    let active = true
    listAdminServices().then((response) => { if (active) setServices(response) })
      .catch((caught) => { if (active) handleError(caught, 'Services could not be loaded.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [handleError])

  function setEditor(service: AdminServiceDetail) {
    setSelected(service)
    setForm(toForm(service))
    setImageAlt(service.imageAlt ?? '')
    setImageFile(null)
    setError('')
    setSuccess('')
  }

  async function openService(id: string) {
    setLoading(true)
    try { setEditor(await getAdminService(id)) }
    catch (caught) { handleError(caught, 'The service could not be loaded.') }
    finally { setLoading(false) }
  }

  function update<K extends keyof SaveServiceInput>(field: K, value: SaveServiceInput[K]) {
    setForm((current) => current ? { ...current, [field]: value } : current)
    setError('')
    setSuccess('')
  }

  function updateInclusion(index: number, value: string) {
    if (!form) return
    update('inclusions', form.inclusions.map((item, itemIndex) => itemIndex === index ? value : item))
  }

  function addInclusion() {
    if (form && form.inclusions.length < 12) update('inclusions', [...form.inclusions, ''])
  }

  function removeInclusion(index: number) {
    if (form && form.inclusions.length > 1) update('inclusions', form.inclusions.filter((_, itemIndex) => itemIndex !== index))
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected || !form) return
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const updated = await updateAdminService(selected.id, { ...form, inclusions: form.inclusions.map((item) => item.trim()).filter(Boolean), version: selected.version })
      setEditor(updated)
      await refresh()
      setSuccess('Service draft changes saved.')
    } catch (caught) { handleError(caught, 'The service could not be saved.') }
    finally { setSaving(false) }
  }

  async function changePublication(status: 'DRAFT' | 'PUBLISHED') {
    if (!selected) return
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const updated = await setAdminServicePublication(selected, status)
      setEditor(updated)
      await refresh()
      setSuccess(status === 'PUBLISHED' ? 'Service published on the customer website.' : 'Service hidden and returned to Draft.')
    } catch (caught) { handleError(caught, 'Publication status could not be changed.') }
    finally { setSaving(false) }
  }

  function chooseImage(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null
    if (file && file.size > 5 * 1024 * 1024) {
      event.target.value = ''
      setImageFile(null)
      setError('The featured image must be 5 MB or smaller.')
      return
    }
    setImageFile(file)
    setError('')
  }

  async function uploadImage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected || !imageFile) return
    setSaving(true)
    setError('')
    try {
      const updated = await uploadAdminServiceImage(selected.id, imageFile, imageAlt)
      setEditor(updated)
      await refresh()
      setSuccess('Featured image saved.')
    } catch (caught) { handleError(caught, 'The image could not be uploaded.') }
    finally { setSaving(false) }
  }

  async function removeImage() {
    if (!selected || !window.confirm('Remove this featured image?')) return
    setSaving(true)
    setError('')
    try {
      const updated = await removeAdminServiceImage(selected.id)
      setEditor(updated)
      await refresh()
      setSuccess('Featured image removed.')
    } catch (caught) { handleError(caught, 'The image could not be removed.') }
    finally { setSaving(false) }
  }

  const publishedCount = useMemo(() => services.filter((service) => service.status === 'PUBLISHED').length, [services])
  const editable = selected?.status === 'DRAFT'

  if (loading && !services.length) return <div className="content-loading"><LoaderCircle className="admin-spinner" aria-hidden="true" /> Loading services…</div>

  return <section className="admin-services">
    <header className="content-page-heading"><div><span className="admin-eyebrow">Website content</span><h1>Painting services</h1><p>Keep service information accurate and control what customers can request.</p></div><div className="content-heading-actions"><span><strong>{publishedCount}</strong> published</span></div></header>
    {error && <div className="content-message error" role="alert"><AlertTriangle aria-hidden="true" />{error}</div>}
    {success && <div className="content-message success" role="status"><CheckCircle2 aria-hidden="true" />{success}</div>}

    {!selected || !form ? <div className="content-project-list service-admin-list">{services.map((service) => <button type="button" onClick={() => void openService(service.id)} key={service.id}><div className="content-project-thumb">{service.imageUrl ? <img src={adminServiceImageUrl(service.imageUrl)} alt="" /> : <ImagePlus aria-hidden="true" />}</div><div className="content-project-title"><span>Position {service.displayOrder}</span><h2>{service.title}</h2><p>{service.label}</p></div><span className={`content-status ${service.status.toLowerCase()}`}>{statusLabel(service.status)}</span><span className="content-updated">Updated {dateTime(service.updatedAt)}</span><Paintbrush aria-hidden="true" /></button>)}</div> : <div className="content-editor-shell">
      <div className="content-editor-topbar"><button type="button" onClick={() => { setSelected(null); setForm(null); setError(''); setSuccess('') }}><ArrowLeft aria-hidden="true" /> All services</button><div><button type="button" className="article-preview-trigger" onClick={() => setPreviewOpen(true)}><Eye aria-hidden="true" /> Preview</button><span className={`content-status ${selected.status.toLowerCase()}`}>{statusLabel(selected.status)}</span>{selected.status === 'PUBLISHED' && <a href={`/services/#${selected.slug}`} target="_blank" rel="noreferrer"><Eye aria-hidden="true" /> View live</a>}</div></div>
      <div className="content-editor-grid">
        <form className="project-content-form service-content-form" onSubmit={save}>
          <header><div><span className="admin-eyebrow">Service editor</span><h2>{selected.title}</h2><small>Stable URL: /services/#{selected.slug}</small></div></header>
          {!editable && <div className="content-lock-note"><Eye aria-hidden="true" /><p><strong>This service is live.</strong> An Owner must return it to Draft before its content, order, or image can change.</p></div>}
          <div className="project-form-grid">
            <label><span>Service title *</span><input value={form.title} onChange={(event) => update('title', event.target.value)} maxLength={150} required disabled={!editable} /></label>
            <label><span>Section label *</span><input value={form.label} onChange={(event) => update('label', event.target.value)} maxLength={100} required disabled={!editable} /></label>
            <label><span>Display order *</span><input type="number" min="1" max="100" value={form.displayOrder} onChange={(event) => update('displayOrder', Number(event.target.value))} required disabled={!editable} /></label>
            <label className="span-two"><span>Overview summary *</span><textarea value={form.summary} onChange={(event) => update('summary', event.target.value)} maxLength={500} rows={3} required disabled={!editable} /><small>{form.summary.length}/500 characters · Used on homepage and overview cards.</small></label>
            <label className="span-two"><span>Detailed description *</span><textarea value={form.description} onChange={(event) => update('description', event.target.value)} maxLength={10000} rows={6} required disabled={!editable} /></label>
            <fieldset className="span-two service-inclusions"><legend>What is included *</legend>{form.inclusions.map((item, index) => <div key={index}><input value={item} onChange={(event) => updateInclusion(index, event.target.value)} maxLength={180} required disabled={!editable} aria-label={`Service inclusion ${index + 1}`} />{editable && form.inclusions.length > 1 && <button type="button" onClick={() => removeInclusion(index)} aria-label={`Remove inclusion ${index + 1}`}><X aria-hidden="true" /></button>}</div>)}{editable && form.inclusions.length < 12 && <button type="button" className="service-add-inclusion" onClick={addInclusion}>+ Add inclusion</button>}</fieldset>
            <label className="span-two"><span>Planning note *</span><textarea value={form.note} onChange={(event) => update('note', event.target.value)} maxLength={500} rows={3} required disabled={!editable} /><small>{form.note.length}/500 characters</small></label>
          </div>
          {editable && <button type="submit" disabled={saving}>{saving ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <Save aria-hidden="true" />}Save draft changes</button>}
        </form>

        <aside className="project-editor-side">
          <section className="project-publication-card"><span className="admin-eyebrow">Publication</span><h2>{selected.status === 'PUBLISHED' ? 'Visible to customers' : 'Hidden draft'}</h2><p>{selected.status === 'PUBLISHED' ? `Published ${dateTime(selected.publishedAt)}` : 'This service is hidden from public service lists and new quote requests.'}</p>{session.role === 'OWNER' ? <button type="button" onClick={() => void changePublication(selected.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED')} disabled={saving}>{selected.status === 'PUBLISHED' ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}{selected.status === 'PUBLISHED' ? 'Hide and edit service' : 'Publish service'}</button> : <small>Only an Owner can change publication status.</small>}</section>
          <section className="project-images-card article-image-card"><header><div><span className="admin-eyebrow">Service media</span><h2>Featured image</h2></div></header>{selected.imageUrl && <div className="article-current-image"><img src={adminServiceImageUrl(selected.imageUrl)} alt={selected.imageAlt ?? ''} />{editable && <button type="button" onClick={() => void removeImage()} aria-label="Remove featured image"><Trash2 aria-hidden="true" /></button>}<p>{selected.imageAlt}</p></div>}{editable && <form onSubmit={uploadImage}><label className="project-file-picker"><ImagePlus aria-hidden="true" /><span>{imageFile ? imageFile.name : selected.imageUrl ? 'Choose a replacement image' : 'Choose a featured image'}</span><input type="file" accept="image/jpeg,image/png,image/webp,image/heic,image/heif" onChange={chooseImage} required /></label><label><span>Image description *</span><input value={imageAlt} onChange={(event) => setImageAlt(event.target.value)} maxLength={250} required /></label><button type="submit" disabled={!imageFile || !imageAlt.trim() || saving}><ImagePlus aria-hidden="true" />{selected.imageUrl ? 'Replace image' : 'Add image'}</button><small>JPEG, PNG, WebP, HEIC or HEIF · 5 MB maximum</small></form>}</section>
          <section className="project-history-card"><header><Clock3 aria-hidden="true" /><div><span className="admin-eyebrow">Audit history</span><h2>Recent activity</h2></div></header>{selected.activities.length ? selected.activities.map((activity) => <article key={activity.id}><p>{activity.summary}</p><span>{dateTime(activity.createdAt)}</span></article>) : <p>No recorded changes yet.</p>}</section>
        </aside>
      </div>
    </div>}
    {previewOpen && selected && form && <ServicePreview form={form} imageUrl={selected.imageUrl} imageAlt={imageAlt} slug={selected.slug} onClose={() => setPreviewOpen(false)} />}
  </section>
}
