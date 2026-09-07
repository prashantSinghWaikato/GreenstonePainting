import { resolveContentUrl } from './projects'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type PublishedService = {
  slug: string
  title: string
  label: string
  summary: string
  description: string
  inclusions: string[]
  note: string
  displayOrder: number
  image: string
  imageAlt: string
}

type ServiceResponse = Omit<PublishedService, 'image'> & { imageUrl: string }

export async function getPublishedServices() {
  const response = await fetch(`${API_BASE_URL}/api/services`)
  if (!response.ok) throw new Error('Painting services could not be loaded.')
  return ((await response.json()) as ServiceResponse[]).map((service) => ({ ...service, image: resolveContentUrl(service.imageUrl) }))
}
