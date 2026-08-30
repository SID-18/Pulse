import { useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { login } from '../api/auth'
import { saveAccessToken } from '../auth/session'

export function LoginPage() {
  const navigate = useNavigate(); const location = useLocation(); const [email, setEmail] = useState(''); const [password, setPassword] = useState(''); const [error, setError] = useState('')
  const sessionExpired = Boolean((location.state as { sessionExpired?: boolean } | null)?.sessionExpired)
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); try { const result = await login(email, password); saveAccessToken(result.accessToken); navigate('/incidents') } catch { setError('Invalid email or password.') } }
  return <main className="login-shell"><section className="login-brand"><p>PULSE</p><h1>Respond with confidence.</h1><span>One calm workspace for incidents, alerts, tasks, and updates.</span></section><form className="login-card" onSubmit={submit}><p className="login-eyebrow">WELCOME BACK</p><h2>Sign in to Pulse</h2>{sessionExpired && <p className="login-error">Your session expired. Please sign in again.</p>}<label>Email<input type="email" value={email} onChange={event => setEmail(event.target.value)} placeholder="you@company.com" required /></label><label>Password<input type="password" value={password} onChange={event => setPassword(event.target.value)} required /></label>{error && <p className="login-error">{error}</p>}<button>Sign in</button></form></main>
}
