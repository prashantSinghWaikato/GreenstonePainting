import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import Blog from './Blog.tsx'
import ServicesPage from './Services.tsx'
import ProjectsPage from './Projects.tsx'
import ChatAssistant from './ChatAssistant.tsx'
import AdminApp from './AdminApp.tsx'

const pathname = window.location.pathname.endsWith('/') ? window.location.pathname : `${window.location.pathname}/`
const isAdmin = pathname === '/admin/'
const isKnownPublicPage = pathname === '/' || pathname === '/services/' || pathname === '/projects/'
const isJournalPage = pathname === '/blog/' || (!isAdmin && !isKnownPublicPage)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <>
      {isAdmin ? <AdminApp /> : pathname === '/services/' ? <ServicesPage /> : pathname === '/projects/' ? <ProjectsPage /> : isJournalPage ? <Blog /> : <App />}
      {!isAdmin && <ChatAssistant />}
    </>
  </StrictMode>,
)
