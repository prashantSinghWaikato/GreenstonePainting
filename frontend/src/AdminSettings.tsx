import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { AlertTriangle, Check, CheckCircle2, Clock3, KeyRound, LoaderCircle, LockKeyhole, Plus, ShieldCheck, UserCheck, UserRound, UserX, UsersRound } from 'lucide-react'
import type { AdminSession } from './api/adminAuth'
import {
  AdminStaffApiError,
  changeAdminPassword,
  createAdminStaff,
  getAdminStaffDashboard,
  setAdminStaffEnabled,
  type AdminRole,
  type AdminStaffDashboard,
  type AdminStaffMember,
} from './api/adminStaff'
import './AdminSettings.css'

function formatDate(value: string | null) {
  if (!value) return 'Never'
  return new Intl.DateTimeFormat('en-NZ', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Pacific/Auckland',
  }).format(new Date(value))
}

function PasswordPanel({ onSessionExpired }: { onSessionExpired: () => void }) {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const passwordLengthValid = newPassword.length >= 12 && newPassword.length <= 100
  const passwordLetterValid = /[A-Za-z]/.test(newPassword)
  const passwordNumberValid = /\d/.test(newPassword)
  const passwordsMatch = newPassword.length > 0 && newPassword === confirmPassword
  const valid = currentPassword.length > 0 && passwordLengthValid && passwordLetterValid && passwordNumberValid && passwordsMatch

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!valid) return
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      await changeAdminPassword(currentPassword, newPassword)
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setSuccess('Your password has been changed.')
    } catch (caught) {
      if (caught instanceof AdminStaffApiError && caught.status === 401) onSessionExpired()
      else setError(caught instanceof Error ? caught.message : 'Your password could not be changed.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="settings-card settings-password-card">
      <header><div className="settings-icon"><KeyRound aria-hidden="true" /></div><div><span className="admin-eyebrow">Your security</span><h2>Change password</h2><p>Update the password used for your own staff account.</p></div></header>
      <form onSubmit={submit}>
        <label><span>Current password</span><input type="password" value={currentPassword} onChange={(event) => { setCurrentPassword(event.target.value); setError(''); setSuccess('') }} autoComplete="current-password" required /></label>
        <label><span>New password</span><input type="password" value={newPassword} onChange={(event) => { setNewPassword(event.target.value); setError(''); setSuccess('') }} autoComplete="new-password" required maxLength={100} /></label>
        <label><span>Confirm new password</span><input type="password" value={confirmPassword} onChange={(event) => { setConfirmPassword(event.target.value); setError(''); setSuccess('') }} autoComplete="new-password" required maxLength={100} /></label>
        <div className="settings-password-rules" aria-label="Password requirements">
          <span className={passwordLengthValid ? 'valid' : ''}><Check aria-hidden="true" /> 12–100 characters</span>
          <span className={passwordLetterValid ? 'valid' : ''}><Check aria-hidden="true" /> At least one letter</span>
          <span className={passwordNumberValid ? 'valid' : ''}><Check aria-hidden="true" /> At least one number</span>
          <span className={passwordsMatch ? 'valid' : ''}><Check aria-hidden="true" /> Passwords match</span>
        </div>
        {success && <div className="settings-success" role="status"><CheckCircle2 aria-hidden="true" /> {success}</div>}
        {error && <div className="settings-error" role="alert"><AlertTriangle aria-hidden="true" /> {error}</div>}
        <button type="submit" disabled={!valid || saving}>{saving ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <LockKeyhole aria-hidden="true" />}{saving ? 'Changing password…' : 'Change password'}</button>
      </form>
    </section>
  )
}

