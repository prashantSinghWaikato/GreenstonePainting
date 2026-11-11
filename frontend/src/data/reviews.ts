export type CustomerReview = {
  author: string
  rating: number
  text: string
  source?: string
}

// Approved customer reviews are rendered directly from this local data. The
// public site does not call Google Places or another review API.
export const customerReviews: CustomerReview[] = [
  {
    author: 'Batinder Singh',
    rating: 5,
    text: 'Greenstone Painting stained the exterior of my house and did a wonderful job. The team was professional, the results are absolutely beautiful. Highly recommend them for any painting or staining needs.',
    source: 'Google review',
  },
  {
    author: 'ashleigh A',
    rating: 5,
    text: 'We have had greenstone patining here at our cafe in tirau painting our building and deck they have done and awesome job there friendly and reliable and were so pleased with the final results we highly recommend them!!!',
    source: 'Google review',
  },
  {
    author: 'gagan singh',
    rating: 5,
    text: 'Highly recommend this company. The team were professional, polite, efficient and a pleasure to deal with. Ajay was responsive to my questions and had the patience of a saint when I was trying to decide on colors. I LOVE the new look. Thank you Green Stone Painting you guys were really amazing with your work.',
    source: 'Google review',
  },
]
