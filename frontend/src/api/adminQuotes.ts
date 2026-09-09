import { getAdminMutationHeaders } from './adminAuth'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type QuoteStatus = 'DRAFT' | 'SENT' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED' | 'SUPERSEDED'
export type QuoteItemCategory = 'LABOUR' | 'MATERIALS' | 'PREPARATION' | 'OPTIONAL' | 'OTHER'

export type AdminQuoteItem = {
  id: string
  category: QuoteItemCategory
  description: string
  quantity: number
  unit: string
  unitPrice: number
  lineTotal: number
  optional: boolean
}

export type AdminQuoteSummary = {
  id: string
  quoteNumber: string
  revisionNumber: number
  status: QuoteStatus
  total: number
  validUntil: string
  updatedAt: string
  sentAt: string | null
}

export type AdminQuoteDetail = AdminQuoteSummary & {
  enquiryId: string
  version: number
  customerName: string
  customerEmail: string
  propertyAddress: string | null
  title: string
  scope: string
  terms: string
  gstRate: number
  subtotal: number
  gstAmount: number
  optionalTotal: number
  estimatedStartDate: string | null
  estimatedEndDate: string | null
  acceptedAt: string | null
  declinedAt: string | null
  declineReason: string | null
  createdAt: string
  items: AdminQuoteItem[]
  activities: Array<{
    id: string
    type: 'CREATED' | 'UPDATED' | 'SENT' | 'ACCEPTED' | 'DECLINED' | 'REVISION_CREATED'
    summary: string
    actorDisplayName: string
    createdAt: string
  }>
}

export type SaveAdminQuote = {
  customerName: string
  customerEmail: string
  propertyAddress: string | null
  title: string
  scope: string
  terms: string
  validUntil: string
  estimatedStartDate: string | null
  estimatedEndDate: string | null
  items: Array<{
    category: QuoteItemCategory
    description: string
    quantity: number
    unit: string
    unitPrice: number
    optional: boolean
  }>
  version: number
}

export class AdminQuoteApiError extends Error {
  status: number
  fieldErrors: Record<string, string>

  constructor(message: string, status: number, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'AdminQuoteApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function responseError(response: Response, fallback: string) {
  try {
    const body = (await response.json()) as { message?: string; fieldErrors?: Record<string, string> }
    return new AdminQuoteApiError(body.message || fallback, response.status, body.fieldErrors)
  } catch {
    return new AdminQuoteApiError(fallback, response.status)
  }
}

async function request<T>(path: string, init: RequestInit | undefined, fallback: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include', ...init })
  if (!response.ok) throw await responseError(response, fallback)
  return response.json() as Promise<T>
}

export function listAdminQuotes(enquiryId: string) {
  return request<AdminQuoteSummary[]>(`/api/admin/enquiries/${encodeURIComponent(enquiryId)}/quotes`, undefined, 'Quote history could not be loaded.')
}

export function createAdminQuote(enquiryId: string) {
  return getAdminMutationHeaders().then((headers) => request<AdminQuoteDetail>(
    `/api/admin/enquiries/${encodeURIComponent(enquiryId)}/quotes`, { method: 'POST', headers }, 'The quote draft could not be created.',
  ))
}

export function getAdminQuote(quoteId: string) {
  return request<AdminQuoteDetail>(`/api/admin/quotes/${encodeURIComponent(quoteId)}`, undefined, 'The quote could not be loaded.')
}

export function saveAdminQuote(quoteId: string, input: SaveAdminQuote) {
  return getAdminMutationHeaders().then((headers) => request<AdminQuoteDetail>(
    `/api/admin/quotes/${encodeURIComponent(quoteId)}`, { method: 'PUT', headers, body: JSON.stringify(input) }, 'The quote could not be saved.',
  ))
}

export function createQuoteRevision(quoteId: string) {
  return getAdminMutationHeaders().then((headers) => request<AdminQuoteDetail>(
    `/api/admin/quotes/${encodeURIComponent(quoteId)}/revisions`, { method: 'POST', headers }, 'A new quote revision could not be created.',
  ))
}

export function sendAdminQuote(quoteId: string) {
  return getAdminMutationHeaders().then((headers) => request<AdminQuoteDetail>(
    `/api/admin/quotes/${encodeURIComponent(quoteId)}/send`, { method: 'POST', headers }, 'The quote could not be emailed.',
  ))
}

export async function downloadAdminQuotePdf(quoteId: string) {
  const response = await fetch(`${API_BASE_URL}/api/admin/quotes/${encodeURIComponent(quoteId)}/pdf`, { credentials: 'include' })
  if (!response.ok) throw await responseError(response, 'The quote PDF could not be generated.')
  return response.blob()
}
