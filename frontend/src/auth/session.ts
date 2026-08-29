const key = 'pulse.accessToken'
export const getAccessToken = () => sessionStorage.getItem(key)
export const saveAccessToken = (token: string) => sessionStorage.setItem(key, token)
export const clearAccessToken = () => sessionStorage.removeItem(key)
export function getRole() {
  const token = getAccessToken()
  if (!token) return null
  try { return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).role as string } catch { return null }
}
