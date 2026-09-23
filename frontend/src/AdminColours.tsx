import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { ExternalLink, LoaderCircle, Palette, Plus, Save, Trash2 } from 'lucide-react'
import {
  AdminColourApiError,
  createAdminColour,
  deleteAdminColour,
  listAdminColours,
  updateAdminColour,
  type AdminColour,
  type SaveColourInput,
} from './api/adminColours'
import './AdminColours.css'

const emptyColour: SaveColourInput = {
  name: '', hex: '#e9e7e3', reseneUrl: '', active: true, displayOrder: 1,
  defaultInterior: false, defaultExterior: false, version: 0,
}

function fields(draft: SaveColourInput, setDraft: (value: SaveColourInput) => void) {
  return <>
    <label><span>Colour name</span><input value={draft.name} onChange={(event) => setDraft({ ...draft, name: event.target.value })} maxLength={100} required /></label>
    <label className="colour-value-field"><span>Colour value</span><div><input type="color" value={draft.hex} onChange={(event) => setDraft({ ...draft, hex: event.target.value })} aria-label="Choose colour" /><input value={draft.hex} onChange={(event) => setDraft({ ...draft, hex: event.target.value })} pattern="#[0-9A-Fa-f]{6}" maxLength={7} required /></div></label>
    <label><span>Display order</span><input type="number" min={1} max={1000} value={draft.displayOrder} onChange={(event) => setDraft({ ...draft, displayOrder: Number(event.target.value) })} required /></label>
    <label className="colour-link-field"><span>Official Resene link <small>optional</small></span><input type="url" value={draft.reseneUrl ?? ''} onChange={(event) => setDraft({ ...draft, reseneUrl: event.target.value })} maxLength={1000} placeholder="https://www.resene.co.nz/…" /></label>
    <div className="colour-options">
      <label><input type="checkbox" checked={draft.active} onChange={(event) => setDraft({ ...draft, active: event.target.checked })} /> Visible on website</label>
      <label><input type="checkbox" checked={draft.defaultInterior} disabled={!draft.active} onChange={(event) => setDraft({ ...draft, defaultInterior: event.target.checked })} /> Interior default</label>
      <label><input type="checkbox" checked={draft.defaultExterior} disabled={!draft.active} onChange={(event) => setDraft({ ...draft, defaultExterior: event.target.checked })} /> Exterior default</label>
    </div>
  </>
}

function ColourCard({ colour, onChanged, onExpired }: { colour: AdminColour; onChanged: () => Promise<void>; onExpired: () => void }) {
  const [draft, setDraft] = useState<SaveColourInput>({ ...colour, reseneUrl: colour.reseneUrl ?? '' })
  const [busy, setBusy] = useState('')
  const [error, setError] = useState('')

  function handle(caught: unknown) {
    if (caught instanceof AdminColourApiError && caught.status === 401) onExpired()
    setError(caught instanceof Error ? caught.message : 'The colour could not be updated.')
  }

  async function submit(event: FormEvent) {
    event.preventDefault(); setBusy('save'); setError('')
    try { await updateAdminColour(colour.id, draft); await onChanged() } catch (caught) { handle(caught) } finally { setBusy('') }
  }

  async function remove() {
    if (!window.confirm(`Remove ${colour.name} from the palette?`)) return
    setBusy('delete'); setError('')
    try { await deleteAdminColour(colour.id); await onChanged() } catch (caught) { handle(caught) } finally { setBusy('') }
  }

  return <form className="admin-colour-card" onSubmit={submit}>
    <div className="admin-colour-preview" style={{ backgroundColor: draft.hex }} aria-label={`${draft.name || 'Colour'} preview`} />
    <div className="admin-colour-fields">{fields(draft, setDraft)}</div>
    {error && <div className="admin-form-error" role="alert">{error}</div>}
    <footer>
      {draft.reseneUrl && <a href={draft.reseneUrl} target="_blank" rel="noreferrer">Open Resene <ExternalLink /></a>}
      <div><button type="button" className="colour-delete" onClick={remove} disabled={Boolean(busy)}>{busy === 'delete' ? <LoaderCircle className="admin-spinner" /> : <Trash2 />} Remove</button><button type="submit" className="colour-save" disabled={Boolean(busy)}>{busy === 'save' ? <LoaderCircle className="admin-spinner" /> : <Save />} Save colour</button></div>
    </footer>
  </form>
}

export default function AdminColours({ onSessionExpired }: { onSessionExpired: () => void }) {
  const [colours, setColours] = useState<AdminColour[]>([])
  const [creating, setCreating] = useState(false)
  const [draft, setDraft] = useState<SaveColourInput>(emptyColour)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function load() {
    try { setColours(await listAdminColours()) } catch (caught) {
      if (caught instanceof AdminColourApiError && caught.status === 401) onSessionExpired()
      setError(caught instanceof Error ? caught.message : 'Colours could not be loaded.')
    } finally { setLoading(false) }
  }

  useEffect(() => {
    let active = true
    listAdminColours()
      .then((response) => { if (active) setColours(response) })
      .catch((caught) => {
        if (!active) return
        if (caught instanceof AdminColourApiError && caught.status === 401) onSessionExpired()
        setError(caught instanceof Error ? caught.message : 'Colours could not be loaded.')
      })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [onSessionExpired])

  async function create(event: FormEvent) {
    event.preventDefault(); setCreating(true); setError('')
    try {
      await createAdminColour(draft)
      setDraft({ ...emptyColour, displayOrder: colours.length + 1 })
      await load()
    } catch (caught) {
      if (caught instanceof AdminColourApiError && caught.status === 401) onSessionExpired()
      setError(caught instanceof Error ? caught.message : 'The colour could not be added.')
    } finally { setCreating(false) }
  }

  if (loading) return <div className="content-loading"><LoaderCircle className="admin-spinner" /> Loading colour palette…</div>

  return <section className="admin-colours">
    <header className="admin-colours-heading"><div><span className="admin-eyebrow">Colour Studio</span><h1>Manage the public palette.</h1><p>Add, hide, reorder, or remove colours shown in the interactive preview. Only visible colours appear on the website.</p></div><Palette aria-hidden="true" /></header>
    <form className="admin-colour-create" onSubmit={create}><div><Plus aria-hidden="true" /><span><strong>Add a colour</strong><small>Use the official digital hex value supplied by Resene.</small></span></div><div className="admin-colour-fields">{fields(draft, setDraft)}</div>{error && <div className="admin-form-error" role="alert">{error}</div>}<button className="colour-save" type="submit" disabled={creating}>{creating ? <LoaderCircle className="admin-spinner" /> : <Plus />} Add to palette</button></form>
    <div className="admin-colour-list">{colours.map((colour) => <ColourCard colour={colour} onChanged={load} onExpired={onSessionExpired} key={`${colour.id}-${colour.version}`} />)}</div>
    {!colours.length && <div className="admin-colour-empty"><Palette /><h2>No colours yet</h2><p>Add the first colour using the form above.</p></div>}
  </section>
}
