import { Client } from '@stomp/stompjs'
import { getAccessToken } from '../auth/session'

export type IncidentRealtimeEvent = {
  eventId: string
  incidentId: string
  type: string
  message: string
  occurredAt: string
}

export function subscribeToIncidentEvents(
  onEvent: (event: IncidentRealtimeEvent) => void,
) {
  const accessToken = getAccessToken()
  if (!accessToken) return () => undefined

  const client = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    connectHeaders: { Authorization: `Bearer ${accessToken}` },
    reconnectDelay: 5000,
    debug: () => undefined,
  })
  client.onConnect = () => {
    client.subscribe('/topic/incidents', message => {
      try {
        onEvent(JSON.parse(message.body) as IncidentRealtimeEvent)
      } catch {
        // Ignore malformed messages without breaking an active subscription.
      }
    })
  }
  client.activate()

  return () => {
    void client.deactivate()
  }
}
