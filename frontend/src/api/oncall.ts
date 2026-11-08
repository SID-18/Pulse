import { ApiRequestError, authenticatedFetch } from './client'
import type { AssignableUser } from './incidents'

export type Team = { id: string; name: string }
export type Service = { id: string; name: string; description: string; teamId?: string | null; teamName?: string | null }
export type OnCallAssignment = { id: string; teamId: string; teamName: string; userId: string; userName: string; startsAt: string; endsAt: string }

async function readJson<T>(response: Response, message: string): Promise<T> {
  if (!response.ok) throw new ApiRequestError(message, response.status)
  return response.json() as Promise<T>
}

export function getServices() {
  return authenticatedFetch('http://localhost:8080/api/services').then(response => readJson<Service[]>(response, 'Unable to load services.'))
}

export function getTeams() {
  return authenticatedFetch('http://localhost:8080/api/teams').then(response => readJson<Team[]>(response, 'Unable to load teams.'))
}

export function getAssignableUsers() {
  return authenticatedFetch('http://localhost:8080/api/users/assignable').then(response => readJson<AssignableUser[]>(response, 'Unable to load on-call users.'))
}

export async function assignServiceTeam(serviceId: string, teamId: string) {
  const response = await authenticatedFetch(`http://localhost:8080/api/services/${serviceId}/team/${teamId}`, { method: 'PATCH' })
  return readJson<Service>(response, 'Unable to assign the service team.')
}

export async function scheduleOnCall(teamId: string, userId: string, startsAt: string, endsAt: string) {
  const response = await authenticatedFetch('http://localhost:8080/api/on-call', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ teamId, userId, startsAt, endsAt }),
  })
  return readJson<OnCallAssignment>(response, 'Unable to schedule on-call coverage.')
}

export async function getActiveOnCall(teamId: string) {
  const response = await authenticatedFetch(`http://localhost:8080/api/on-call/active?teamId=${teamId}`)
  if (response.status === 204) return null
  return readJson<OnCallAssignment | null>(response, 'Unable to load active on-call coverage.')
}
