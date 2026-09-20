import { Link, useParams } from 'react-router'
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import {
  getAiSummary,
  getRagRecommendation,
  type IncidentAiSummary,
  type IncidentRagRecommendation,
} from '../api/ai'
import {
  addComment,
  assignIncidentOwner,
  getAssignableUsers,
  getIncidentData,
  updateIncidentStatus,
  updateTaskStatus,
  type IncidentData,
  type AssignableUser,
} from '../api/incidents'
import { ApiRequestError } from '../api/client'
import { getRole } from '../auth/session'
import { subscribeToIncidentEvents } from '../realtime/incidentEvents'

export function IncidentDetailPage() {
  const { id = '' } = useParams()
  const [data, setData] = useState<IncidentData>()
  const [error, setError] = useState('')
  const [comment, setComment] = useState('')
  const [aiSummary, setAiSummary] = useState<IncidentAiSummary>()
  const [ragRecommendation, setRagRecommendation] = useState<IncidentRagRecommendation>()
  const [aiError, setAiError] = useState('')
  const [aiLoading, setAiLoading] = useState(false)
  const [ragLoading, setRagLoading] = useState(false)
  const [assignableUsers, setAssignableUsers] = useState<AssignableUser[]>([])
  const [selectedOwnerId, setSelectedOwnerId] = useState('')
  const [ownerFeedback, setOwnerFeedback] = useState('')
  const [ownerUpdating, setOwnerUpdating] = useState(false)
  const manager = ['MANAGER', 'ADMIN'].includes(getRole() ?? '')

  const load = useCallback(() => {
    getIncidentData(id)
      .then(result => {
        setData(result)
        setError('')
      })
      .catch(reason => {
        if (reason instanceof ApiRequestError && reason.status === 401) return
        setError('We could not load this incident right now.')
      })
  }, [id])

  useEffect(() => { load() }, [load])
  useEffect(() => {
    if (!manager) return
    getAssignableUsers().then(setAssignableUsers).catch(() => setAssignableUsers([]))
  }, [manager])
  useEffect(
    () => subscribeToIncidentEvents(event => {
      if (event.incidentId === id) load()
    }),
    [id, load],
  )

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await addComment(id, comment)
    setComment('')
    load()
  }

  async function changeStatus(action: 'acknowledge' | 'resolve') {
    await updateIncidentStatus(id, action)
    load()
  }

  async function changeTask(taskId: string, action: 'start' | 'complete') {
    await updateTaskStatus(taskId, action)
    load()
  }

  async function changeOwner() {
    if (!selectedOwnerId) return
    setOwnerUpdating(true)
    setOwnerFeedback('')
    try {
      await assignIncidentOwner(id, selectedOwnerId)
      setSelectedOwnerId('')
      setOwnerFeedback('Incident owner updated.')
      load()
    } catch {
      setOwnerFeedback('Unable to update the incident owner. Please try again.')
    } finally {
      setOwnerUpdating(false)
    }
  }

  async function generateAiSummary() {
    setAiLoading(true)
    setAiError('')
    try {
      setAiSummary(await getAiSummary(id))
    } catch (reason) {
      setAiError(reason instanceof Error ? reason.message : 'Local AI summary is unavailable.')
    } finally {
      setAiLoading(false)
    }
  }

  async function generateRagRecommendation() {
    setRagLoading(true)
    setAiError('')
    try {
      setRagRecommendation(await getRagRecommendation(id))
    } catch (reason) {
      setAiError(reason instanceof Error ? reason.message : 'Local RAG recommendation is unavailable.')
    } finally {
      setRagLoading(false)
    }
  }

  if (error) return <main className="workspace">
    <h1>Incident unavailable</h1>
    <p>{error} Check that Pulse is running, then try again.</p>
    <button onClick={load}>Try again</button>
    <p><Link className="back-link" to="/incidents">← Return to incidents</Link></p>
  </main>
  if (!data) return <main className="workspace"><p>Loading incident...</p></main>

  return <main className="workspace">
    <Link className="back-link" to="/incidents">← All incidents</Link>
    <header className="incident-header">
      <div>
        <span className={`severity ${data.incident.severity.toLowerCase()}`}>{data.incident.severity}</span>
        <h1>{data.incident.title}</h1><p>{data.incident.description}</p>
      </div>
      <div>
        <span className={`status ${data.incident.status.toLowerCase()}`}>{data.incident.status}</span>
        {manager && data.incident.status === 'OPEN' && <button onClick={() => changeStatus('acknowledge')}>Acknowledge</button>}
        {manager && data.incident.status === 'ACKNOWLEDGED' && <button onClick={() => changeStatus('resolve')}>Resolve</button>}
      </div>
    </header>
    <div className="detail-grid">
      <section>
        <h2>Activity timeline</h2>
        <div className="timeline">
          {data.events.map(event => <article className="timeline-item" key={event.id}>
            <strong>{event.type.replaceAll('_', ' ')}</strong><p>{event.message}</p>
            <small>{new Date(event.createdAt).toLocaleString()}</small>
          </article>)}
        </div>
        <h2>Updates</h2>
        <form className="composer" onSubmit={submit}>
          <label htmlFor="comment">Add an incident update</label>
          <textarea id="comment" value={comment} onChange={event => setComment(event.target.value)} placeholder="Share what you checked, changed, or observed…" required />
          <button>Post update</button>
        </form>
        <div className="comments">
          {data.comments.map(item => <article key={item.id}>
            <strong>{item.authorName}</strong><p>{item.content}</p>
          </article>)}
        </div>
      </section>
      <aside>
        <section className="summary-card ownership-card">
          <h2>Incident ownership</h2>
          <p><strong>Current owner</strong><br />{data.incident.ownerName ?? 'Unassigned'}</p>
          {manager && <label>
            Assign or reassign owner
            <select
              value={selectedOwnerId || data.incident.ownerId || ''}
              onChange={event => {
                setSelectedOwnerId(event.target.value)
                setOwnerFeedback('')
              }}
            >
              <option value="" disabled>Select a manager or engineer</option>
              {assignableUsers.map(user => <option key={user.id} value={user.id}>
                {user.name} · {user.role}
              </option>)}
            </select>
            <button
              type="button"
              disabled={!selectedOwnerId || selectedOwnerId === data.incident.ownerId || ownerUpdating}
              onClick={() => { void changeOwner() }}
            >
              {ownerUpdating ? 'Assigning…' : 'Assign owner'}
            </button>
            {ownerFeedback && <small className="owner-feedback">{ownerFeedback}</small>}
          </label>}
        </section>
        <section className="summary-card">
          <h2>Local AI assistant</h2>
          <p>Generate a local summary and suggested next steps from this incident’s current data.</p>
          <button disabled={aiLoading} onClick={generateAiSummary}>
            {aiLoading ? 'Generating…' : 'Generate AI summary'}
          </button>
          <button className="secondary-action" disabled={ragLoading} onClick={generateRagRecommendation}>
            {ragLoading ? 'Searching history…' : 'Find similar resolved incidents'}
          </button>
          {aiError && <p>{aiError}</p>}
          {aiSummary && <div className="ai-summary">
            <small>Generated locally by {aiSummary.model}</small>
            <p>{aiSummary.summary}</p>
          </div>}
          {ragRecommendation && <div className="ai-summary rag-recommendation">
            <small>Grounded locally by {ragRecommendation.model}</small>
            <h3>Similar resolved incidents</h3>
            {ragRecommendation.similar_incidents.length ? <ul>
              {ragRecommendation.similar_incidents.map(item => <li key={item.id}>
                <strong>{item.title}</strong> · {item.severity} · {Math.round(item.similarity * 100)}% similar
              </li>)}
            </ul> : <p>No resolved incidents have been indexed yet.</p>}
            <h3>Recommendation</h3>
            <p>{ragRecommendation.recommendation}</p>
          </div>}
        </section>
        <section className="summary-card">
          <h2>Alerts</h2>
          {data.alerts.length ? data.alerts.map(alert => <p key={alert.id}>
            <strong>{alert.severity} · {alert.status}</strong><br />{alert.message}
          </p>) : <p>No alerts</p>}
        </section>
        <section className="summary-card">
          <h2>Tasks</h2>
          {data.tasks.length ? data.tasks.map(task => <p key={task.id}>
            <strong>{task.status}</strong><br />{task.title}<br />
            {task.status === 'TODO' && <button onClick={() => changeTask(task.id, 'start')}>Start task</button>}
            {task.status === 'IN_PROGRESS' && <button onClick={() => changeTask(task.id, 'complete')}>Complete task</button>}
          </p>) : <p>No tasks</p>}
        </section>
      </aside>
    </div>
  </main>
}
