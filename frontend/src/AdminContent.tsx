import { useState } from 'react'
import { FileText, Images, Paintbrush } from 'lucide-react'
import type { AdminSession } from './api/adminAuth'
import AdminArticles from './AdminArticles'
import AdminProjects from './AdminProjects'
import AdminServices from './AdminServices'
import './AdminContent.css'

export default function AdminContent({ session, onSessionExpired }: { session: AdminSession; onSessionExpired: () => void }) {
  const [section, setSection] = useState<'services' | 'projects' | 'articles'>('services')
  return <>
    <nav className="content-module-tabs" aria-label="Website content sections">
      <button type="button" className={section === 'services' ? 'active' : ''} onClick={() => setSection('services')}><Paintbrush aria-hidden="true" /><span>Services</span></button>
      <button type="button" className={section === 'projects' ? 'active' : ''} onClick={() => setSection('projects')}><Images aria-hidden="true" /><span>Projects</span></button>
      <button type="button" className={section === 'articles' ? 'active' : ''} onClick={() => setSection('articles')}><FileText aria-hidden="true" /><span>Blog articles</span></button>
    </nav>
    {section === 'services'
      ? <AdminServices session={session} onSessionExpired={onSessionExpired} />
      : section === 'projects'
      ? <AdminProjects session={session} onSessionExpired={onSessionExpired} />
      : <AdminArticles session={session} onSessionExpired={onSessionExpired} />}
  </>
}
