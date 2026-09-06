import { Link } from 'react-router'
import { useCallback, useEffect, useState } from 'react'
import { getServiceHealth, runHealthCheck, type ServiceHealth } from '../api/monitoring'
import { getRole } from '../auth/session'

export function MonitoringPage() {
  const [services, setServices] = useState<ServiceHealth[]>([])
  const [error, setError] = useState('')
  const [runningServiceId, setRunningServiceId] = useState('')
  const manager = ['MANAGER', 'ADMIN'].includes(getRole() ?? '')

  const load = useCallback(() => {
    getServiceHealth()
      .then(result => {
        setServices(result)
        setError('')
      })
      .catch(reason => setError(reason.message))
  }, [])

  useEffect(() => { load() }, [load])

  async function simulate(
    serviceId: string,
    latencyMs: number,
    errorRatePercent: number,
  ) {
    setRunningServiceId(serviceId)
    try {
      await runHealthCheck(serviceId, latencyMs, errorRatePercent)
      load()
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to run health check.')
    } finally {
      setRunningServiceId('')
    }
  }

  return <main className="workspace">
    <Link className="back-link" to="/incidents">← All incidents</Link>
    <p>PULSE / MONITORING</p><h1>Service health</h1>
    <p>Run controlled checks to demonstrate how latency and error-rate thresholds create operational work.</p>
    {error && <p>{error}</p>}
    <section className="health-grid">
      {services.map(service => <article className="health-card" key={service.serviceId}>
        <div><h2>{service.serviceName}</h2><span className={`health ${service.status.toLowerCase()}`}>{service.status}</span></div>
        {service.checkedAt ? <p>{service.latencyMs} ms latency · {service.errorRatePercent}% errors<br /><small>Checked {new Date(service.checkedAt).toLocaleString()}</small></p> : <p>No check recorded yet.</p>}
        {service.activeIncidentId && <Link to={`/incidents/${service.activeIncidentId}`}>View active incident →</Link>}
        {manager && <div className="simulation-actions">
          <button disabled={runningServiceId === service.serviceId} onClick={() => simulate(service.serviceId, 180, 0.10)}>Healthy check</button>
          <button disabled={runningServiceId === service.serviceId} onClick={() => simulate(service.serviceId, 1100, 2.00)}>Degraded check</button>
          <button className="danger" disabled={runningServiceId === service.serviceId} onClick={() => simulate(service.serviceId, 2500, 7.50)}>Simulate outage</button>
        </div>}
      </article>)}
    </section>
  </main>
}
