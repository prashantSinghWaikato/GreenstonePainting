const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type GoogleReview = {
  authorName: string
  authorUri?: string
  authorPhotoUri?: string
  rating: number
  text: string
  relativePublishTime?: string
}

export type GoogleReviewsSummary = {
  rating: number
  reviewCount: number
  googleMapsUrl: string
  reviews: GoogleReview[]
}

export async function getGoogleReviews(): Promise<GoogleReviewsSummary | null> {
  const response = await fetch(`${API_BASE_URL}/api/reviews`, { cache: 'no-store' })
  if (response.status === 204) return null
  if (!response.ok) throw new Error('Google reviews could not be loaded.')
  return response.json() as Promise<GoogleReviewsSummary>
}
