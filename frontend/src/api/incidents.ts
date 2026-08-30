import { authenticatedFetch } from './client'
export type Incident = { id:string; title:string; description:string; severity:string; status:string; createdAt:string }
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
  if (!response.ok) throw new Error('Unable to load incidents.')
  return response.json() as Promise<IncidentPage>
}
export async function getIncidentData(id: string) {
  const requests = ['incidents/' + id, 'incidents/' + id + '/comments', 'alerts?incidentId=' + id, 'incidents/' + id + '/tasks', 'incidents/' + id + '/events']
  const responses = await Promise.all(requests.map(path => authenticatedFetch('http://localhost:8080/api/' + path)))
  if (responses.some(response => !response.ok)) throw new Error('Unable to load incident details.')
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
export async function updateTaskStatus(id: string, action: 'start' | 'complete') {
  const response = await authenticatedFetch(`http://localhost:8080/api/tasks/${id}/${action}`, { method: 'PATCH' })
  if (!response.ok) throw new Error('Unable to update task status.')
}
