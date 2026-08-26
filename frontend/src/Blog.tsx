import { ArrowLeft, ArrowRight, Mail, MapPin, Paintbrush, Phone } from 'lucide-react'
import { FaFacebookF, FaInstagram } from 'react-icons/fa'
import { blogPosts, findBlogPost } from './data/blog'
import { services } from './data/site'
import './App.css'
import './Blog.css'

export function PublicHeader({ active }: { active?: 'services' | 'projects' | 'blog' }) {
  return <>
    <a className="skip-link" href="#main-content">Skip to content</a>
    <div className="utility-bar">
      <div className="page-container utility-inner">
        <p><strong>Registered Master Painters member</strong></p>
        <div className="utility-contact">
          <span className="utility-item"><MapPin size={14} strokeWidth={2} aria-hidden="true" />Hamilton, Waikato</span>
          <a className="utility-item" href="mailto:info@greenstonepainting.co.nz"><Mail size={14} strokeWidth={2} aria-hidden="true" />info@greenstonepainting.co.nz</a>
          <a className="utility-item" href="tel:+642108383831"><Phone size={14} strokeWidth={2} aria-hidden="true" />021 083 83831</a>
        </div>
      </div>
    </div>
    <header className="site-header blog-header">
      <div className="page-container header-inner">
        <a className="brand brand-logo" href="/" aria-label="Greenstone Painting Limited home"><img src="/images/greenstone-logo.png" alt="Greenstone Painting Limited" /></a>
        <nav className="blog-nav" aria-label="Primary navigation">
          <a className={active === 'services' ? 'is-active' : undefined} href="/services/">Services</a><a className={active === 'projects' ? 'is-active' : undefined} href="/projects/">Projects</a><a href="/#about">About Us</a><a href="/#areas">Service Areas</a><a className={active === 'blog' ? 'is-active' : undefined} href="/blog/">Blog</a><a href="/#contact">Contact</a>
        </nav>
        <a className="button button-primary blog-header-cta" href="/#quote">Get a Free Quote</a>
      </div>
    </header>
  </>
}

export function PublicFooter() {
  return <footer className="site-footer">
    <div className="page-container footer-main">
      <div className="footer-brand"><a className="brand brand-logo brand-logo-footer" href="/" aria-label="Greenstone Painting Limited home"><img src="/images/greenstone-logo.png" alt="Greenstone Painting Limited" /></a><p>Professional residential and commercial painting throughout Waikato.</p><a className="footer-call" href="tel:+642108383831">021 083 83831</a></div>
      <div className="footer-column"><h2>Services</h2>{services.slice(0, 5).map((service) => <a href={`/services/#${service.slug}`} key={service.slug}>{service.title}</a>)}</div>
      <div className="footer-column"><h2>Company</h2><a href="/#about">About Us</a><a href="/projects/">Projects</a><a href="/#areas">Service Areas</a><a href="/blog/">Blog</a><a href="/#quote">Get a Quote</a></div>
      <div className="footer-column"><h2>Contact</h2><a href="mailto:info@greenstonepainting.co.nz">info@greenstonepainting.co.nz</a><a href="https://www.google.com/maps/search/?api=1&query=29+Lachlan+Drive,+Dinsdale,+Hamilton,+New+Zealand" target="_blank" rel="noreferrer">29 Lachlan Drive<br />Dinsdale, Hamilton</a><div className="social-row"><a href="https://www.instagram.com/greenstonepainting.nz/" target="_blank" rel="noreferrer" aria-label="Follow Greenstone Painting on Instagram"><FaInstagram size={16} aria-hidden="true" /></a><a href="https://www.facebook.com/greenstonepainting/" target="_blank" rel="noreferrer" aria-label="Visit Greenstone Painting on Facebook"><FaFacebookF size={15} aria-hidden="true" /></a></div></div>
    </div>
    <div className="page-container footer-bottom"><span>© {new Date().getFullYear()} Greenstone Painting Limited</span><a href="/#privacy">Privacy Notice</a></div>
  </footer>
}