function CreateStaffPanel({ onCreated, onSessionExpired }: { onCreated: () => void; onSessionExpired: () => void }) {
  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [role, setRole] = useState<AdminRole>('STAFF')
  const [temporaryPassword, setTemporaryPassword] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const created = await createAdminStaff({ displayName, email, role, temporaryPassword })
      setDisplayName('')
      setEmail('')
      setRole('STAFF')
      setTemporaryPassword('')
      setSuccess(`${created.displayName}'s account is ready.`)
      onCreated()
    } catch (caught) {
      if (caught instanceof AdminStaffApiError && caught.status === 401) onSessionExpired()
      else setError(caught instanceof Error ? caught.message : 'The staff account could not be created.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="settings-card settings-create-card">
      <header><div className="settings-icon"><Plus aria-hidden="true" /></div><div><span className="admin-eyebrow">Owner control</span><h2>Add staff account</h2><p>Create private access without exposing public registration.</p></div></header>
      <form onSubmit={submit}>
        <label><span>Display name</span><input value={displayName} onChange={(event) => setDisplayName(event.target.value)} required maxLength={150} /></label>
        <label><span>Email address</span><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required maxLength={254} /></label>
        <label><span>Role</span><select value={role} onChange={(event) => setRole(event.target.value as AdminRole)}><option value="STAFF">Staff — enquiries and own password</option><option value="OWNER">Owner — includes team management</option></select></label>
        <label><span>Temporary password</span><input type="password" value={temporaryPassword} onChange={(event) => setTemporaryPassword(event.target.value)} required minLength={12} maxLength={100} autoComplete="new-password" /><small>Use 12–100 characters with a letter and number. Share it securely.</small></label>
        {success && <div className="settings-success" role="status"><CheckCircle2 aria-hidden="true" /> {success}</div>}
        {error && <div className="settings-error" role="alert"><AlertTriangle aria-hidden="true" /> {error}</div>}
        <button type="submit" disabled={saving}>{saving ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <UserCheck aria-hidden="true" />}{saving ? 'Creating account…' : 'Create staff account'}</button>
      </form>
    </section>
  )
}

