import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import Blog from './Blog.tsx'
import ServicesPage from './Services.tsx'
import ProjectsPage from './Projects.tsx'
import ChatAssistant from './ChatAssistant.tsx'
import { AboutPage, ContactPage, PrivacyPage, ServiceAreasPage } from './CompanyPages.tsx'

const pathname = window.location.pathname.endsWith('/') ? window.location.pathname : `${window.location.pathname}/`
const isKnownPublicPage = new Set(['/', '/services/', '/projects/', '/about/', '/service-areas/', '/contact/', '/privacy/']).has(pathname)
const isJournalPage = pathname === '/blog/' || !isKnownPublicPage

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <>
      {pathname === '/services/' ? <ServicesPage />
          : pathname === '/projects/' ? <ProjectsPage />
            : pathname === '/about/' ? <AboutPage />
              : pathname === '/service-areas/' ? <ServiceAreasPage />
                : pathname === '/contact/' ? <ContactPage />
                  : pathname === '/privacy/' ? <PrivacyPage />
                    : isJournalPage ? <Blog /> : <App />}
      <ChatAssistant />
    </>
  </StrictMode>,
)
