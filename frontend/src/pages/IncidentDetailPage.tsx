import { Link, useParams } from 'react-router'
import { useEffect, useState } from 'react'
import { getIncidentData, type IncidentData } from '../api/incidents'

export function IncidentDetailPage() {
  const { id = '' } = useParams(); const [data, setData] = useState<IncidentData>(); const [error, setError] = useState('')
  useEffect(() => { getIncidentData(id).then(setData).catch(reason => setError(reason.message)) }, [id])
  if (error) return <main><p>{error}</p></main>
  if (!data) return <main><p>Loading incident...</p></main>
  return <main><Link to="/incidents">Back to incidents</Link><h1>{data.incident.title}</h1><p>{data.incident.description}</p><h2>Timeline</h2><section className="incident-list">{data.events.map(event => <article className="incident-card" key={event.id}><strong>{event.type.replaceAll('_', ' ')}</strong><p>{event.message}</p><small>{new Date(event.createdAt).toLocaleString()}</small></article>)}</section><h2>Alerts</h2>{data.alerts.map(alert => <p key={alert.id}><strong>{alert.severity} · {alert.status}: </strong>{alert.message}</p>)}<h2>Tasks</h2>{data.tasks.map(task => <p key={task.id}><strong>{task.status}: </strong>{task.title}</p>)}<h2>Comments</h2>{data.comments.map(comment => <p key={comment.id}><strong>{comment.authorName}: </strong>{comment.content}</p>)}</main>
}
