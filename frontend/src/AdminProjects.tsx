import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { AlertTriangle, ArrowLeft, CheckCircle2, Clock3, Eye, EyeOff, FilePenLine, ImagePlus, LoaderCircle, MapPin, Plus, Save, Star, Trash2 } from 'lucide-react'
import type { AdminSession } from './api/adminAuth'
import {
  AdminProjectApiError,
  adminProjectImageUrl,
  createAdminProject,
  getAdminProject,
  getAdminProjectDashboard,
  removeAdminProjectImage,
  setAdminProjectPublication,
  updateAdminProject,
  uploadAdminProjectImage,
  type AdminProjectDashboard,
  type AdminProjectDetail,
  type ProjectImagePhase,
  type SaveProjectInput,
} from './api/adminProjects'
import './AdminProjects.css'

const emptyProject: SaveProjectInput = {
  title: '', summary: '', description: '', location: '', completedOn: null,
  serviceSlug: null, featured: false, version: 0,
}

function dateTime(value: string | null) {
  if (!value) return 'Not yet'
  return new Intl.DateTimeFormat('en-NZ', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Pacific/Auckland' }).format(new Date(value))
}

function statusLabel(status: string) {
  return status.charAt(0) + status.slice(1).toLowerCase()
}

export default function AdminProjects({ session, onSessionExpired }: { session: AdminSession; onSessionExpired: () => void }) {
  const [dashboard, setDashboard] = useState<AdminProjectDashboard | null>(null)
  const [selected, setSelected] = useState<AdminProjectDetail | null>(null)
  const [form, setForm] = useState<SaveProjectInput>(emptyProject)
  const [creating, setCreating] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imageAlt, setImageAlt] = useState('')
  const [imagePhase, setImagePhase] = useState<ProjectImagePhase>('AFTER')

  const handleError = useCallback((caught: unknown, fallback: string) => {
    if (caught instanceof AdminProjectApiError && caught.status === 401) onSessionExpired()
    else setError(caught instanceof Error ? caught.message : fallback)
  }, [onSessionExpired])

  const refreshDashboard = useCallback(async () => {
    const response = await getAdminProjectDashboard()
    setDashboard(response)
    return response
  }, [])

  useEffect(() => {
    let active = true
    getAdminProjectDashboard()
      .then((response) => { if (active) setDashboard(response) })
      .catch((caught) => { if (active) handleError(caught, 'Projects could not be loaded.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [handleError])

  function setProjectEditor(project: AdminProjectDetail) {
    setSelected(project)
    setForm({
      title: project.title, summary: project.summary, description: project.description,
      location: project.location, completedOn: project.completedOn,
      serviceSlug: project.serviceSlug, featured: project.featured, version: project.version,
    })
    setCreating(false)
    setError('')
    setSuccess('')
  }

  async function openProject(projectId: string) {
    setLoading(true)
    try { setProjectEditor(await getAdminProject(projectId)) }
    catch (caught) { handleError(caught, 'The project could not be loaded.') }
    finally { setLoading(false) }
  }

  function startProject() {
    setSelected(null)
    setForm(emptyProject)
    setCreating(true)
    setError('')
    setSuccess('')
  }

  function update<K extends keyof SaveProjectInput>(field: K, value: SaveProjectInput[K]) {
    setForm((current) => ({ ...current, [field]: value }))
    setError('')
    setSuccess('')
  }

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const project = selected
        ? await updateAdminProject(selected.id, { ...form, version: selected.version })
        : await createAdminProject(form)
      setProjectEditor(project)
      await refreshDashboard()
      setSuccess(selected ? 'Draft changes saved.' : 'Project draft created. You can now add images.')
    } catch (caught) { handleError(caught, 'The project could not be saved.') }
    finally { setSaving(false) }
  }

  async function changePublication(status: 'DRAFT' | 'PUBLISHED') {
    if (!selected) return
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const updated = await setAdminProjectPublication(selected, status)
      setProjectEditor(updated)
      await refreshDashboard()
      setSuccess(status === 'PUBLISHED' ? 'Project published on the customer website.' : 'Project returned to Draft.')
    } catch (caught) { handleError(caught, 'Publication status could not be changed.') }
    finally { setSaving(false) }
  }

  function chooseImage(event: ChangeEvent<HTMLInputElement>) {
    setImageFile(event.target.files?.[0] ?? null)
    setError('')
  }

  async function uploadImage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected || !imageFile) return
    setSaving(true)
    setError('')
    try {
      const updated = await uploadAdminProjectImage(selected.id, imageFile, imageAlt, imagePhase)
      setProjectEditor(updated)
      await refreshDashboard()
      setImageFile(null)
      setImageAlt('')
      setImagePhase('AFTER')
      setSuccess('Project image added.')
    } catch (caught) { handleError(caught, 'The image could not be uploaded.') }
    finally { setSaving(false) }
  }

  async function removeImage(imageId: string) {
    if (!selected || !window.confirm('Remove this project image?')) return
    setSaving(true)
    try {
      const updated = await removeAdminProjectImage(selected.id, imageId)
      setProjectEditor(updated)
      await refreshDashboard()
      setSuccess('Project image removed.')
    } catch (caught) { handleError(caught, 'The image could not be removed.') }
    finally { setSaving(false) }
  }

  const publishedCount = useMemo(() => dashboard?.projects.filter((project) => project.status === 'PUBLISHED').length ?? 0, [dashboard])
  const editorOpen = creating || selected !== null
  const editable = !selected || selected.status === 'DRAFT'

  if (loading && !dashboard) return <div className="content-loading"><LoaderCircle className="admin-spinner" aria-hidden="true" /> Loading projects…</div>

  return <section className="admin-projects">
    <header className="content-page-heading">
      <div><span className="admin-eyebrow">Website content</span><h1>Project portfolio</h1><p>Prepare project stories and control what customers see.</p></div>
      <div className="content-heading-actions"><span><strong>{publishedCount}</strong> published</span><button type="button" onClick={startProject}><Plus aria-hidden="true" /> New project</button></div>
    </header>

    {error && <div className="content-message error" role="alert"><AlertTriangle aria-hidden="true" />{error}</div>}
    {success && <div className="content-message success" role="status"><CheckCircle2 aria-hidden="true" />{success}</div>}

    {!editorOpen ? <div className="content-project-list">
      {dashboard?.projects.map((project) => <button type="button" onClick={() => void openProject(project.id)} key={project.id}>
        <div className="content-project-thumb">{project.imageUrl ? <img src={adminProjectImageUrl(project.imageUrl)} alt="" /> : <ImagePlus aria-hidden="true" />}</div>
        <div className="content-project-title"><span>{project.serviceTitle || 'Painting project'}</span><h2>{project.title}</h2><p><MapPin aria-hidden="true" />{project.location}</p></div>
        {project.featured && <Star className="content-featured" aria-label="Featured" />}
        <span className={`content-status ${project.status.toLowerCase()}`}>{statusLabel(project.status)}</span>
        <span className="content-updated">Updated {dateTime(project.updatedAt)}</span>
        <FilePenLine aria-hidden="true" />
      </button>)}
    </div> : <div className="content-editor-shell">
      <div className="content-editor-topbar">
        <button type="button" onClick={() => { setSelected(null); setCreating(false); setError(''); setSuccess('') }}><ArrowLeft aria-hidden="true" /> All projects</button>
        {selected && <div><span className={`content-status ${selected.status.toLowerCase()}`}>{statusLabel(selected.status)}</span>{selected.status === 'PUBLISHED' && <a href={`/projects/#${selected.slug}`} target="_blank" rel="noreferrer"><Eye aria-hidden="true" /> View live</a>}</div>}
      </div>

      <div className="content-editor-grid">
        <form className="project-content-form" onSubmit={save}>
          <header><div><span className="admin-eyebrow">{selected ? 'Project editor' : 'New project'}</span><h2>{selected?.title || 'Create a project draft'}</h2></div>{selected?.featured && <span className="featured-label"><Star aria-hidden="true" /> Featured</span>}</header>
          {!editable && <div className="content-lock-note"><Eye aria-hidden="true" /><p><strong>This project is live.</strong> An Owner must return it to Draft before its content or images can change.</p></div>}
          <div className="project-form-grid">
            <label className="span-two"><span>Project title *</span><input value={form.title} onChange={(event) => update('title', event.target.value)} maxLength={180} required disabled={!editable} /></label>
            <label><span>Service</span><select value={form.serviceSlug ?? ''} onChange={(event) => update('serviceSlug', event.target.value || null)} disabled={!editable}><option value="">General painting project</option>{dashboard?.services.map((service) => <option value={service.slug} key={service.slug}>{service.title}</option>)}</select></label>
            <label><span>Location *</span><input value={form.location} onChange={(event) => update('location', event.target.value)} maxLength={150} required disabled={!editable} /></label>
            <label><span>Completion date</span><input type="date" value={form.completedOn ?? ''} onChange={(event) => update('completedOn', event.target.value || null)} disabled={!editable} /></label>
            <label className="featured-check"><input type="checkbox" checked={form.featured} onChange={(event) => update('featured', event.target.checked)} disabled={!editable} /><span><strong>Feature this project</strong><small>Featured projects appear first.</small></span></label>
            <label className="span-two"><span>Short summary *</span><textarea value={form.summary} onChange={(event) => update('summary', event.target.value)} maxLength={600} rows={3} required disabled={!editable} /><small>{form.summary.length}/600 characters</small></label>
            <label className="span-two"><span>Project highlights *</span><textarea value={form.description} onChange={(event) => update('description', event.target.value)} maxLength={10000} rows={5} required disabled={!editable} /><small>Put each public highlight on a new line.</small></label>
          </div>
          {editable && <button type="submit" disabled={saving}>{saving ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <Save aria-hidden="true" />}{selected ? 'Save draft changes' : 'Create project draft'}</button>}
        </form>

        <aside className="project-editor-side">
          {selected ? <>
            <section className="project-publication-card"><span className="admin-eyebrow">Publication</span><h2>{selected.status === 'PUBLISHED' ? 'Visible to customers' : 'Private draft'}</h2><p>{selected.status === 'PUBLISHED' ? `Published ${dateTime(selected.publishedAt)}` : 'Only signed-in staff can view this project.'}</p>{session.role === 'OWNER' ? <button type="button" onClick={() => void changePublication(selected.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED')} disabled={saving}>{selected.status === 'PUBLISHED' ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}{selected.status === 'PUBLISHED' ? 'Return to Draft' : 'Publish project'}</button> : <small>Only an Owner can change publication status.</small>}</section>

            <section className="project-images-card"><header><div><span className="admin-eyebrow">Project gallery</span><h2>Images</h2></div><strong>{selected.images.length}/8</strong></header><div className="project-image-list">{selected.images.map((image) => <article key={image.id}><img src={adminProjectImageUrl(image.imageUrl)} alt={image.altText} /><div><span>{image.phase}</span><p>{image.altText}</p></div>{editable && <button type="button" onClick={() => void removeImage(image.id)} aria-label={`Remove ${image.originalFilename}`}><Trash2 aria-hidden="true" /></button>}</article>)}</div>{editable && selected.images.length < 8 && <form onSubmit={uploadImage}><label className="project-file-picker"><ImagePlus aria-hidden="true" /><span>{imageFile ? imageFile.name : 'Choose a project image'}</span><input type="file" accept="image/jpeg,image/png,image/webp,image/heic,image/heif" onChange={chooseImage} required /></label><label><span>Image description *</span><input value={imageAlt} onChange={(event) => setImageAlt(event.target.value)} maxLength={250} required placeholder="Describe what is visible" /></label><label><span>Image type</span><select value={imagePhase} onChange={(event) => setImagePhase(event.target.value as ProjectImagePhase)}><option value="AFTER">After</option><option value="BEFORE">Before</option><option value="GALLERY">Gallery</option></select></label><button type="submit" disabled={!imageFile || !imageAlt.trim() || saving}><ImagePlus aria-hidden="true" /> Add image</button><small>JPEG, PNG, WebP, HEIC or HEIF · 5 MB maximum</small></form>}</section>

            <section className="project-history-card"><header><Clock3 aria-hidden="true" /><div><span className="admin-eyebrow">Audit history</span><h2>Recent activity</h2></div></header>{selected.activities.length ? selected.activities.map((activity) => <article key={activity.id}><p>{activity.summary}</p><span>{dateTime(activity.createdAt)}</span></article>) : <p>No recorded changes yet.</p>}</section>
          </> : <section className="project-draft-help"><FilePenLine aria-hidden="true" /><h2>Start privately</h2><p>The project begins as a Draft. After saving, add at least one image and ask an Owner to publish it.</p></section>}
        </aside>
      </div>
    </div>}
  </section>
}
