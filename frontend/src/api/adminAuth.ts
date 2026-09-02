const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type AdminSession = {
  email: string
  displayName: string
  role: 'OWNER' | 'STAFF'
}

type ApiErrorBody = {
  message?: string
}

let csrfHeader = 'X-XSRF-TOKEN'
let csrfToken = ''

async function readError(response: Response, fallback: string) {
  try {
    const body = (await response.json()) as ApiErrorBody
    return body.message || fallback
  } catch {
    return fallback
  }
}

async function refreshCsrfToken() {
  const response = await fetch(`${API_BASE_URL}/api/admin/auth/csrf`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error(await readError(response, 'The secure sign-in service is unavailable.'))
  }

  const body = (await response.json()) as { headerName: string; token: string }
  csrfHeader = body.headerName
  csrfToken = body.token
}

export async function getAdminMutationHeaders() {
  await refreshCsrfToken()
  return {
    'Content-Type': 'application/json',
    [csrfHeader]: csrfToken,
  }
}

export async function getAdminSession(): Promise<AdminSession | null> {
  const response = await fetch(`${API_BASE_URL}/api/admin/auth/me`, {
    credentials: 'include',
  })

  if (response.status === 401) {
    return null
  }
  if (!response.ok) {
    throw new Error(await readError(response, 'We could not verify your staff session.'))
  }
  return response.json() as Promise<AdminSession>
}

export async function signInAdmin(email: string, password: string): Promise<AdminSession> {
  await refreshCsrfToken()
  const response = await fetch(`${API_BASE_URL}/api/admin/auth/login`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken,
    },
    body: JSON.stringify({ email, password }),
  })

  if (!response.ok) {
    throw new Error(await readError(response, 'Sign-in failed. Check your details and try again.'))
  }

  const session = (await response.json()) as AdminSession
  await refreshCsrfToken()
  return session
}

export async function signOutAdmin() {
  await refreshCsrfToken()
  const response = await fetch(`${API_BASE_URL}/api/admin/auth/logout`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      [csrfHeader]: csrfToken,
    },
  })

  if (!response.ok) {
    throw new Error(await readError(response, 'Sign-out failed. Please try again.'))
  }
  csrfToken = ''
}
