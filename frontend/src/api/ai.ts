import { authenticatedFetch } from './client'

export type IncidentAiSummary = {
  incidentId: string
  model: string
  summary: string
  generatedAt: string
}

export async function getAiSummary(incidentId: string) {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/incidents/${incidentId}/ai-summary`,
  )
  if (!response.ok) throw new Error('Local AI summary is unavailable.')
  return response.json() as Promise<IncidentAiSummary>
}
