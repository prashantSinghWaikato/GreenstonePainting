export type CustomerReview = {
  author: string
  rating: number
  text: string
  source?: string
}

// Add approved customer reviews here. The public site renders this local data
// directly and does not call Google Places or another review API.
export const customerReviews: CustomerReview[] = []
