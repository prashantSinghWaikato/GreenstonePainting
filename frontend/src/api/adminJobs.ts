import { getAdminMutationHeaders } from './adminAuth'

const API = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')
export type JobStatus = 'PLANNED' | 'SCHEDULED' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED'
export type JobPhotoPhase = 'BEFORE' | 'PROGRESS' | 'COMPLETED'
export type JobSummary = {
  id: string; jobNumber: string; status: JobStatus; customerName: string; title: string; serviceTitle: string | null
  propertyAddress: string | null; assignedAdminId: string | null; assignedDisplayName: string | null
  scheduledStartDate: string | null; scheduledEndDate: string | null; photoCount: number
  quoteId: string; quoteNumber: string; quoteTotal: number; updatedAt: string
}
export type JobDetail = JobSummary & {
  enquiryId: string; version: number; customerEmail: string; customerPhone: string | null; scope: string
  siteInstructions: string | null; internalNotes: string | null; actualStartedAt: string | null; completedAt: string | null
  createdAt: string
  photos: Array<{ id: string; phase: JobPhotoPhase; filename: string; contentType: string; sizeBytes: number; createdAt: string }>
  checklist: Array<{ id: string; label: string; position: number; completed: boolean; completedAt: string | null; completedBy: string | null }>
  customerSignoffName: string | null; customerSignoffAt: string | null
  invoice: null | { id: string; invoiceNumber: string; status: string; total: number; amountPaid: number; balanceDue: number }
  activities: Array<{ id: string; type: string; summary: string; noteBody: string | null; actorDisplayName: string; createdAt: string }>
}
export type JobMetrics = { planned: number; scheduled: number; inProgress: number; completed: number }
export type JobPage = { items: JobSummary[]; staff: Array<{ id: string; displayName: string; email: string; enabled: boolean }>; metrics: JobMetrics }
export type JobOverview = { metrics: JobMetrics; upcoming: JobSummary[] }

export class JobApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'JobApiError'
    this.status = status
  }
}
async function error(response: Response, fallback: string) { try { const body = await response.json() as { message?: string }; return new JobApiError(body.message || fallback, response.status) } catch { return new JobApiError(fallback, response.status) } }
async function json<T>(path: string, init?: RequestInit, fallback = 'The job request could not be completed.') { const response = await fetch(`${API}${path}`, { credentials: 'include', ...init }); if (!response.ok) throw await error(response, fallback); return response.json() as Promise<T> }

export function listJobs(query = '', status = '', assignment = '') { const p = new URLSearchParams(); if (query.trim()) p.set('q', query.trim()); if (status) p.set('status', status); if (assignment) p.set('assignment', assignment); return json<JobPage>(`/api/admin/jobs?${p}`, undefined, 'Jobs could not be loaded.') }
export function getJob(id: string) { return json<JobDetail>(`/api/admin/jobs/${encodeURIComponent(id)}`, undefined, 'The job could not be loaded.') }
export function getJobOverview() { return json<JobOverview>('/api/admin/jobs/overview', undefined, 'Job overview could not be loaded.') }
export async function createJobFromQuote(quoteId: string) { return json<JobDetail>(`/api/admin/quotes/${encodeURIComponent(quoteId)}/job`, { method: 'POST', headers: await getAdminMutationHeaders() }, 'The job could not be created.') }
export async function updateJob(id: string, update: { status: JobStatus; assignedAdminId: string | null; scheduledStartDate: string | null; scheduledEndDate: string | null; siteInstructions: string | null; internalNotes: string | null; version: number }) { return json<JobDetail>(`/api/admin/jobs/${encodeURIComponent(id)}`, { method: 'PATCH', headers: await getAdminMutationHeaders(), body: JSON.stringify(update) }, 'The job could not be saved.') }
export async function uploadJobPhoto(id: string, phase: JobPhotoPhase, file: File) { const headers: Record<string, string> = await getAdminMutationHeaders(); delete headers['Content-Type']; const body = new FormData(); body.append('file', file); return json<JobDetail>(`/api/admin/jobs/${encodeURIComponent(id)}/photos?phase=${phase}`, { method: 'POST', headers, body }, 'The job photo could not be uploaded.') }
export async function updateJobChecklist(jobId: string, itemId: string, completed: boolean) { return json<JobDetail>(`/api/admin/jobs/${encodeURIComponent(jobId)}/checklist/${encodeURIComponent(itemId)}`, { method: 'PATCH', headers: await getAdminMutationHeaders(), body: JSON.stringify({ completed }) }, 'The checklist could not be updated.') }
export async function recordJobSignoff(jobId: string, customerName: string) { return json<JobDetail>(`/api/admin/jobs/${encodeURIComponent(jobId)}/signoff`, { method: 'POST', headers: await getAdminMutationHeaders(), body: JSON.stringify({ customerName }) }, 'Customer sign-off could not be recorded.') }
export async function getJobPhoto(jobId: string, photoId: string) { const response = await fetch(`${API}/api/admin/jobs/${encodeURIComponent(jobId)}/photos/${encodeURIComponent(photoId)}`, { credentials: 'include' }); if (!response.ok) throw await error(response, 'The job photo could not be loaded.'); return response.blob() }
