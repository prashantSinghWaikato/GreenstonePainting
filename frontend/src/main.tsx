import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import Blog from './Blog.tsx'
import ServicesPage from './Services.tsx'
import ProjectsPage from './Projects.tsx'

const blogPaths = new Set([
  '/blog/',
  '/how-painters-prepare-your-home-for-a-smooth-paint-job/',
  '/822-2/',
  '/wood-staining-benefits-you-need-to-take-advantage-of/',
])

const pathname = window.location.pathname.endsWith('/') ? window.location.pathname : `${window.location.pathname}/`

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {pathname === '/services/' ? <ServicesPage /> : pathname === '/projects/' ? <ProjectsPage /> : blogPaths.has(pathname) ? <Blog /> : <App />}
  </StrictMode>,
)
