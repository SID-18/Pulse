export async function login(email: string, password: string) {
  const response = await fetch('http://localhost:8080/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, password }) })
  if (!response.ok) throw new Error('Invalid email or password.')
  return response.json() as Promise<{ accessToken: string }>
}
