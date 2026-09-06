import { authenticatedFetch } from './client'

export type ServiceHealth = {
  serviceId: string
  serviceName: string
  status: 'UNKNOWN' | 'HEALTHY' | 'DEGRADED' | 'DOWN'
  latencyMs: number | null
  errorRatePercent: number | null
  checkedAt: string | null
  activeIncidentId: string | null
}

export async function getServiceHealth() {
  const response = await authenticatedFetch('http://localhost:8080/api/monitoring/services')
  if (!response.ok) throw new Error('Unable to load service health.')
  return response.json() as Promise<ServiceHealth[]>
}

export async function runHealthCheck(
  serviceId: string,
  latencyMs: number,
  errorRatePercent: number,
) {
  const response = await authenticatedFetch(
    `http://localhost:8080/api/monitoring/services/${serviceId}/checks`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ latencyMs, errorRatePercent }),
    },
  )
  if (!response.ok) throw new Error('Unable to record health check.')
  return response.json() as Promise<ServiceHealth>
}
