import { authenticatedFetch } from './client'

export type Notification = {
  eventId: string
  incidentId: string
  type: string
  message: string
  occurredAt: string
  receivedAt: string
}

export async function getMyNotifications() {
  const response = await authenticatedFetch('http://localhost:8080/api/notifications/me')
  if (!response.ok) throw new Error('Unable to load notifications.')
  return response.json() as Promise<Notification[]>
}
