import { getAdminMutationHeaders } from './adminAuth'
import { resolveContentUrl } from './projects'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type ProjectStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
export type ProjectImagePhase = 'BEFORE' | 'AFTER' | 'GALLERY'

export type AdminProjectSummary = {
  id: string
  slug: string
  title: string
  location: string
  serviceTitle: string | null
  status: ProjectStatus
  featured: boolean
  imageUrl: string | null
  updatedAt: string
  version: number
}

export type AdminProjectImage = {
  id: string
  altText: string
  phase: ProjectImagePhase
  displayOrder: number
  imageUrl: string
  originalFilename: string
  contentType: string
  sizeBytes: number
}

export type AdminProjectActivity = {
  id: string
  type: 'CREATED' | 'UPDATED' | 'IMAGE_ADDED' | 'IMAGE_REMOVED' | 'PUBLISHED' | 'UNPUBLISHED'
  summary: string
  actorDisplayName: string
  createdAt: string
}

export type AdminProjectDetail = {
  id: string
  slug: string
  title: string
  summary: string
  description: string
  location: string
  completedOn: string | null
  serviceSlug: string | null
  serviceTitle: string | null
  status: ProjectStatus
  featured: boolean
  publishedAt: string | null
  createdAt: string
  updatedAt: string
  version: number
  images: AdminProjectImage[]
  activities: AdminProjectActivity[]
}

export type AdminProjectDashboard = {
  projects: AdminProjectSummary[]
  services: Array<{ slug: string; title: string }>
}

export type SaveProjectInput = {
  title: string
  summary: string
  description: string
  location: string
  completedOn: string | null
  serviceSlug: string | null
  featured: boolean
  version: number
}

export class AdminProjectApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'AdminProjectApiError'
    this.status = status
  }
}

async function message(response: Response, fallback: string) {
  try {
    const body = (await response.json()) as { message?: string }
    return body.message || fallback
  } catch {
    return fallback
  }
}

async function read<T>(path: string, fallback: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include' })
  if (!response.ok) throw new AdminProjectApiError(await message(response, fallback), response.status)
  return response.json() as Promise<T>
}

async function mutate<T>(path: string, method: 'POST' | 'PATCH' | 'DELETE', body?: BodyInit) {
  const headers = new Headers(await getAdminMutationHeaders())
  if (body instanceof FormData) headers.delete('Content-Type')
  const response = await fetch(`${API_BASE_URL}${path}`, { method, credentials: 'include', headers, body })
  if (!response.ok) throw new AdminProjectApiError(await message(response, 'The project could not be saved.'), response.status)
  return response.json() as Promise<T>
}

export function adminProjectImageUrl(path: string) {
  return resolveContentUrl(path)
}

export function getAdminProjectDashboard() {
  return read<AdminProjectDashboard>('/api/admin/content/projects', 'Projects could not be loaded.')
}

export function getAdminProject(projectId: string) {
  return read<AdminProjectDetail>(`/api/admin/content/projects/${encodeURIComponent(projectId)}`, 'The project could not be loaded.')
}

export function createAdminProject(input: SaveProjectInput) {
  return mutate<AdminProjectDetail>('/api/admin/content/projects', 'POST', JSON.stringify(input))
}

export function updateAdminProject(projectId: string, input: SaveProjectInput) {
  return mutate<AdminProjectDetail>(`/api/admin/content/projects/${encodeURIComponent(projectId)}`, 'PATCH', JSON.stringify(input))
}

export function setAdminProjectPublication(project: AdminProjectDetail, status: 'DRAFT' | 'PUBLISHED') {
  return mutate<AdminProjectDetail>(`/api/admin/content/projects/${encodeURIComponent(project.id)}/publication`, 'PATCH', JSON.stringify({ status, version: project.version }))
}

export function uploadAdminProjectImage(projectId: string, file: File, altText: string, phase: ProjectImagePhase) {
  const form = new FormData()
  form.append('file', file)
  form.append('altText', altText)
  form.append('phase', phase)
  return mutate<AdminProjectDetail>(`/api/admin/content/projects/${encodeURIComponent(projectId)}/images`, 'POST', form)
}

export function removeAdminProjectImage(projectId: string, imageId: string) {
  return mutate<AdminProjectDetail>(`/api/admin/content/projects/${encodeURIComponent(projectId)}/images/${encodeURIComponent(imageId)}`, 'DELETE')
}
