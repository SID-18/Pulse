import { getAccessToken } from '../auth/session'
export type Incident = { id:string; title:string; description:string; severity:string; status:string; createdAt:string }
export type IncidentComment = { id:string; authorName:string; content:string }
export type IncidentAlert = { id:string; severity:string; status:string; message:string }
export type IncidentTask = { id:string; status:string; title:string }
export type IncidentEvent = { id:string; type:string; message:string; createdAt:string }
export type IncidentData = { incident: Incident; comments: IncidentComment[]; alerts: IncidentAlert[]; tasks: IncidentTask[]; events: IncidentEvent[] }
export async function getIncidents() {
  const response = await fetch('http://localhost:8080/api/incidents', { headers: { Authorization: `Bearer ${getAccessToken()}` } })
  if (!response.ok) throw new Error('Unable to load incidents.')
  return response.json() as Promise<{ content: Incident[] }>
}
export async function getIncidentData(id: string) {
  const headers = { Authorization: `Bearer ${getAccessToken()}` }
  const requests = ['incidents/' + id, 'incidents/' + id + '/comments', 'alerts?incidentId=' + id, 'incidents/' + id + '/tasks', 'incidents/' + id + '/events']
  const responses = await Promise.all(requests.map(path => fetch('http://localhost:8080/api/' + path, { headers })))
  if (responses.some(response => !response.ok)) throw new Error('Unable to load incident details.')
  const [incident, comments, alerts, tasks, events] = await Promise.all(responses.map(response => response.json()))
  return { incident, comments, alerts, tasks, events } as IncidentData
}

export async function addComment(incidentId: string, content: string) {
  const response = await fetch(`http://localhost:8080/api/incidents/${incidentId}/comments`, { method: 'POST', headers: { Authorization: `Bearer ${getAccessToken()}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ content }) })
  if (!response.ok) throw new Error('Unable to add comment.')
}
export async function updateIncidentStatus(id: string, action: 'acknowledge' | 'resolve') {
  const response = await fetch(`http://localhost:8080/api/incidents/${id}/${action}`, { method: 'PATCH', headers: { Authorization: `Bearer ${getAccessToken()}` } })
  if (!response.ok) throw new Error('Unable to update incident status.')
}
export async function updateTaskStatus(id: string, action: 'start' | 'complete') {
  const response = await fetch(`http://localhost:8080/api/tasks/${id}/${action}`, { method: 'PATCH', headers: { Authorization: `Bearer ${getAccessToken()}` } })
  if (!response.ok) throw new Error('Unable to update task status.')
}
