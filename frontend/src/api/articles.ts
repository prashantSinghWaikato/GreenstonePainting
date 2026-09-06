import { resolveContentUrl } from './projects'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type PublishedArticle = {
  slug: string
  path: string
  title: string
  shortTitle: string
  topic: string
  excerpt: string
  body: string
  readTimeMinutes: number
  image: string
  imageAlt: string
  publishedAt: string
}

type ArticleResponse = Omit<PublishedArticle, 'image'> & { imageUrl: string }

function mapArticle(article: ArticleResponse): PublishedArticle {
  return { ...article, image: resolveContentUrl(article.imageUrl) }
}

export async function getPublishedArticles() {
  const response = await fetch(`${API_BASE_URL}/api/articles`)
  if (!response.ok) throw new Error('Published articles could not be loaded.')
  return ((await response.json()) as ArticleResponse[]).map(mapArticle)
}

export async function getPublishedArticle(slug: string) {
  const response = await fetch(`${API_BASE_URL}/api/articles/${encodeURIComponent(slug)}`)
  if (!response.ok) throw new Error('Article could not be loaded.')
  return mapArticle((await response.json()) as ArticleResponse)
}
