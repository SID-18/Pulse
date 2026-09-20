import { authenticatedFetch } from './client'

export type IncidentAiSummary = {
  incidentId: string
  model: string
  summary: string
  generatedAt: string
}

export type SimilarIncident = {
  id: string
  title: string
  severity: string
  status: string
  service_name: string | null
  similarity: number
}

export type IncidentRagRecommendation = {
  incidentId: string
  model: string
  recommendation: string
  similar_incidents: SimilarIncident[]
  generatedAt: string
}

export async function getAiSummary(incidentId: string) {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/incidents/${incidentId}/ai-summary`,
  )
  if (!response.ok) throw new Error('Local AI summary is unavailable.')
  return response.json() as Promise<IncidentAiSummary>
}

export async function getRagRecommendation(incidentId: string) {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/incidents/${incidentId}/ai-recommendation`,
  )
  if (!response.ok) throw new Error('Local RAG recommendation is unavailable.')
  return response.json() as Promise<IncidentRagRecommendation>
}