function BlogIndex() {
  return <>
    <section className="blog-hero">
      <div className="blog-hero-colour" aria-hidden="true"><span /><span /><span /></div>
      <div className="page-container blog-hero-inner">
        <div><p className="eyebrow eyebrow-light">Advice &amp; insights</p><h1>Better decisions<br />before the first coat.</h1></div>
        <p>Practical guidance on preparation, materials, finishes, and caring for painted surfaces in Waikato homes and commercial spaces.</p>
      </div>
    </section>
    <section className="section blog-list-section" aria-labelledby="latest-articles">
      <div className="page-container">
        <div className="blog-section-heading"><div><span>Greenstone journal</span><h2 id="latest-articles">Latest articles</h2></div><p>Clear information from a working painter’s perspective.</p></div>
        <div className="blog-card-grid">
          {blogPosts.map((post, index) => <article className="blog-card" key={post.path}>
            <a className="blog-card-image" href={post.path} aria-label={`Read ${post.title}`}><img src={post.image} alt={post.imageAlt} /><span>{post.topic}</span></a>
            <div className="blog-card-body"><p className="blog-meta"><time>{post.date}</time><span>{post.readTime}</span></p><h3><a href={post.path}>{post.title}</a></h3><p>{post.excerpt}</p><a className="blog-read-link" href={post.path}>Read article <ArrowRight size={16} aria-hidden="true" /></a></div>
            <span className="blog-card-index" aria-hidden="true">0{index + 1}</span>
          </article>)}
        </div>
      </div>
    </section>
    <BlogCallout />
  </>
}

function BlogCallout() {
  return <section className="blog-callout">
    <div className="page-container blog-callout-inner"><div><Paintbrush size={28} strokeWidth={1.6} aria-hidden="true" /><p>Have a project in mind?</p><h2>Turn the advice into a clear painting plan.</h2></div><a className="button button-primary" href="/#quote">Request Your Free Quote <ArrowRight size={17} aria-hidden="true" /></a></div>
  </section>
}

function ArticlePage({ pathname }: { pathname: string }) {
  const post = findBlogPost(pathname)
  if (!post) return <NotFound />
  const relatedPosts = blogPosts.filter((item) => item.path !== post.path)

  return <>
    <article>
      <header className="article-hero">
        <div className="article-hero-image"><img src={post.image} alt={post.imageAlt} /></div>
        <div className="article-hero-shade" aria-hidden="true" />
        <div className="page-container article-hero-inner">
          <a className="article-back" href="/blog/"><ArrowLeft size={15} aria-hidden="true" /> All articles</a>
          <div className="article-title-block"><p className="article-topic">{post.topic}</p><h1>{post.title}</h1><div className="article-meta"><time>{post.date}</time><span>{post.readTime}</span><span>Greenstone Painting</span></div></div>
        </div>
      </header>

      <div className="page-container article-layout">
        <aside className="article-rail" aria-label="Article summary"><span>In this guide</span><p>{post.excerpt}</p><a href="/#quote">Discuss your project <ArrowRight size={15} aria-hidden="true" /></a></aside>
        <div className="article-content">
          <div className="article-introduction">{post.introduction.map((paragraph) => <p key={paragraph}>{paragraph}</p>)}</div>
          {post.sections.map((section) => <section key={section.heading}>
            <h2>{section.heading}</h2>
            {section.paragraphs?.map((paragraph) => <p key={paragraph}>{paragraph}</p>)}
            {section.bullets && <ul>{section.bullets.map((bullet) => <li key={bullet}>{bullet}</li>)}</ul>}
          </section>)}
          <div className="article-note"><strong>Planning note</strong><p>Every property and coating system is different. We recommend a site assessment before confirming preparation, products, programme, or price.</p></div>
        </div>
      </div>
    </article>

    <section className="section related-section" aria-labelledby="related-heading"><div className="page-container"><div className="related-heading"><div><p className="eyebrow">Continue reading</p><h2 id="related-heading">More from the journal</h2></div><a href="/blog/">View all articles <ArrowRight size={15} aria-hidden="true" /></a></div><div className="related-grid">{relatedPosts.map((item) => <article key={item.path}><img src={item.image} alt="" /><div><span>{item.topic}</span><h3><a href={item.path}>{item.shortTitle}</a></h3><p>{item.date}</p></div></article>)}</div></div></section>
    <BlogCallout />
  </>
}

function NotFound() {
  return <section className="blog-not-found"><div className="page-container"><p className="eyebrow eyebrow-light">Article not found</p><h1>This page is not in the journal.</h1><a className="button button-primary" href="/blog/">View all articles</a></div></section>
}

export default function Blog() {
  const pathname = window.location.pathname.endsWith('/') ? window.location.pathname : `${window.location.pathname}/`
  return <div className="site-shell blog-shell"><PublicHeader active="blog" /><main id="main-content">{pathname === '/blog/' ? <BlogIndex /> : <ArticlePage pathname={pathname} />}</main><PublicFooter /></div>
}
