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

type ServiceResponse = Omit<PublishedService, 'image'> & { imageUrl: string | null }

export async function getPublishedServices() {
  const response = await fetch(`${API_BASE_URL}/api/services`)
  if (!response.ok) throw new Error('Painting services could not be loaded.')
  return ((await response.json()) as ServiceResponse[]).map((service) => {
    // Keep bundled imagery current while older servers await the content migration.
    if (service.slug === 'roof-painting' && service.imageUrl === '/images/greenstone-before-after.jpg') {
      return { ...service, image: '', imageAlt: '' }
    }
    if (service.slug === 'deck-fence-staining' && ['/images/greenstone-exterior.webp', '/images/projects/staining-11.webp'].includes(service.imageUrl ?? '')) {
      return { ...service, image: '/images/projects/staining-07.webp', imageAlt: 'Timber deck during stain application' }
    }
    return { ...service, image: service.imageUrl ? resolveContentUrl(service.imageUrl) : '' }
  })
}
