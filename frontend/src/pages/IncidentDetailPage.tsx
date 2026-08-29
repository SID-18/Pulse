import { Link, useParams } from 'react-router'
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { addComment, getIncidentData, updateIncidentStatus, updateTaskStatus, type IncidentData } from '../api/incidents'
import { getRole } from '../auth/session'

export function IncidentDetailPage() {
  const { id = '' } = useParams(); const [data, setData] = useState<IncidentData>(); const [error, setError] = useState(''); const [comment, setComment] = useState('')
  const load = useCallback(() => getIncidentData(id).then(setData).catch(reason => setError(reason.message)), [id])
  useEffect(() => { load() }, [load])
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); await addComment(id, comment); setComment(''); load() }
  async function changeStatus(action: 'acknowledge' | 'resolve') { await updateIncidentStatus(id, action); load() }
  async function changeTask(id: string, action: 'start' | 'complete') { await updateTaskStatus(id, action); load() }
  if (error) return <main className="workspace"><p>{error}</p></main>
  if (!data) return <main className="workspace"><p>Loading incident...</p></main>
  const manager = ['MANAGER', 'ADMIN'].includes(getRole() ?? '')
  return <main className="workspace"><Link className="back-link" to="/incidents">← All incidents</Link><header className="incident-header"><div><span className={`severity ${data.incident.severity.toLowerCase()}`}>{data.incident.severity}</span><h1>{data.incident.title}</h1><p>{data.incident.description}</p></div><div><span className={`status ${data.incident.status.toLowerCase()}`}>{data.incident.status}</span>{manager && data.incident.status === 'OPEN' && <button onClick={() => changeStatus('acknowledge')}>Acknowledge</button>}{manager && data.incident.status === 'ACKNOWLEDGED' && <button onClick={() => changeStatus('resolve')}>Resolve</button>}</div></header><div className="detail-grid"><section><h2>Activity timeline</h2><div className="timeline">{data.events.map(event => <article className="timeline-item" key={event.id}><strong>{event.type.replaceAll('_', ' ')}</strong><p>{event.message}</p><small>{new Date(event.createdAt).toLocaleString()}</small></article>)}</div><h2>Updates</h2><form className="composer" onSubmit={submit}><label htmlFor="comment">Add an incident update</label><textarea id="comment" value={comment} onChange={event => setComment(event.target.value)} placeholder="Share what you checked, changed, or observed…" required /><button>Post update</button></form><div className="comments">{data.comments.map(item => <article key={item.id}><strong>{item.authorName}</strong><p>{item.content}</p></article>)}</div></section><aside><section className="summary-card"><h2>Alerts</h2>{data.alerts.length ? data.alerts.map(alert => <p key={alert.id}><strong>{alert.severity} · {alert.status}</strong><br />{alert.message}</p>) : <p>No alerts</p>}</section><section className="summary-card"><h2>Tasks</h2>{data.tasks.length ? data.tasks.map(task => <p key={task.id}><strong>{task.status}</strong><br />{task.title}<br />{task.status === 'TODO' && <button onClick={() => changeTask(task.id, 'start')}>Start task</button>}{task.status === 'IN_PROGRESS' && <button onClick={() => changeTask(task.id, 'complete')}>Complete task</button>}</p>) : <p>No tasks</p>}</section></aside></div></main>
}
