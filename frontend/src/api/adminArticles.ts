import { getAdminMutationHeaders } from './adminAuth'
import { resolveContentUrl } from './projects'
import type { ProjectStatus } from './adminProjects'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type AdminArticleSummary = {
  id: string
  slug: string
  title: string
  topic: string
  status: ProjectStatus
  imageUrl: string | null
  publishedAt: string | null
  updatedAt: string
  version: number
}

export type AdminArticleActivity = {
  id: string
  type: 'CREATED' | 'UPDATED' | 'IMAGE_CHANGED' | 'IMAGE_REMOVED' | 'PUBLISHED' | 'UNPUBLISHED'
  summary: string
  actorDisplayName: string
  createdAt: string
}

export type AdminArticleDetail = {
  id: string
  slug: string
  title: string
  shortTitle: string
  topic: string
  excerpt: string
  body: string
  readTimeMinutes: number
  status: ProjectStatus
  imageUrl: string | null
  imageAlt: string | null
  imageFilename: string | null
  imageSizeBytes: number | null
  publishedAt: string | null
  createdAt: string
  updatedAt: string
  version: number
  activities: AdminArticleActivity[]
}

export type SaveArticleInput = {
  title: string
  shortTitle: string
  topic: string
  excerpt: string
  body: string
  readTimeMinutes: number
  version: number
}

export class AdminArticleApiError extends Error {
  status: number
  constructor(message: string, status: number) {
    super(message)
    this.name = 'AdminArticleApiError'
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
  if (!response.ok) throw new AdminArticleApiError(await errorMessage(response, 'Articles could not be loaded.'), response.status)
  return response.json() as Promise<T>
}

async function mutate<T>(path: string, method: 'POST' | 'PATCH' | 'DELETE', body?: BodyInit) {
  const headers = new Headers(await getAdminMutationHeaders())
  if (body instanceof FormData) headers.delete('Content-Type')
  const response = await fetch(`${API_BASE_URL}${path}`, { method, credentials: 'include', headers, body })
  if (!response.ok) throw new AdminArticleApiError(await errorMessage(response, 'The article could not be saved.'), response.status)
  return response.json() as Promise<T>
}

export const adminArticleImageUrl = (path: string) => resolveContentUrl(path)
export const listAdminArticles = () => read<AdminArticleSummary[]>('/api/admin/content/articles')
export const getAdminArticle = (id: string) => read<AdminArticleDetail>(`/api/admin/content/articles/${encodeURIComponent(id)}`)
export const createAdminArticle = (input: SaveArticleInput) => mutate<AdminArticleDetail>('/api/admin/content/articles', 'POST', JSON.stringify(input))
export const updateAdminArticle = (id: string, input: SaveArticleInput) => mutate<AdminArticleDetail>(`/api/admin/content/articles/${encodeURIComponent(id)}`, 'PATCH', JSON.stringify(input))
export const setAdminArticlePublication = (article: AdminArticleDetail, status: 'DRAFT' | 'PUBLISHED') => mutate<AdminArticleDetail>(`/api/admin/content/articles/${encodeURIComponent(article.id)}/publication`, 'PATCH', JSON.stringify({ status, version: article.version }))

export function uploadAdminArticleImage(id: string, file: File, altText: string) {
  const form = new FormData()
  form.append('file', file)
  form.append('altText', altText)
  return mutate<AdminArticleDetail>(`/api/admin/content/articles/${encodeURIComponent(id)}/image`, 'POST', form)
}

export const removeAdminArticleImage = (id: string) => mutate<AdminArticleDetail>(`/api/admin/content/articles/${encodeURIComponent(id)}/image`, 'DELETE')
