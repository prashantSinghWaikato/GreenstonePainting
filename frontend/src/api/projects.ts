const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type PublishedProject = {
  slug: string
  title: string
  category: string
  location: string
  summary: string
  highlights: string[]
  image: string
  alt: string
}

type PublishedProjectResponse = Omit<PublishedProject, 'image' | 'alt'> & {
  imageUrl: string
  imageAlt: string
}

export function resolveContentUrl(path: string) {
  return path.startsWith('/api/') ? `${API_BASE_URL}${path}` : path
}

export async function getPublishedProjects(): Promise<PublishedProject[]> {
  const response = await fetch(`${API_BASE_URL}/api/projects`)
  if (!response.ok) throw new Error('Published projects could not be loaded.')
  const projects = (await response.json()) as PublishedProjectResponse[]
  return projects.map((project) => ({
    ...project,
    category: project.slug === 'new-build-interior-package' && project.category === 'New Builds & Renovations'
      ? 'Interior Painting'
      : project.slug === 'residential-transformation' && project.category === 'Exterior Painting'
        ? 'New Builds & Renovations'
        : project.category,
    image: project.slug === 'contemporary-exterior-renewal' && project.imageUrl === '/images/greenstone-exterior.webp'
      ? '/images/projects/featured-townhouse-exterior.webp'
      : project.slug === 'new-build-interior-package' && project.imageUrl === '/images/greenstone-kitchen.webp'
        ? '/images/projects/featured-interior-bedroom.webp'
        : project.slug === 'residential-transformation' && project.imageUrl === '/images/greenstone-before-after.jpg'
          ? '/images/projects/featured-twilight-exterior.webp'
          : resolveContentUrl(project.imageUrl),
    alt: project.slug === 'contemporary-exterior-renewal' && project.imageUrl === '/images/greenstone-exterior.webp'
      ? 'White and charcoal multi-unit townhouse exterior'
      : project.slug === 'new-build-interior-package' && project.imageUrl === '/images/greenstone-kitchen.webp'
        ? 'Freshly painted white bedroom with decorative ceiling panels'
        : project.slug === 'residential-transformation' && project.imageUrl === '/images/greenstone-before-after.jpg'
          ? 'Twilight view of two freshly painted modern homes'
          : project.imageAlt,
  }))
}
