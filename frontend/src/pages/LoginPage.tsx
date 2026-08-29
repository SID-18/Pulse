import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router'
import { login } from '../api/auth'
import { saveAccessToken } from '../auth/session'

export function LoginPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); try { const result = await login(email, password); saveAccessToken(result.accessToken); navigate('/incidents') } catch { setError('Invalid email or password.') } }
  return <main><form onSubmit={submit}><p>PULSE</p><h1>Incident command center</h1><label>Email<input type="email" value={email} onChange={event => setEmail(event.target.value)} required /></label><label>Password<input type="password" value={password} onChange={event => setPassword(event.target.value)} required /></label>{error && <p>{error}</p>}<button>Sign in</button></form></main>
}
