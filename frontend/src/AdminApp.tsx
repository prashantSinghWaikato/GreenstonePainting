import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { ArrowRight, BriefcaseBusiness, CalendarDays, ChartNoAxesCombined, FileText, Inbox, LayoutDashboard, LoaderCircle, LockKeyhole, LogOut, ReceiptText, Settings, ShieldCheck } from 'lucide-react'
import { getAdminSession, signInAdmin, signOutAdmin, type AdminSession } from './api/adminAuth'
import AdminEnquiries from './AdminEnquiries'
import AdminContent from './AdminContent'
import AdminSettings from './AdminSettings'
import AdminJobs from './AdminJobs'
import { getJobOverview, type JobOverview } from './api/adminJobs'
import AdminInvoices from './AdminInvoices'
import AdminReports from './AdminReports'
import './AdminApp.css'

type AdminView = 'overview' | 'enquiries' | 'jobs' | 'invoices' | 'reports' | 'content' | 'settings'

function viewFromLocation(): AdminView {
  const section = new URLSearchParams(window.location.search).get('section')
  return section === 'enquiries' || section === 'jobs' || section === 'invoices' || section === 'reports' || section === 'content' || section === 'settings' ? section : 'overview'
}

function AdminOverview({ session, onNavigate, onSessionExpired }: { session: AdminSession; onNavigate: (view: AdminView) => void; onSessionExpired: () => void }) {
  const [jobs, setJobs] = useState<JobOverview | null>(null)
  useEffect(() => { getJobOverview().then(setJobs).catch((error) => { if (error && typeof error === 'object' && 'status' in error && error.status === 401) onSessionExpired() }) }, [onSessionExpired])
  return <>
    <div className="admin-page-heading"><span className="admin-eyebrow">Overview</span><h1>Today across Greenstone.</h1><p>Move customer enquiries into accepted quotes, then keep scheduled painting work and the team in view.</p></div>
    <div className="admin-status-grid">
      <article className="admin-status-card admin-status-card--active"><span>Access</span><ShieldCheck /><h2>Staff session active</h2><p>Signed in as {session.email}</p></article>
      <button type="button" className="admin-status-card admin-status-card--button" onClick={() => onNavigate('enquiries')}><span>Sales pipeline</span><Inbox /><h2>Open enquiry inbox</h2><p>Follow up requests, prepare quotes and record customer decisions.</p></button>
      <button type="button" className="admin-status-card admin-status-card--button" onClick={() => onNavigate('jobs')}><span>Operations</span><BriefcaseBusiness /><h2>{jobs ? `${jobs.metrics.scheduled + jobs.metrics.inProgress} active jobs` : 'Painting jobs'}</h2><p>Plan dates, assign the crew and record progress from site.</p></button>
    </div>
    <section className="admin-jobs-overview"><header><div><span className="admin-eyebrow">Forward schedule</span><h2>Upcoming painting work</h2></div><button onClick={() => onNavigate('jobs')}>View all jobs <ArrowRight /></button></header>
      {!jobs ? <div className="admin-overview-loading"><LoaderCircle className="admin-spinner" /> Loading schedule…</div> : jobs.upcoming.length ? <div>{jobs.upcoming.map((job) => <button key={job.id} onClick={() => { window.location.href = `/admin/?section=jobs&job=${job.id}` }}><CalendarDays /><span><strong>{job.customerName}</strong><small>{job.title} · {job.propertyAddress || 'Address pending'}</small></span><time>{job.scheduledStartDate ? new Intl.DateTimeFormat('en-NZ', { dateStyle: 'medium' }).format(new Date(`${job.scheduledStartDate}T00:00:00`)) : 'Unscheduled'}</time><ArrowRight /></button>)}</div> : <p className="admin-overview-empty">No upcoming jobs are scheduled yet. Accepted quotes can be converted into jobs from the enquiry workspace.</p>}
    </section>
  </>
}

function StaffLogin({ onAuthenticated }: { onAuthenticated: (session: AdminSession) => void }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      onAuthenticated(await signInAdmin(email, password))
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Sign-in failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="admin-login-shell">
      <section className="admin-login-brand" aria-label="Greenstone Painting staff portal">
        <a href="/" className="admin-brand-link" aria-label="Return to Greenstone Painting website">
          <img src="/images/greenstone-logo.png" alt="Greenstone Painting" />
        </a>
        <div className="admin-login-brand-copy">
          <span className="admin-eyebrow">Staff workspace</span>
          <h1>Keep every customer conversation moving.</h1>
          <p>A secure operational space for the Greenstone Painting team.</p>
        </div>
        <div className="admin-security-note">
          <ShieldCheck aria-hidden="true" />
          <span>Protected staff access</span>
        </div>
      </section>

      <section className="admin-login-panel">
        <div className="admin-login-card">
          <div className="admin-lock-mark"><LockKeyhole aria-hidden="true" /></div>
          <span className="admin-eyebrow">Welcome back</span>
          <h2>Sign in to the staff portal</h2>
          <p>Use the administrator account configured for this environment.</p>

          <form onSubmit={handleSubmit} className="admin-login-form">
            <label>
              <span>Email address</span>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoComplete="username"
                required
                maxLength={254}
              />
            </label>
            <label>
              <span>Password</span>
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="current-password"
                required
                maxLength={200}
              />
            </label>
            {error && <div className="admin-form-error" role="alert">{error}</div>}
            <button type="submit" className="admin-primary-button" disabled={submitting}>
              {submitting ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : null}
              <span>{submitting ? 'Signing in…' : 'Sign in securely'}</span>
              {!submitting && <ArrowRight aria-hidden="true" />}
            </button>
          </form>
          <a href="/" className="admin-back-link">Return to public website</a>
        </div>
      </section>
    </main>
  )
}

