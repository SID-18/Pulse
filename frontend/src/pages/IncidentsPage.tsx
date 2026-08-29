import { Link } from 'react-router'
import { useEffect, useState } from 'react'
import { getIncidents, type Incident } from '../api/incidents'
import { clearAccessToken } from '../auth/session'

export function IncidentsPage() {
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    getIncidents().then(result => setIncidents(result.content)).catch(reason => setError(reason.message))
  }, [])

  return <main><p>PULSE / COMMAND CENTER</p><h1>Incidents</h1><Link to="/login" onClick={clearAccessToken}>Sign out</Link>{error && <p>{error}</p>}<section className="incident-list">{incidents.map(incident => <Link className="incident-card" key={incident.id} to={`/incidents/${incident.id}`}><div><strong className={`severity ${incident.severity.toLowerCase()}`}>{incident.severity}</strong><span className={`status ${incident.status.toLowerCase()}`}>{incident.status}</span></div><h2>{incident.title}</h2><p>{incident.description}</p><small>Created {new Date(incident.createdAt).toLocaleString()}</small></Link>)}</section></main>
}