function TeamManagement({ session, onSessionExpired }: { session: AdminSession; onSessionExpired: () => void }) {
  const [dashboard, setDashboard] = useState<AdminStaffDashboard | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [changingId, setChangingId] = useState('')
  const [confirmTarget, setConfirmTarget] = useState<AdminStaffMember | null>(null)

  useEffect(() => {
    let active = true
    getAdminStaffDashboard()
      .then((response) => { if (active) setDashboard(response) })
      .catch((caught) => {
        if (!active) return
        if (caught instanceof AdminStaffApiError && caught.status === 401) onSessionExpired()
        else setError(caught instanceof Error ? caught.message : 'Team accounts could not be loaded.')
      })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [onSessionExpired])

  async function refresh() {
    setError('')
    try {
      setDashboard(await getAdminStaffDashboard())
    } catch (caught) {
      if (caught instanceof AdminStaffApiError && caught.status === 401) onSessionExpired()
      else setError(caught instanceof Error ? caught.message : 'Team accounts could not be refreshed.')
    }
  }

  async function changeEnabled(staff: AdminStaffMember, enabled: boolean) {
    setChangingId(staff.id)
    setError('')
    try {
      await setAdminStaffEnabled(staff, enabled)
      await refresh()
    } catch (caught) {
      if (caught instanceof AdminStaffApiError && caught.status === 401) onSessionExpired()
      else setError(caught instanceof Error ? caught.message : 'The account status could not be changed.')
    } finally {
      setChangingId('')
      setConfirmTarget(null)
    }
  }

  if (loading) return <div className="settings-loading"><LoaderCircle className="admin-spinner" aria-hidden="true" /> Loading team accounts…</div>

  return (
    <>
      <CreateStaffPanel onCreated={refresh} onSessionExpired={onSessionExpired} />
      <section className="settings-card settings-team-card">
        <header><div className="settings-icon"><UsersRound aria-hidden="true" /></div><div><span className="admin-eyebrow">Access control</span><h2>Team accounts</h2><p>Deactivate access immediately when a staff member no longer needs the portal.</p></div></header>
        {error && <div className="settings-error" role="alert"><AlertTriangle aria-hidden="true" /> {error}</div>}
        <div className="settings-team-list">
          {dashboard?.staff.map((staff) => {
            const isCurrent = staff.email.toLowerCase() === session.email.toLowerCase()
            return (
              <article key={staff.id} className={!staff.enabled ? 'disabled' : ''}>
                <div className="settings-staff-avatar">{staff.displayName.slice(0, 1).toUpperCase()}</div>
                <div className="settings-staff-identity"><strong>{staff.displayName}</strong><span>{staff.email}</span><small>{staff.role === 'OWNER' ? 'Owner' : 'Staff'}{isCurrent ? ' · You' : ''}</small></div>
                <div className="settings-staff-login"><span>Last login</span><strong>{formatDate(staff.lastLoginAt)}</strong>{staff.lockedUntil && <small>Locked until {formatDate(staff.lockedUntil)}</small>}</div>
                <span className={`settings-account-status ${staff.enabled ? 'active' : 'inactive'}`}>{staff.enabled ? 'Active' : 'Inactive'}</span>
                {staff.enabled ? <button type="button" onClick={() => setConfirmTarget(staff)} disabled={isCurrent || changingId === staff.id} className="settings-disable"><UserX aria-hidden="true" /> Deactivate</button> : <button type="button" onClick={() => void changeEnabled(staff, true)} disabled={changingId === staff.id} className="settings-enable">{changingId === staff.id ? <LoaderCircle className="admin-spinner" aria-hidden="true" /> : <UserCheck aria-hidden="true" />} Activate</button>}
              </article>
            )
          })}
        </div>
      </section>

      <section className="settings-card settings-audit-card">
        <header><div className="settings-icon"><Clock3 aria-hidden="true" /></div><div><span className="admin-eyebrow">Security history</span><h2>Account activity</h2><p>The latest team-access and password events.</p></div></header>
        <div className="settings-audit-list">
          {dashboard?.activities.length ? dashboard.activities.map((activity) => <article key={activity.id}><div><ShieldCheck aria-hidden="true" /></div><p><strong>{activity.summary}</strong><span>{formatDate(activity.createdAt)}</span></p></article>) : <p className="settings-empty-audit">Account-management activity will appear here.</p>}
        </div>
      </section>

      {confirmTarget && <div className="settings-confirm-overlay"><section role="dialog" aria-modal="true" aria-labelledby="deactivate-title"><AlertTriangle aria-hidden="true" /><span className="admin-eyebrow">Confirm access change</span><h2 id="deactivate-title">Deactivate {confirmTarget.displayName}?</h2><p>Their active staff session will stop working on the next request. Their enquiry and audit history will remain intact.</p><div><button type="button" onClick={() => setConfirmTarget(null)} className="cancel">Cancel</button><button type="button" onClick={() => void changeEnabled(confirmTarget, false)} className="confirm">Deactivate account</button></div></section></div>}
    </>
  )
}

export default function AdminSettings({ session, onSessionExpired }: { session: AdminSession; onSessionExpired: () => void }) {
  return (
    <section className="admin-settings">
      <header className="settings-page-heading"><div><span className="admin-eyebrow">Staff settings</span><h1>Accounts & security</h1><p>Manage secure access to Greenstone operations.</p></div><span className="settings-role-badge">{session.role === 'OWNER' ? 'Owner access' : 'Staff access'}</span></header>
      <div className="settings-grid"><PasswordPanel onSessionExpired={onSessionExpired} />{session.role === 'OWNER' && <TeamManagement session={session} onSessionExpired={onSessionExpired} />}</div>
      {session.role === 'STAFF' && <section className="settings-staff-notice"><UserRound aria-hidden="true" /><div><h2>Staff account</h2><p>Only Owners can create, activate or deactivate team accounts. Contact an Owner if your access details need to change.</p></div></section>}
    </section>
  )
}
