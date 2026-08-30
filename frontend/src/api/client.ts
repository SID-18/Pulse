import { clearAccessToken, getAccessToken } from '../auth/session'

export async function authenticatedFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
) {
  const headers = new Headers(init.headers)
  const accessToken = getAccessToken()

  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(input, { ...init, headers })

  if (response.status === 401) {
    clearAccessToken()
    window.dispatchEvent(new Event('pulse:session-expired'))
  }

  return response
}
