import { useEffect, useRef, useState } from 'react'
import type { CSSProperties } from 'react'
import { House, Paintbrush, RotateCcw, Sofa, X } from 'lucide-react'
import './ColourStudio.css'

type Scene = 'interior' | 'exterior'

const colours = [
  { name: 'Linen', hex: '#e8dfcf' },
  { name: 'Soft Clay', hex: '#c98770' },
  { name: 'Ochre', hex: '#c99a48' },
  { name: 'Sage Leaf', hex: '#98aa91' },
  { name: 'River Stone', hex: '#9ba8aa' },
  { name: 'Mist Blue', hex: '#86aebc' },
  { name: 'Lavender Grey', hex: '#a99bb9' },
  { name: 'Forest', hex: '#2f6250' },
  { name: 'Harbour', hex: '#315d70' },
  { name: 'Terracotta', hex: '#9e4f3b' },
  { name: 'Deep Navy', hex: '#203447' },
  { name: 'Charcoal', hex: '#3e4443' },
]

const defaults: Record<Scene, string> = { interior: '#e8dfcf', exterior: '#98aa91' }

export default function ColourStudio({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [scene, setScene] = useState<Scene>('interior')
  const [selectedColour, setSelectedColour] = useState(defaults.interior)
  const closeRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!open) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    closeRef.current?.focus()
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', closeOnEscape)
    }
  }, [open, onClose])

  if (!open) return null

  const selectScene = (nextScene: Scene) => {
    setScene(nextScene)
    setSelectedColour(defaults[nextScene])
  }
  const colour = colours.find((item) => item.hex === selectedColour) ?? colours[0]
  const previewStyle = { '--preview-colour': selectedColour } as CSSProperties

  return <div className="colour-studio-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
    <section className="colour-studio" role="dialog" aria-modal="true" aria-labelledby="colour-studio-title">
      <div className="colour-studio-paint-line" aria-hidden="true"><span /><span /><span /><span /><span /></div>
      <header className="colour-studio-header"><div><p>Interactive colour preview</p><h2 id="colour-studio-title">See how colour changes a space.</h2></div><button ref={closeRef} type="button" onClick={onClose} aria-label="Close colour preview"><X size={21} /></button></header>

      <div className="colour-studio-layout">
        <div className="colour-preview-column">
          <div className="scene-switcher" aria-label="Choose a preview scene"><button className={scene === 'interior' ? 'is-active' : ''} type="button" aria-pressed={scene === 'interior'} onClick={() => selectScene('interior')}><Sofa size={17} aria-hidden="true" />Interior wall</button><button className={scene === 'exterior' ? 'is-active' : ''} type="button" aria-pressed={scene === 'exterior'} onClick={() => selectScene('exterior')}><House size={17} aria-hidden="true" />Exterior body</button></div>

          <div className={`colour-preview-scene ${scene}`} style={previewStyle} aria-label={`${scene === 'interior' ? 'Interior wall' : 'Exterior house'} preview in ${colour.name}`}>
            {scene === 'interior' ? <>
              <div className="preview-interior-wall" />
              <div className="preview-interior-ceiling" />
              <div className="preview-window"><span /><span /><span /><span /></div>
              <div className="preview-art"><span /><span /></div>
              <div className="preview-sofa"><span /><span /><span /></div>
              <div className="preview-lamp"><span /><span /></div>
              <div className="preview-interior-floor" />
              <div className="preview-rug" />
            </> : <>
              <div className="preview-sky" />
              <div className="preview-cloud cloud-one" /><div className="preview-cloud cloud-two" />
              <div className="preview-house"><div className="preview-roof" /><div className="preview-facade" /><div className="preview-door" /><div className="preview-house-window window-left"><span /></div><div className="preview-house-window window-right"><span /></div><div className="preview-trim" /></div>
              <div className="preview-lawn" /><div className="preview-path" />
            </>}
            <div className="colour-preview-label"><span style={{ backgroundColor: colour.hex }} /><div><small>Previewing</small><strong>{colour.name}</strong></div></div>
          </div>
        </div>

        <div className="colour-controls">
          <div><p className="colour-controls-kicker"><Paintbrush size={16} aria-hidden="true" />Choose a colour</p><h3>{scene === 'interior' ? 'Wall colour' : 'Exterior body colour'}</h3><p>Select a swatch to update the preview instantly.</p></div>
          <div className="colour-swatch-grid" role="list" aria-label="Available preview colours">{colours.map((item) => <button className={item.hex === selectedColour ? 'is-selected' : ''} type="button" role="listitem" aria-label={`Preview ${item.name}`} aria-pressed={item.hex === selectedColour} onClick={() => setSelectedColour(item.hex)} key={item.name}><span style={{ backgroundColor: item.hex }} /><small>{item.name}</small></button>)}</div>
          <button className="colour-reset" type="button" onClick={() => setSelectedColour(defaults[scene])}><RotateCcw size={15} aria-hidden="true" />Reset preview</button>
          <p className="colour-disclaimer">For visual inspiration only. Screen settings and lighting affect colour appearance, so review a physical sample before making a final choice. Nothing is saved or submitted.</p>
        </div>
      </div>
    </section>
  </div>
}
