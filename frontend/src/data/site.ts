export type Service = {
  slug: string
  code: string
  title: string
  description: string
}

export type Project = {
  title: string
  category: string
  location: string
  image: string
  alt: string
  summary: string
}

export type HeroSlide = {
  image: string
  label: string
}

export const heroSlides: HeroSlide[] = [
  { image: '/images/greenstone-exterior.webp', label: 'Exterior painting · Waikato' },
  { image: '/images/greenstone-kitchen.webp', label: 'New build finish · Hamilton' },
  { image: '/images/greenstone-bedroom.webp', label: 'Interior painting · Waikato' },
]

export const services: Service[] = [
  {
    slug: 'interior-painting',
    code: 'IN',
    title: 'Interior Painting',
    description: 'Walls, ceilings, trim, and detailed finishes delivered with careful protection and preparation.',
  },
  {
    slug: 'exterior-painting',
    code: 'EX',
    title: 'Exterior Painting',
    description: 'Weather-ready coating systems selected for Waikato properties and New Zealand conditions.',
  },
  {
    slug: 'commercial-painting',
    code: 'CO',
    title: 'Commercial Painting',
    description: 'Structured delivery for offices, retail, developments, and operational commercial spaces.',
  },
  {
    slug: 'roof-painting',
    code: 'RF',
    title: 'Roof Painting',
    description: 'Cleaning, preparation, and protective roof coatings that restore appearance and resilience.',
  },
  {
    slug: 'new-builds-renovations',
    code: 'NB',
    title: 'New Builds & Renovations',
    description: 'Reliable coordination and consistent finishes from early preparation through final touch-ups.',
  },
  {
    slug: 'deck-fence-staining',
    code: 'DS',
    title: 'Deck & Fence Staining',
    description: 'Specialist staining and sealing that protects timber while showcasing its natural character.',
  },
]

export const projects: Project[] = [
  {
    title: 'Contemporary Exterior Renewal',
    category: 'Exterior painting',
    location: 'Hamilton, Waikato',
    image: '/images/greenstone-exterior.webp',
    alt: 'Freshly painted modern residential exterior',
    summary: 'A precise multi-surface exterior finish designed to complement modern architectural lines.',
  },
  {
    title: 'New Build Interior Package',
    category: 'New build finish',
    location: 'Waikato',
    image: '/images/greenstone-kitchen.webp',
    alt: 'Modern kitchen and living area with crisp painted surfaces',
    summary: 'A coordinated interior package with clean transitions across walls, ceilings, trim, and cabinetry.',
  },
  {
    title: 'Residential Transformation',
    category: 'Before & after',
    location: 'Hamilton',
    image: '/images/greenstone-before-after.jpg',
    alt: 'Before and after exterior painting transformation',
    summary: 'A complete exterior refresh that modernised the property while improving surface protection.',
  },
]

export const serviceAreas = [
  'Hamilton',
  'Cambridge',
  'Tamahere',
  'Te Awamutu',
  'Ngaruawahia',
  'Raglan',
  'Pukekohe',
  'Taupō',
]

export const articles = [
  { date: '19 Sep 2025', title: 'How Painters Prepare Your Home for a Smooth Paint Job', topic: 'Preparation', path: '/how-painters-prepare-your-home-for-a-smooth-paint-job/' },
  { date: '24 Aug 2025', title: 'A Guide to Compliance-Friendly Paint Options', topic: 'Materials', path: '/822-2/' },
  { date: '14 Aug 2025', title: 'Wood Staining Benefits You Need to Know', topic: 'Care guide', path: '/wood-staining-benefits-you-need-to-take-advantage-of/' },
]
