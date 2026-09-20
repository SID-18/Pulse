import { ApiRequestError, authenticatedFetch } from './client'
export type Incident = { id:string; title:string; description:string; severity:string; status:string; serviceId?:string | null; ownerId?:string | null; ownerName?:string | null; createdAt:string }
export type AssignableUser = { id:string; name:string; role:'MANAGER' | 'ENGINEER'; email:string; teamId:string; teamName:string }
export type IncidentComment = { id:string; authorName:string; content:string }
export type IncidentAlert = { id:string; severity:string; status:string; message:string }
export type IncidentTask = { id:string; status:string; title:string }
export type IncidentEvent = { id:string; type:string; message:string; createdAt:string }
export type IncidentData = { incident: Incident; comments: IncidentComment[]; alerts: IncidentAlert[]; tasks: IncidentTask[]; events: IncidentEvent[] }
export type IncidentPage = { content: Incident[]; page:number; totalPages:number; totalElements:number; first:boolean; last:boolean }
export async function getIncidents(page = 0, status = '', sortBy = 'CREATED_AT', direction = 'DESC') {
  const params = new URLSearchParams({ page: String(page), size: '6', sortBy, direction })
  if (status) params.set('status', status)
  const response = await authenticatedFetch(`http://localhost:8080/api/incidents?${params}`)
  if (!response.ok) throw new ApiRequestError('Unable to load incidents.', response.status)
  return response.json() as Promise<IncidentPage>
}
export async function getIncidentData(id: string) {
  const requests = ['incidents/' + id, 'incidents/' + id + '/comments', 'alerts?incidentId=' + id, 'incidents/' + id + '/tasks', 'incidents/' + id + '/events']
  const responses = await Promise.all(requests.map(path => authenticatedFetch('http://localhost:8080/api/' + path)))
  const failedResponse = responses.find(response => !response.ok)
  if (failedResponse) throw new ApiRequestError('Unable to load incident details.', failedResponse.status)
  const [incident, comments, alerts, tasks, events] = await Promise.all(responses.map(response => response.json()))
  return { incident, comments, alerts, tasks, events } as IncidentData
}

export async function addComment(incidentId: string, content: string) {
  const response = await authenticatedFetch(`http://localhost:8080/api/incidents/${incidentId}/comments`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ content }) })
  if (!response.ok) throw new Error('Unable to add comment.')
}
export async function updateIncidentStatus(id: string, action: 'acknowledge' | 'resolve') {
  const response = await authenticatedFetch(`http://localhost:8080/api/incidents/${id}/${action}`, { method: 'PATCH' })
  if (!response.ok) throw new Error('Unable to update incident status.')
}
export async function getAssignableUsers() {
  const response = await authenticatedFetch('http://localhost:8080/api/users/assignable')
  if (!response.ok) throw new Error('Unable to load people who can own incidents.')
  return response.json() as Promise<AssignableUser[]>
}
export async function assignIncidentOwner(incidentId: string, userId: string) {
  const response = await authenticatedFetch(`http://localhost:8080/api/incidents/${incidentId}/owner/${userId}`, { method: 'PATCH' })
  if (!response.ok) throw new Error('Unable to assign the incident owner.')
}
export async function updateTaskStatus(id: string, action: 'start' | 'complete') {
  const response = await authenticatedFetch(`http://localhost:8080/api/tasks/${id}/${action}`, { method: 'PATCH' })
  if (!response.ok) throw new Error('Unable to update task status.')
}
