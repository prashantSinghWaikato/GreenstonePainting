import { getAdminMutationHeaders } from './adminAuth'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type EnquiryStatus = 'NEW' | 'IN_REVIEW' | 'CONTACTED' | 'QUOTED' | 'WON' | 'LOST' | 'CLOSED'
export type EnquiryPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
export type EnquiryFollowUpFilter = 'OVERDUE' | 'TODAY' | 'UPCOMING'

export type AdminEnquirySummary = {
  id: string
  reference: string
  firstName: string
  lastName: string
  email: string
  phone: string | null
  serviceSlug: string | null
  serviceTitle: string | null
  propertyAddress: string | null
  status: EnquiryStatus
  assignedAdminId: string | null
  assignedDisplayName: string | null
  priority: EnquiryPriority
  followUpAt: string | null
  overdue: boolean
  attachmentCount: number
  createdAt: string
  completedAt: string | null
}

export type AdminEnquiryAttachment = {
  id: string
  filename: string
  contentType: string
  sizeBytes: number
  createdAt: string
}

export type AdminEnquiryDetail = Omit<AdminEnquirySummary, 'attachmentCount'> & {
  version: number
  type: 'GENERAL' | 'QUOTE_REQUEST'
  contactPreference: 'EMAIL' | 'PHONE' | 'EITHER'
  suburb: string | null
  message: string
  estimatedBudget: number | null
  desiredStartDate: string | null
  internalNotes: string | null
  updatedAt: string
  notificationSentAt: string | null
  attachments: AdminEnquiryAttachment[]
  activities: AdminEnquiryActivity[]
}

export type AdminEnquiryActivity = {
  id: string
  type: 'STATUS_CHANGED' | 'NOTE_UPDATED' | 'NOTE_ADDED' | 'ASSIGNMENT_CHANGED' | 'PRIORITY_CHANGED' | 'FOLLOW_UP_CHANGED' | 'NOTIFICATION_SENT' | 'NOTIFICATION_FAILED'
  previousStatus: EnquiryStatus | null
  newStatus: EnquiryStatus | null
  summary: string
  noteBody: string | null
  actorDisplayName: string
  createdAt: string
}

export type AdminEnquiryPage = {
  items: AdminEnquirySummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  statusCounts: Record<EnquiryStatus, number>
  services: Array<{ slug: string; title: string }>
  staff: Array<{ id: string; displayName: string; email: string; enabled: boolean }>
  metrics: { unassigned: number; dueToday: number; overdue: number; mine: number }
}

export type AdminEnquiryFilters = {
  query: string
  status: '' | EnquiryStatus
  service: string
  assignment: '' | 'mine' | 'unassigned' | string
  priority: '' | EnquiryPriority
  followUp: '' | EnquiryFollowUpFilter
  from: string
  to: string
}

export class AdminEnquiryApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'AdminEnquiryApiError'
    this.status = status
  }
}

async function request<T>(path: string, fallback: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include' })
  if (!response.ok) {
    let message = fallback
    try {
      const body = (await response.json()) as { message?: string }
      message = body.message || fallback
    } catch {
      // Keep the user-friendly fallback when an intermediary returns non-JSON.
    }
    throw new AdminEnquiryApiError(message, response.status)
  }
  return response.json() as Promise<T>
}

export function listAdminEnquiries(filters: AdminEnquiryFilters, page: number, size = 20) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (filters.query.trim()) params.set('q', filters.query.trim())
  if (filters.status) params.set('status', filters.status)
  if (filters.service) params.set('service', filters.service)
  if (filters.assignment) params.set('assignment', filters.assignment)
  if (filters.priority) params.set('priority', filters.priority)
  if (filters.followUp) params.set('followUp', filters.followUp)
  if (filters.from) params.set('from', filters.from)
  if (filters.to) params.set('to', filters.to)
  return request<AdminEnquiryPage>(`/api/admin/enquiries?${params}`, 'The enquiry inbox could not be loaded.')
}

export function getAdminEnquiry(enquiryId: string) {
  return request<AdminEnquiryDetail>(`/api/admin/enquiries/${encodeURIComponent(enquiryId)}`, 'The enquiry could not be loaded.')
}

export async function getAdminEnquiryPhoto(enquiryId: string, attachmentId: string) {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/enquiries/${encodeURIComponent(enquiryId)}/attachments/${encodeURIComponent(attachmentId)}`,
    { credentials: 'include' },
  )
  if (!response.ok) {
    throw new AdminEnquiryApiError('The project photo could not be loaded.', response.status)
  }
  return response.blob()
}

export async function updateAdminEnquiryWorkflow(
  enquiryId: string,
  update: {
    status: EnquiryStatus
    assignedAdminId: string | null
    priority: EnquiryPriority
    followUpAt: string | null
    newNote: string | null
    version: number
  },
) {
  const response = await fetch(
    `${API_BASE_URL}/api/admin/enquiries/${encodeURIComponent(enquiryId)}/workflow`,
    {
      method: 'PATCH',
      credentials: 'include',
      headers: await getAdminMutationHeaders(),
      body: JSON.stringify(update),
    },
  )
  if (!response.ok) {
    let message = 'The enquiry workflow could not be saved.'
    try {
      const body = (await response.json()) as { message?: string }
      message = body.message || message
    } catch {
      // Keep the user-friendly fallback when an intermediary returns non-JSON.
    }
    throw new AdminEnquiryApiError(message, response.status)
  }
  return response.json() as Promise<AdminEnquiryDetail>
}
