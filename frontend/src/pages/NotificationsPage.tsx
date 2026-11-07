import { Link } from 'react-router'
import { useCallback, useEffect, useState } from 'react'
import { getMyNotifications, markNotificationAsRead, type Notification } from '../api/notifications'
import { ApiRequestError } from '../api/client'

export function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [updatingEventId, setUpdatingEventId] = useState('')

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

  async function markAsRead(eventId: string) {
    setUpdatingEventId(eventId)
    try {
      await markNotificationAsRead(eventId)
      setNotifications(current => current.map(notification =>
        notification.eventId === eventId ? { ...notification, read: true } : notification,
      ))
    } finally {
      setUpdatingEventId('')
    }
  }

  async function markAllAsRead() {
    const unread = notifications.filter(notification => !notification.read)
    if (!unread.length) return
    setUpdatingEventId('all')
    try {
      await Promise.all(unread.map(notification => markNotificationAsRead(notification.eventId)))
      setNotifications(current => current.map(notification => ({ ...notification, read: true })))
    } finally {
      setUpdatingEventId('')
    }
  }

  const unreadCount = notifications.filter(notification => !notification.read).length

  return <main className="workspace">
    <Link className="back-link" to="/incidents">← All incidents</Link>
    <header className="page-header">
      <div><p>PULSE / COMMAND CENTER</p><h1>My notifications</h1><span className="unread-count">{unreadCount} unread</span></div>
      <div className="notification-actions">
        <button className="secondary-action" onClick={() => { void markAllAsRead() }} disabled={!unreadCount || updatingEventId === 'all'}>
          {updatingEventId === 'all' ? 'Updating…' : 'Mark all read'}
        </button>
        <button onClick={load} disabled={loading}>{loading ? 'Refreshing…' : 'Refresh'}</button>
      </div>
    </header>
    <p>Updates routed to you directly or through your team.</p>
    {error && <section className="notice-error"><p>{error}</p><button onClick={load}>Try again</button></section>}
    {!loading && !error && notifications.length === 0 && <section className="empty-state">
      <h2>No notifications yet</h2><p>When an incident assigned to you or your team changes, it will appear here.</p>
    </section>}
    <section className="notification-list">
      {notifications.map(notification => <article className={`notification-card ${notification.read ? 'read' : 'unread'}`} key={notification.eventId}>
        <div><strong className="notification-type">{notification.type.replaceAll('_', ' ')}</strong><small>{new Date(notification.occurredAt).toLocaleString()}</small></div>
        <p>{notification.message}</p>
        <footer><Link to={`/incidents/${notification.incidentId}`}>Open incident →</Link>
          {!notification.read && <button onClick={() => { void markAsRead(notification.eventId) }} disabled={updatingEventId === notification.eventId}>
            {updatingEventId === notification.eventId ? 'Updating…' : 'Mark read'}
          </button>}
        </footer>
      </article>)}
    </section>
  </main>
}
