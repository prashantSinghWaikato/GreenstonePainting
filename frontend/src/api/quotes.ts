const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type PublicQuote = {
  quoteNumber: string
  revisionNumber: number
  status: 'SENT' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED' | 'SUPERSEDED'
  customerName: string
  propertyAddress: string | null
  title: string
  scope: string
  terms: string
  subtotal: number
  gstAmount: number
  total: number
  optionalTotal: number
  validUntil: string
  estimatedStartDate: string | null
  estimatedEndDate: string | null
  items: Array<{
    id: string
    category: 'LABOUR' | 'MATERIALS' | 'PREPARATION' | 'OPTIONAL' | 'OTHER'
    description: string
    quantity: number
    unit: string
    unitPrice: number
    lineTotal: number
    optional: boolean
  }>
}

async function errorMessage(response: Response, fallback: string) {
  try {
    const body = (await response.json()) as { message?: string }
    return body.message || fallback
  } catch { return fallback }
}

export async function getPublicQuote(token: string) {
  const response = await fetch(`${API_BASE_URL}/api/quotes/response?token=${encodeURIComponent(token)}`)
  if (!response.ok) throw new Error(await errorMessage(response, 'This quote could not be loaded.'))
  return response.json() as Promise<PublicQuote>
}

export async function respondToQuote(token: string, decision: 'ACCEPT' | 'DECLINE', reason?: string) {
  const response = await fetch(`${API_BASE_URL}/api/quotes/response?token=${encodeURIComponent(token)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ decision, reason: reason?.trim() || null }),
  })
  if (!response.ok) throw new Error(await errorMessage(response, 'Your response could not be saved.'))
  return response.json() as Promise<PublicQuote>
}

export async function downloadPublicQuotePdf(token: string) {
  const response = await fetch(`${API_BASE_URL}/api/quotes/pdf?token=${encodeURIComponent(token)}`)
  if (!response.ok) throw new Error(await errorMessage(response, 'The quote PDF could not be downloaded.'))
  return response.blob()
}
