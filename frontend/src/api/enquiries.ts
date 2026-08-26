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
  uploadToken: string
  uploadExpiresAt: string
}

export type EnquiryAttachmentResponse = {
  id: string
  originalFilename: string
  contentType: string
  sizeBytes: number
  createdAt: string
}

export type EnquiryCompletionResponse = {
  id: string
  completedAt: string
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

export async function uploadEnquiryPhoto(enquiryId: string, uploadToken: string, file: File): Promise<EnquiryAttachmentResponse> {
  const formData = new FormData()
  formData.append('file', withInferredContentType(file))

  const response = await fetch(`${apiBaseUrl}/api/enquiries/${enquiryId}/attachments`, {
    method: 'POST',
    headers: { 'X-Upload-Token': uploadToken },
    body: formData,
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({})) as ApiError
    throw new Error(error.message ?? `We could not upload ${file.name}.`)
  }

  return response.json() as Promise<EnquiryAttachmentResponse>
}

export async function completeEnquiry(enquiryId: string, uploadToken: string): Promise<EnquiryCompletionResponse> {
  const response = await fetch(`${apiBaseUrl}/api/enquiries/${enquiryId}/complete`, {
    method: 'POST',
    headers: { 'X-Upload-Token': uploadToken },
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({})) as ApiError
    throw new Error(error.message ?? 'Your request was saved, but we could not notify our team yet.')
  }

  return response.json() as Promise<EnquiryCompletionResponse>
}

function withInferredContentType(file: File): File {
  if (file.type) return file

  const extension = file.name.split('.').pop()?.toLowerCase()
  const contentType = extension === 'heic' ? 'image/heic' : extension === 'heif' ? 'image/heif' : ''
  return contentType ? new File([file], file.name, { type: contentType, lastModified: file.lastModified }) : file
}
