import { useEffect, useRef, useState } from 'react'
import { projectPhotoGroups } from './data/projectPhotos'
import './ProjectPhotoGallery.css'

export default function ProjectPhotoGallery() {
  const [selected, setSelected] = useState<{ src: string; alt: string } | null>(null)
  const dialog = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    if (!selected) return
    dialog.current?.showModal()
    const overflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => { document.body.style.overflow = overflow }
  }, [selected])

  return <section className="project-photo-gallery section" aria-labelledby="photo-gallery-heading">
    <div className="page-container">
      <p className="eyebrow">Our work in pictures</p>
      <h2 id="photo-gallery-heading">From preparation to the finish.</h2>
      <p className="photo-gallery-intro">Explore commercial projects, feature walls, new builds, renovation and staining. Select a photo for a closer look.</p>
      <nav className="photo-category-links" aria-label="Project photo categories">
        {projectPhotoGroups.map((group) => <a key={group.slug} href={`#photos-${group.slug}`}>{group.title}</a>)}
      </nav>
      {projectPhotoGroups.map((group) => <section className="photo-category" id={`photos-${group.slug}`} key={group.slug} aria-labelledby={`heading-${group.slug}`}>
        <h3 id={`heading-${group.slug}`}>{group.title}</h3>
        <div className="project-photo-grid">
          {group.photos.map((photo) => <button key={photo.src} type="button" onClick={() => setSelected(photo)} aria-label={`Enlarge: ${photo.alt}`}>
            <img src={photo.src} alt={photo.alt} loading="lazy" decoding="async" />
          </button>)}
        </div>
      </section>)}
    </div>
    <dialog ref={dialog} className="project-photo-dialog" aria-label="Project photograph" onClose={() => setSelected(null)} onClick={(event) => { if (event.target === event.currentTarget) dialog.current?.close() }}>
      <button className="photo-dialog-close" type="button" onClick={() => dialog.current?.close()} aria-label="Close photograph">Close ×</button>
      {selected && <figure><img src={selected.src} alt={selected.alt} /></figure>}
    </dialog>
  </section>
}
