import { Link } from 'react-router'
import { useCallback, useEffect, useState } from 'react'
import { getMyNotifications, type Notification } from '../api/notifications'
import { ApiRequestError } from '../api/client'

export function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = useCallback(() => {
    setLoading(true)
    getMyNotifications()
      .then(items => {
        setNotifications(items)
        setError('')
      })
      .catch(reason => {
        if (reason instanceof ApiRequestError && reason.status === 401) return
        setError('We could not load notifications right now.')
      })
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    getMyNotifications()
      .then(items => {
        setNotifications(items)
        setError('')
      })
      .catch(reason => {
        if (reason instanceof ApiRequestError && reason.status === 401) return
        setError('We could not load notifications right now.')
      })
      .finally(() => setLoading(false))
  }, [])

  return <main className="workspace">
    <Link className="back-link" to="/incidents">← All incidents</Link>
    <header className="page-header">
      <div><p>PULSE / COMMAND CENTER</p><h1>My notifications</h1></div>
      <button onClick={load} disabled={loading}>{loading ? 'Refreshing…' : 'Refresh'}</button>
    </header>
    <p>Updates routed to you directly or through your team.</p>
    {error && <section className="notice-error"><p>{error}</p><button onClick={load}>Try again</button></section>}
    {!loading && !error && notifications.length === 0 && <section className="empty-state">
      <h2>No notifications yet</h2><p>When an incident assigned to you or your team changes, it will appear here.</p>
    </section>}
    <section className="notification-list">
      {notifications.map(notification => <article className="notification-card" key={notification.eventId}>
        <div><strong className="notification-type">{notification.type.replaceAll('_', ' ')}</strong><small>{new Date(notification.occurredAt).toLocaleString()}</small></div>
        <p>{notification.message}</p>
        <Link to={`/incidents/${notification.incidentId}`}>Open incident →</Link>
      </article>)}
    </section>
  </main>
}
