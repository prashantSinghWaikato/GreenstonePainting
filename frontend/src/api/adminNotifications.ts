import { getAdminMutationHeaders } from './adminAuth'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type AdminNotificationPreferences = {
  assignmentNotificationsEnabled: boolean
  followUpNotificationsEnabled: boolean
  dailyDigestEnabled: boolean
  jobNotificationsEnabled: boolean
}

export class AdminNotificationApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'AdminNotificationApiError'
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

export async function getAdminNotificationPreferences() {
  const response = await fetch(`${API_BASE_URL}/api/admin/account/notifications`, { credentials: 'include' })
  if (!response.ok) throw new AdminNotificationApiError(await errorMessage(response, 'Notification preferences could not be loaded.'), response.status)
  return response.json() as Promise<AdminNotificationPreferences>
}

export async function updateAdminNotificationPreferences(preferences: AdminNotificationPreferences) {
  const response = await fetch(`${API_BASE_URL}/api/admin/account/notifications`, {
    method: 'PATCH',
    credentials: 'include',
    headers: await getAdminMutationHeaders(),
    body: JSON.stringify(preferences),
  })
  if (!response.ok) throw new AdminNotificationApiError(await errorMessage(response, 'Notification preferences could not be saved.'), response.status)
  return response.json() as Promise<AdminNotificationPreferences>
}
