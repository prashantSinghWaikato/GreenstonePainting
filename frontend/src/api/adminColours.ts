import { getAdminMutationHeaders } from './adminAuth'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type AdminColour = {
  id: string
  name: string
  hex: string
  reseneUrl: string | null
  active: boolean
  displayOrder: number
  defaultInterior: boolean
  defaultExterior: boolean
  version: number
  updatedAt: string
}

export type SaveColourInput = Omit<AdminColour, 'id' | 'updatedAt'>

export class AdminColourApiError extends Error {
  status: number
  constructor(message: string, status: number) {
    super(message)
    this.name = 'AdminColourApiError'
    this.status = status
  }
}

async function message(response: Response, fallback: string) {
  try {
    const body = await response.json() as { message?: string }
    return body.message || fallback
  } catch { return fallback }
}

export async function listAdminColours() {
  const response = await fetch(`${API_BASE_URL}/api/admin/content/colours`, { credentials: 'include' })
  if (!response.ok) throw new AdminColourApiError(await message(response, 'Colours could not be loaded.'), response.status)
  return response.json() as Promise<AdminColour[]>
}

async function save(path: string, method: 'POST' | 'PATCH', input: SaveColourInput) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    credentials: 'include',
    headers: await getAdminMutationHeaders(),
    body: JSON.stringify(input),
  })
  if (!response.ok) throw new AdminColourApiError(await message(response, 'The colour could not be saved.'), response.status)
  return response.json() as Promise<AdminColour>
}

export const createAdminColour = (input: SaveColourInput) => save('/api/admin/content/colours', 'POST', input)
export const updateAdminColour = (id: string, input: SaveColourInput) => save(`/api/admin/content/colours/${encodeURIComponent(id)}`, 'PATCH', input)

export async function deleteAdminColour(id: string) {
  const response = await fetch(`${API_BASE_URL}/api/admin/content/colours/${encodeURIComponent(id)}`, {
    method: 'DELETE', credentials: 'include', headers: await getAdminMutationHeaders(),
  })
  if (!response.ok) throw new AdminColourApiError(await message(response, 'The colour could not be removed.'), response.status)
}
