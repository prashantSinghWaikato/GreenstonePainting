export type CreateEnquiryRequest = {
  firstName: string
  lastName: string
  email: string
  phone: string
  serviceSlug: string
  propertyAddress: string
  message: string
}

export type EnquiryResponse = {
  id: string
  status: 'NEW'
  createdAt: string
  notificationSent: boolean
}

type ApiError = {
  message?: string
  fieldErrors?: Record<string, string>
}

export class EnquiryApiError extends Error {
  fieldErrors: Record<string, string>

  constructor(message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'EnquiryApiError'
    this.fieldErrors = fieldErrors
  }
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export async function createEnquiry(request: CreateEnquiryRequest): Promise<EnquiryResponse> {
  const response = await fetch(`${apiBaseUrl}/api/enquiries`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({})) as ApiError
    throw new EnquiryApiError(
      error.message ?? 'We could not submit your request. Please try again.',
      error.fieldErrors ?? {},
    )
  }

  return response.json() as Promise<EnquiryResponse>
}
