import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { AlertTriangle, ArrowLeft, CheckCircle2, Clock3, Eye, EyeOff, FilePenLine, ImagePlus, LoaderCircle, Plus, Save, Trash2, X } from 'lucide-react'
import type { AdminSession } from './api/adminAuth'
import {
  AdminArticleApiError,
  adminArticleImageUrl,
  createAdminArticle,
  getAdminArticle,
  listAdminArticles,
  removeAdminArticleImage,
  setAdminArticlePublication,
  updateAdminArticle,
  uploadAdminArticleImage,
  type AdminArticleDetail,
  type AdminArticleSummary,
  type SaveArticleInput,
} from './api/adminArticles'
import { parseArticleBody } from './articleContent'
import './AdminArticles.css'

const emptyArticle: SaveArticleInput = {
  title: '', shortTitle: '', topic: '', excerpt: '', body: '', readTimeMinutes: 5, version: 0,
}

function dateTime(value: string | null) {
  if (!value) return 'Not yet'
  return new Intl.DateTimeFormat('en-NZ', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'Pacific/Auckland' }).format(new Date(value))
}

function statusLabel(status: string) {
  return status.charAt(0) + status.slice(1).toLowerCase()
}

function ArticlePreview({ form, imageUrl, onClose }: { form: SaveArticleInput; imageUrl: string | null; onClose: () => void }) {
  const blocks = parseArticleBody(form.body)
  return <div className="article-preview-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <section className="article-preview-dialog" role="dialog" aria-modal="true" aria-labelledby="article-preview-title">
      <header><div><span className="admin-eyebrow">Customer view preview</span><h2 id="article-preview-title">{form.title || 'Untitled article'}</h2></div><button type="button" onClick={onClose} aria-label="Close article preview"><X aria-hidden="true" /></button></header>
      {imageUrl ? <img className="article-preview-image" src={adminArticleImageUrl(imageUrl)} alt="" /> : <div className="article-preview-image empty"><ImagePlus aria-hidden="true" /><span>Add a featured image before publishing</span></div>}
      <div className="article-preview-copy">
        <p className="article-preview-meta"><span>{form.topic || 'Article category'}</span><span>{form.readTimeMinutes} min read</span></p>
        <p className="article-preview-excerpt">{form.excerpt || 'Your article summary will appear here.'}</p>
        {blocks.length ? blocks.map((block, index) => block.type === 'heading'
          ? <h3 key={`${block.type}-${index}`}>{block.text}</h3>
          : block.type === 'paragraph'
            ? <p key={`${block.type}-${index}`}>{block.text}</p>
            : <ul key={`${block.type}-${index}`}>{block.items.map((item) => <li key={item}>{item}</li>)}</ul>)
          : <p className="article-preview-empty">Write article content to preview it.</p>}
      </div>
    </section>
  </div>
}

