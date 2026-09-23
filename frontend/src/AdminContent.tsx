import { useState } from 'react'
import { FileText, Images, Paintbrush, Palette } from 'lucide-react'
import type { AdminSession } from './api/adminAuth'
import AdminArticles from './AdminArticles'
import AdminProjects from './AdminProjects'
import AdminServices from './AdminServices'
import AdminColours from './AdminColours'
import './AdminContent.css'

export default function AdminContent({ session, onSessionExpired }: { session: AdminSession; onSessionExpired: () => void }) {
  const [section, setSection] = useState<'services' | 'projects' | 'articles' | 'colours'>('services')
  return <>
    <nav className="content-module-tabs" aria-label="Website content sections">
      <button type="button" className={section === 'services' ? 'active' : ''} onClick={() => setSection('services')}><Paintbrush aria-hidden="true" /><span>Services</span></button>
      <button type="button" className={section === 'projects' ? 'active' : ''} onClick={() => setSection('projects')}><Images aria-hidden="true" /><span>Projects</span></button>
      <button type="button" className={section === 'articles' ? 'active' : ''} onClick={() => setSection('articles')}><FileText aria-hidden="true" /><span>Blog articles</span></button>
      {session.role === 'OWNER' && <button type="button" className={section === 'colours' ? 'active' : ''} onClick={() => setSection('colours')}><Palette aria-hidden="true" /><span>Colour palette</span></button>}
    </nav>
    {section === 'services'
      ? <AdminServices session={session} onSessionExpired={onSessionExpired} />
      : section === 'projects'
      ? <AdminProjects session={session} onSessionExpired={onSessionExpired} />
      : section === 'articles'
      ? <AdminArticles session={session} onSessionExpired={onSessionExpired} />
      : <AdminColours onSessionExpired={onSessionExpired} />}
  </>
}