function AdminWorkspace({
  session,
  view,
  onNavigate,
  onSignedOut,
}: {
  session: AdminSession
  view: AdminView
  onNavigate: (view: AdminView) => void
  onSignedOut: () => void
}) {
  const [signingOut, setSigningOut] = useState(false)
  const [signOutError, setSignOutError] = useState('')

  async function handleSignOut() {
    setSigningOut(true)
    setSignOutError('')
    try {
      await signOutAdmin()
      onSignedOut()
    } catch (caught) {
      setSignOutError(caught instanceof Error ? caught.message : 'Sign-out failed.')
    } finally {
      setSigningOut(false)
    }
  }

  return (
    <div className="admin-workspace">
      <aside className="admin-sidebar">
        <button type="button" className="admin-sidebar-logo" onClick={() => onNavigate('overview')} aria-label="Go to admin overview">
          <img src="/images/greenstone-logo.png" alt="Greenstone Painting" />
        </button>
        <nav aria-label="Staff portal">
          <button type="button" onClick={() => onNavigate('overview')} className={view === 'overview' ? 'active' : ''} aria-current={view === 'overview' ? 'page' : undefined}><LayoutDashboard aria-hidden="true" /> Overview</button>
          <button type="button" onClick={() => onNavigate('enquiries')} className={view === 'enquiries' ? 'active' : ''} aria-current={view === 'enquiries' ? 'page' : undefined}><Inbox aria-hidden="true" /> Enquiries</button>
          <button type="button" onClick={() => onNavigate('jobs')} className={view === 'jobs' ? 'active' : ''} aria-current={view === 'jobs' ? 'page' : undefined}><BriefcaseBusiness aria-hidden="true" /> Jobs</button>
          <button type="button" onClick={() => onNavigate('invoices')} className={view === 'invoices' ? 'active' : ''} aria-current={view === 'invoices' ? 'page' : undefined}><ReceiptText aria-hidden="true" /> Invoices</button>
          <button type="button" onClick={() => onNavigate('reports')} className={view === 'reports' ? 'active' : ''} aria-current={view === 'reports' ? 'page' : undefined}><ChartNoAxesCombined aria-hidden="true" /> Reports</button>
          <button type="button" onClick={() => onNavigate('content')} className={view === 'content' ? 'active' : ''} aria-current={view === 'content' ? 'page' : undefined}><FileText aria-hidden="true" /> Content</button>
          <button type="button" onClick={() => onNavigate('settings')} className={view === 'settings' ? 'active' : ''} aria-current={view === 'settings' ? 'page' : undefined}><Settings aria-hidden="true" /> Settings</button>
        </nav>
        <div className="admin-sidebar-security"><ShieldCheck aria-hidden="true" /><span>Secure staff session</span></div>
      </aside>

      <main className="admin-main">
        <header className="admin-topbar">
          <div>
            <span className="admin-eyebrow">Greenstone operations</span>
            <strong>{session.displayName}</strong>
          </div>
          <button type="button" onClick={handleSignOut} disabled={signingOut} className="admin-signout">
            {signingOut ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <LogOut aria-hidden="true" />}
            {signingOut ? 'Signing out…' : 'Sign out'}
          </button>
        </header>

        <section className={`admin-content${view !== 'overview' ? ' admin-content--wide' : ''}`}>
          {signOutError && <div className="admin-form-error" role="alert">{signOutError}</div>}
          {view === 'enquiries' ? (
            <AdminEnquiries onSessionExpired={onSignedOut} />
          ) : view === 'jobs' ? (
            <AdminJobs onSessionExpired={onSignedOut} />
          ) : view === 'invoices' ? (
            <AdminInvoices onSessionExpired={onSignedOut} />
          ) : view === 'reports' ? (
            <AdminReports onSessionExpired={onSignedOut} />
          ) : view === 'content' ? (
            <AdminContent session={session} onSessionExpired={onSignedOut} />
          ) : view === 'settings' ? (
            <AdminSettings session={session} onSessionExpired={onSignedOut} />
          ) : <AdminOverview session={session} onNavigate={onNavigate} onSessionExpired={onSignedOut} />}
        </section>
      </main>
    </div>
  )
}

export default function AdminApp() {
  const [session, setSession] = useState<AdminSession | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [view, setView] = useState<AdminView>(viewFromLocation)

  useEffect(() => {
    function handlePopState() {
      setView(viewFromLocation())
    }
    window.addEventListener('popstate', handlePopState)
    return () => window.removeEventListener('popstate', handlePopState)
  }, [])

  function navigate(nextView: AdminView) {
    const url = nextView === 'overview' ? '/admin/' : `/admin/?section=${nextView}`
    window.history.pushState({}, '', url)
    setView(nextView)
  }

  useEffect(() => {
    let active = true
    getAdminSession()
      .then((currentSession) => {
        if (active) setSession(currentSession)
      })
      .catch((caught) => {
        if (active) setError(caught instanceof Error ? caught.message : 'The staff portal is unavailable.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [])

  if (loading) {
    return <main className="admin-loading"><LoaderCircle className="admin-spinner" aria-hidden="true" /><span>Checking secure session…</span></main>
  }

  if (error) {
    return <main className="admin-loading"><div className="admin-form-error" role="alert">{error}</div><button onClick={() => window.location.reload()} className="admin-primary-button">Try again</button></main>
  }

  return session
    ? <AdminWorkspace session={session} view={view} onNavigate={navigate} onSignedOut={() => setSession(null)} />
    : <StaffLogin onAuthenticated={setSession} />
}