export default function AdminArticles({ session, onSessionExpired }: { session: AdminSession; onSessionExpired: () => void }) {
  const [articles, setArticles] = useState<AdminArticleSummary[]>([])
  const [selected, setSelected] = useState<AdminArticleDetail | null>(null)
  const [form, setForm] = useState<SaveArticleInput>(emptyArticle)
  const [creating, setCreating] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imageAlt, setImageAlt] = useState('')
  const [previewOpen, setPreviewOpen] = useState(false)

  const handleError = useCallback((caught: unknown, fallback: string) => {
    if (caught instanceof AdminArticleApiError && caught.status === 401) onSessionExpired()
    else setError(caught instanceof Error ? caught.message : fallback)
  }, [onSessionExpired])

  const refresh = useCallback(async () => {
    const response = await listAdminArticles()
    setArticles(response)
    return response
  }, [])

  useEffect(() => {
    let active = true
    listAdminArticles()
      .then((response) => { if (active) setArticles(response) })
      .catch((caught) => { if (active) handleError(caught, 'Articles could not be loaded.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [handleError])

  function setEditor(article: AdminArticleDetail) {
    setSelected(article)
    setForm({ title: article.title, shortTitle: article.shortTitle, topic: article.topic, excerpt: article.excerpt, body: article.body, readTimeMinutes: article.readTimeMinutes, version: article.version })
    setCreating(false)
    setImageFile(null)
    setImageAlt(article.imageAlt ?? '')
    setError('')
    setSuccess('')
  }

  async function openArticle(id: string) {
    setLoading(true)
    try { setEditor(await getAdminArticle(id)) }
    catch (caught) { handleError(caught, 'The article could not be loaded.') }
    finally { setLoading(false) }
  }

  function startArticle() {
    setSelected(null)
    setForm(emptyArticle)
    setCreating(true)
    setImageFile(null)
    setImageAlt('')
    setError('')
    setSuccess('')
  }

  function update<K extends keyof SaveArticleInput>(field: K, value: SaveArticleInput[K]) {
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
      const article = selected
        ? await updateAdminArticle(selected.id, { ...form, version: selected.version })
        : await createAdminArticle(form)
      setEditor(article)
      await refresh()
      setSuccess(selected ? 'Draft changes saved.' : 'Article draft created. You can now add its featured image.')
    } catch (caught) { handleError(caught, 'The article could not be saved.') }
    finally { setSaving(false) }
  }

  async function changePublication(status: 'DRAFT' | 'PUBLISHED') {
    if (!selected) return
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const updated = await setAdminArticlePublication(selected, status)
      setEditor(updated)
      await refresh()
      setSuccess(status === 'PUBLISHED' ? 'Article published on the customer website.' : 'Article returned to Draft.')
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
      const updated = await uploadAdminArticleImage(selected.id, imageFile, imageAlt)
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
      const updated = await removeAdminArticleImage(selected.id)
      setEditor(updated)
      await refresh()
      setSuccess('Featured image removed.')
    } catch (caught) { handleError(caught, 'The image could not be removed.') }
    finally { setSaving(false) }
  }

  const publishedCount = useMemo(() => articles.filter((article) => article.status === 'PUBLISHED').length, [articles])
  const editorOpen = creating || selected !== null
  const editable = !selected || selected.status === 'DRAFT'

  if (loading && !articles.length && !editorOpen) return <div className="content-loading"><LoaderCircle className="admin-spinner" aria-hidden="true" /> Loading articles…</div>

  return <section className="admin-articles">
    <header className="content-page-heading">
      <div><span className="admin-eyebrow">Website content</span><h1>Blog journal</h1><p>Write useful painting guidance and control when it appears publicly.</p></div>
      <div className="content-heading-actions"><span><strong>{publishedCount}</strong> published</span><button type="button" onClick={startArticle}><Plus aria-hidden="true" /> New article</button></div>
    </header>

    {error && <div className="content-message error" role="alert"><AlertTriangle aria-hidden="true" />{error}</div>}
    {success && <div className="content-message success" role="status"><CheckCircle2 aria-hidden="true" />{success}</div>}

    {!editorOpen ? <div className="content-project-list article-admin-list">
      {articles.map((article) => <button type="button" onClick={() => void openArticle(article.id)} key={article.id}>
        <div className="content-project-thumb">{article.imageUrl ? <img src={adminArticleImageUrl(article.imageUrl)} alt="" /> : <ImagePlus aria-hidden="true" />}</div>
        <div className="content-project-title"><span>{article.topic}</span><h2>{article.title}</h2><p>/{article.slug}/</p></div>
        <span className={`content-status ${article.status.toLowerCase()}`}>{statusLabel(article.status)}</span>
        <span className="content-updated">Updated {dateTime(article.updatedAt)}</span>
        <FilePenLine aria-hidden="true" />
      </button>)}
      {!articles.length && <div className="article-empty-state"><FilePenLine aria-hidden="true" /><h2>No articles yet</h2><p>Create a private draft to begin the journal.</p></div>}
    </div> : <div className="content-editor-shell">
      <div className="content-editor-topbar">
        <button type="button" onClick={() => { setSelected(null); setCreating(false); setError(''); setSuccess('') }}><ArrowLeft aria-hidden="true" /> All articles</button>
        <div><button type="button" className="article-preview-trigger" onClick={() => setPreviewOpen(true)}><Eye aria-hidden="true" /> Preview</button>{selected && <span className={`content-status ${selected.status.toLowerCase()}`}>{statusLabel(selected.status)}</span>}{selected?.status === 'PUBLISHED' && <a href={`/${selected.slug}/`} target="_blank" rel="noreferrer"><Eye aria-hidden="true" /> View live</a>}</div>
      </div>

      <div className="content-editor-grid">
        <form className="project-content-form article-content-form" onSubmit={save}>
          <header><div><span className="admin-eyebrow">{selected ? 'Article editor' : 'New article'}</span><h2>{selected?.title || 'Create an article draft'}</h2></div></header>
          {!editable && <div className="content-lock-note"><Eye aria-hidden="true" /><p><strong>This article is live.</strong> An Owner must return it to Draft before its content or image can change.</p></div>}
          <div className="project-form-grid">
            <label className="span-two"><span>Article title *</span><input value={form.title} onChange={(event) => update('title', event.target.value)} maxLength={180} required disabled={!editable} /><small>{form.title.length}/180 characters</small></label>
            <label><span>Short title *</span><input value={form.shortTitle} onChange={(event) => update('shortTitle', event.target.value)} maxLength={120} required disabled={!editable} /></label>
            <label><span>Category *</span><input value={form.topic} onChange={(event) => update('topic', event.target.value)} maxLength={100} required disabled={!editable} placeholder="Preparation, materials, care guide…" /></label>
            <label><span>Estimated read time *</span><div className="article-number-field"><input type="number" min="1" max="60" value={form.readTimeMinutes} onChange={(event) => update('readTimeMinutes', Number(event.target.value))} required disabled={!editable} /><span>minutes</span></div></label>
            <label className="span-two"><span>Article summary *</span><textarea value={form.excerpt} onChange={(event) => update('excerpt', event.target.value)} maxLength={600} rows={4} required disabled={!editable} /><small>{form.excerpt.length}/600 characters · Used on article cards and beside the article.</small></label>
            <label className="span-two"><span>Article content *</span><textarea className="article-body-input" value={form.body} onChange={(event) => update('body', event.target.value)} maxLength={20000} rows={18} required disabled={!editable} placeholder={'Start with an introduction.\n\n## Add a section heading\nWrite the section here.\n\n- Add a bullet point'} /><small>{form.body.length}/20,000 characters · Use <strong>## Heading</strong> for section titles and <strong>- item</strong> for bullet points.</small></label>
          </div>
          {editable && <button type="submit" disabled={saving}>{saving ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <Save aria-hidden="true" />}{selected ? 'Save draft changes' : 'Create article draft'}</button>}
        </form>

        <aside className="project-editor-side">
          {selected ? <>
            <section className="project-publication-card"><span className="admin-eyebrow">Publication</span><h2>{selected.status === 'PUBLISHED' ? 'Visible to customers' : 'Private draft'}</h2><p>{selected.status === 'PUBLISHED' ? `Published ${dateTime(selected.publishedAt)}` : 'Only signed-in staff can view this article.'}</p>{session.role === 'OWNER' ? <button type="button" onClick={() => void changePublication(selected.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED')} disabled={saving}>{selected.status === 'PUBLISHED' ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}{selected.status === 'PUBLISHED' ? 'Return to Draft' : 'Publish article'}</button> : <small>Only an Owner can change publication status.</small>}</section>

            <section className="project-images-card article-image-card"><header><div><span className="admin-eyebrow">Article media</span><h2>Featured image</h2></div></header>{selected.imageUrl && <div className="article-current-image"><img src={adminArticleImageUrl(selected.imageUrl)} alt={selected.imageAlt ?? ''} />{editable && <button type="button" onClick={() => void removeImage()} aria-label="Remove featured image"><Trash2 aria-hidden="true" /></button>}<p>{selected.imageAlt}</p></div>}{editable && <form onSubmit={uploadImage}><label className="project-file-picker"><ImagePlus aria-hidden="true" /><span>{imageFile ? imageFile.name : selected.imageUrl ? 'Choose a replacement image' : 'Choose a featured image'}</span><input type="file" accept="image/jpeg,image/png,image/webp,image/heic,image/heif" onChange={chooseImage} required /></label><label><span>Image description *</span><input value={imageAlt} onChange={(event) => setImageAlt(event.target.value)} maxLength={250} required placeholder="Describe what is visible" /></label><button type="submit" disabled={!imageFile || !imageAlt.trim() || saving}><ImagePlus aria-hidden="true" /> {selected.imageUrl ? 'Replace image' : 'Add image'}</button><small>JPEG, PNG, WebP, HEIC or HEIF · 5 MB maximum</small></form>}</section>

            <section className="project-history-card"><header><Clock3 aria-hidden="true" /><div><span className="admin-eyebrow">Audit history</span><h2>Recent activity</h2></div></header>{selected.activities.length ? selected.activities.map((activity) => <article key={activity.id}><p>{activity.summary}</p><span>{dateTime(activity.createdAt)}</span></article>) : <p>No recorded changes yet.</p>}</section>
          </> : <section className="project-draft-help"><FilePenLine aria-hidden="true" /><h2>Start privately</h2><p>The article begins as a Draft. Save the content, add a featured image, preview it, then ask an Owner to publish it.</p></section>}
        </aside>
      </div>
    </div>}
    {previewOpen && <ArticlePreview form={form} imageUrl={selected?.imageUrl ?? null} onClose={() => setPreviewOpen(false)} />}
  </section>
}
