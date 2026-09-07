import { getAdminMutationHeaders } from './adminAuth'
import { resolveContentUrl } from './projects'
import type { ProjectStatus } from './adminProjects'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type AdminServiceSummary = {
  id: string
  slug: string
  title: string
  label: string
  displayOrder: number
  status: ProjectStatus
  imageUrl: string | null
  updatedAt: string
  version: number
}

export type AdminServiceActivity = {
  id: string
  type: 'UPDATED' | 'IMAGE_CHANGED' | 'IMAGE_REMOVED' | 'PUBLISHED' | 'UNPUBLISHED'
  summary: string
  actorDisplayName: string
  createdAt: string
}

export type AdminServiceDetail = {
  id: string
  slug: string
  title: string
  label: string
  summary: string
  description: string
  inclusions: string[]
  note: string
  displayOrder: number
  status: ProjectStatus
  imageUrl: string | null
  imageAlt: string | null
  imageFilename: string | null
  imageSizeBytes: number | null
  publishedAt: string | null
  createdAt: string
  updatedAt: string
  version: number
  activities: AdminServiceActivity[]
}

export type SaveServiceInput = {
  title: string
  label: string
  summary: string
  description: string
  inclusions: string[]
  note: string
  displayOrder: number
  version: number
}

export class AdminServiceApiError extends Error {
  status: number
  constructor(message: string, status: number) {
    super(message)
    this.name = 'AdminServiceApiError'
    this.status = status
  }
}

async function errorMessage(response: Response, fallback: string) {
  try {
    const body = (await response.json()) as { message?: string }
    return body.message || fallback
  } catch { return fallback }
}

async function read<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include' })
  if (!response.ok) throw new AdminServiceApiError(await errorMessage(response, 'Services could not be loaded.'), response.status)
  return response.json() as Promise<T>
}

async function mutate<T>(path: string, method: 'POST' | 'PATCH' | 'DELETE', body?: BodyInit) {
  const headers = new Headers(await getAdminMutationHeaders())
  if (body instanceof FormData) headers.delete('Content-Type')
  const response = await fetch(`${API_BASE_URL}${path}`, { method, credentials: 'include', headers, body })
  if (!response.ok) throw new AdminServiceApiError(await errorMessage(response, 'The service could not be saved.'), response.status)
  return response.json() as Promise<T>
}

export const adminServiceImageUrl = (path: string) => resolveContentUrl(path)
export const listAdminServices = () => read<AdminServiceSummary[]>('/api/admin/content/services')
export const getAdminService = (id: string) => read<AdminServiceDetail>(`/api/admin/content/services/${encodeURIComponent(id)}`)
export const updateAdminService = (id: string, input: SaveServiceInput) => mutate<AdminServiceDetail>(`/api/admin/content/services/${encodeURIComponent(id)}`, 'PATCH', JSON.stringify(input))
export const setAdminServicePublication = (service: AdminServiceDetail, status: 'DRAFT' | 'PUBLISHED') => mutate<AdminServiceDetail>(`/api/admin/content/services/${encodeURIComponent(service.id)}/publication`, 'PATCH', JSON.stringify({ status, version: service.version }))

export function uploadAdminServiceImage(id: string, file: File, altText: string) {
  const form = new FormData()
  form.append('file', file)
  form.append('altText', altText)
  return mutate<AdminServiceDetail>(`/api/admin/content/services/${encodeURIComponent(id)}/image`, 'POST', form)
}

export const removeAdminServiceImage = (id: string) => mutate<AdminServiceDetail>(`/api/admin/content/services/${encodeURIComponent(id)}/image`, 'DELETE')
