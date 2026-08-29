import { Link } from 'react-router'
import { useEffect, useState } from 'react'
import { getIncidents, type Incident } from '../api/incidents'
import { clearAccessToken } from '../auth/session'

export function IncidentsPage() {
  const [incidents, setIncidents] = useState<Incident[]>([]); const [query, setQuery] = useState(''); const [status, setStatus] = useState(''); const [error, setError] = useState('')
  useEffect(() => { getIncidents().then(result => setIncidents(result.content)).catch(reason => setError(reason.message)) }, [])
  const visible = incidents.filter(item => (!status || item.status === status) && `${item.title} ${item.description}`.toLowerCase().includes(query.toLowerCase()))
  const open = incidents.filter(item => item.status === 'OPEN').length; const resolved = incidents.filter(item => item.status === 'RESOLVED').length
  return <main><p>PULSE / COMMAND CENTER</p><h1>Incidents</h1><Link to="/login" onClick={clearAccessToken}>Sign out</Link><section className="metric-row"><article><span>Total incidents</span><strong>{incidents.length}</strong></article><article><span>Open</span><strong>{open}</strong></article><article><span>Resolved</span><strong>{resolved}</strong></article></section><section className="filters"><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Search incidents" /><select value={status} onChange={event => setStatus(event.target.value)}><option value="">All statuses</option><option>OPEN</option><option>ACKNOWLEDGED</option><option>RESOLVED</option></select></section>{error && <p>{error}</p>}<section className="incident-list">{visible.map(incident => <Link className="incident-card" key={incident.id} to={`/incidents/${incident.id}`}><div><strong className={`severity ${incident.severity.toLowerCase()}`}>{incident.severity}</strong><span className={`status ${incident.status.toLowerCase()}`}>{incident.status}</span></div><h2>{incident.title}</h2><p>{incident.description}</p><small>Created {new Date(incident.createdAt).toLocaleString()}</small></Link>)}</section></main>
}
