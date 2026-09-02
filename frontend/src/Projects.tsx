import { useEffect, useState } from 'react'
import { ArrowRight, CheckCircle2, MapPin, Paintbrush } from 'lucide-react'
import { PublicFooter, PublicHeader } from './Blog'
import { projects } from './data/site'
import { getPublishedProjects, type PublishedProject } from './api/projects'
import './Projects.css'

const fallbackProjects: PublishedProject[] = projects.map((project) => ({ ...project, slug: project.title.toLowerCase().replace(/[^a-z0-9]+/g, '-'), highlights: [project.category, 'Preparation-led finish', 'Quality-controlled outcome'] }))

export default function ProjectsPage() {
  const [portfolio, setPortfolio] = useState<PublishedProject[]>(fallbackProjects)

  useEffect(() => {
    let active = true
    getPublishedProjects().then((published) => { if (active && published.length) setPortfolio(published) }).catch(() => undefined)
    return () => { active = false }
  }, [])

  return <div className="site-shell projects-page-shell">
    <PublicHeader active="projects" />
    <main id="main-content">
      <section className="projects-page-hero">
        <div className="projects-hero-image" aria-hidden="true"><img src="/images/greenstone-before-after.jpg" alt="" /></div>
        <div className="projects-hero-overlay" aria-hidden="true" />
        <div className="page-container projects-hero-inner">
          <div><p className="eyebrow eyebrow-light">Selected painting work</p><h1>See the finish.<br /><span>Understand the work.</span></h1></div>
          <div className="projects-hero-summary"><p>A focused portfolio of residential painting outcomes currently featured by Greenstone Painting across Hamilton and Waikato.</p><a className="button button-primary" href="/#quote">Plan a Similar Project <ArrowRight size={17} aria-hidden="true" /></a></div>
        </div>
        <div className="projects-paint-strip" aria-hidden="true"><span /><span /><span /><span /><span /></div>
      </section>

      <section className="section projects-intro" aria-labelledby="projects-heading"><div className="page-container projects-intro-layout"><div><p className="eyebrow">Project portfolio</p><h2 id="projects-heading">Selected outcomes.<br />Clearly presented.</h2></div><div><p>Each project starts with the property and the surfaces in front of us. Preparation, product selection, access, and sequencing are defined around the actual scope—not a one-size-fits-all process.</p><a href="/services/">Explore our painting services <ArrowRight size={15} aria-hidden="true" /></a></div></div></section>

      <section className="projects-gallery" aria-label="Featured painting projects">
        {portfolio.map((project, index) => <article className={`portfolio-project portfolio-project-${(index % 3) + 1}`} id={project.slug} key={project.slug}>
          <div className="portfolio-image"><img src={project.image} alt={project.alt} /><div className="portfolio-image-shade" aria-hidden="true" /><span>{project.category}</span></div>
          <div className="portfolio-copy"><div className="portfolio-location"><MapPin size={15} strokeWidth={1.8} aria-hidden="true" />{project.location}</div><h2>{project.title}</h2><p>{project.summary}</p><ul>{project.highlights.map((note) => <li key={note}><CheckCircle2 size={16} strokeWidth={1.8} aria-hidden="true" />{note}</li>)}</ul><a href="/#quote">Discuss a similar project <ArrowRight size={16} aria-hidden="true" /></a></div>
        </article>)}
      </section>

      <section className="section portfolio-standard" aria-labelledby="portfolio-standard-heading"><div className="page-container"><div className="portfolio-standard-heading"><div><p className="eyebrow eyebrow-light">Behind the finish</p><h2 id="portfolio-standard-heading">A disciplined path<br />to the final result.</h2></div><p>Good painting photographs show the outcome. The working process is what helps create it.</p></div><div className="portfolio-standard-grid"><article><span>Scope</span><h3>Start with the surfaces</h3><p>We review condition, access, priorities, and the intended finish before defining the work.</p></article><article><span>Prepare</span><h3>Build the right foundation</h3><p>Protection, cleaning, repairs, sanding, and priming are matched to the agreed surfaces.</p></article><article><span>Finish</span><h3>Apply a controlled system</h3><p>Coatings are applied according to the selected products and project sequence.</p></article><article><span>Review</span><h3>Complete with clarity</h3><p>The agreed scope and relevant finishing details are reviewed before handover.</p></article></div></div></section>

      <section className="projects-cta"><div className="projects-cta-image" aria-hidden="true"><img src="/images/greenstone-bedroom.webp" alt="" /></div><div className="projects-cta-overlay" aria-hidden="true" /><div className="page-container projects-cta-inner"><div><Paintbrush size={27} strokeWidth={1.6} aria-hidden="true" /><p>Make your property the next project</p><h2>Tell us what you want<br />to transform.</h2></div><a className="button button-primary" href="/#quote">Request Your Free Quote <ArrowRight size={17} aria-hidden="true" /></a></div></section>
    </main>
    <PublicFooter />
  </div>
}
