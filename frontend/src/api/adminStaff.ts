import { getAdminMutationHeaders } from './adminAuth'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type AdminRole = 'OWNER' | 'STAFF'

export type AdminStaffMember = {
  id: string
  email: string
  displayName: string
  role: AdminRole
  enabled: boolean
  lastLoginAt: string | null
  passwordChangedAt: string | null
  lockedUntil: string | null
  createdAt: string
  version: number
}

export type AdminAccountActivity = {
  id: string
  type: 'ACCOUNT_CREATED' | 'ACCOUNT_ACTIVATED' | 'ACCOUNT_DEACTIVATED' | 'PASSWORD_CHANGED'
  summary: string
  actorDisplayName: string
  targetDisplayName: string
  createdAt: string
}

export type AdminStaffDashboard = {
  staff: AdminStaffMember[]
  activities: AdminAccountActivity[]
}

export class AdminStaffApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'AdminStaffApiError'
    this.status = status
  }
}

async function errorMessage(response: Response, fallback: string) {
  try {
    const body = (await response.json()) as { message?: string }
    return body.message || fallback
  } catch {
    return fallback
  }
}

export async function getAdminStaffDashboard() {
  const response = await fetch(`${API_BASE_URL}/api/admin/staff`, { credentials: 'include' })
  if (!response.ok) throw new AdminStaffApiError(await errorMessage(response, 'Team accounts could not be loaded.'), response.status)
  return response.json() as Promise<AdminStaffDashboard>
}

export async function createAdminStaff(input: { displayName: string; email: string; role: AdminRole; temporaryPassword: string }) {
  const response = await fetch(`${API_BASE_URL}/api/admin/staff`, {
    method: 'POST',
    credentials: 'include',
    headers: await getAdminMutationHeaders(),
    body: JSON.stringify(input),
  })
  if (!response.ok) throw new AdminStaffApiError(await errorMessage(response, 'The staff account could not be created.'), response.status)
  return response.json() as Promise<AdminStaffMember>
}

export async function setAdminStaffEnabled(staff: AdminStaffMember, enabled: boolean) {
  const response = await fetch(`${API_BASE_URL}/api/admin/staff/${encodeURIComponent(staff.id)}/enabled`, {
    method: 'PATCH',
    credentials: 'include',
    headers: await getAdminMutationHeaders(),
    body: JSON.stringify({ enabled, version: staff.version }),
  })
  if (!response.ok) throw new AdminStaffApiError(await errorMessage(response, 'The account status could not be changed.'), response.status)
  return response.json() as Promise<AdminStaffMember>
}

export async function changeAdminPassword(currentPassword: string, newPassword: string) {
  const response = await fetch(`${API_BASE_URL}/api/admin/account/password`, {
    method: 'PATCH',
    credentials: 'include',
    headers: await getAdminMutationHeaders(),
    body: JSON.stringify({ currentPassword, newPassword }),
  })
  if (!response.ok) throw new AdminStaffApiError(await errorMessage(response, 'Your password could not be changed.'), response.status)
}
