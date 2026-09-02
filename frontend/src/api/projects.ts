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
    image: resolveContentUrl(project.imageUrl),
    alt: project.imageAlt,
  }))
}
