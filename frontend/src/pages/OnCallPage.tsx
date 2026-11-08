import { Link } from 'react-router'
import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { getRole } from '../auth/session'
import {
  assignServiceTeam, getActiveOnCall, getAssignableUsers, getServices, getTeams, scheduleOnCall,
  type OnCallAssignment, type Service, type Team,
} from '../api/oncall'
import type { AssignableUser } from '../api/incidents'

function localDateTime(offsetMinutes: number) {
  const date = new Date(Date.now() + offsetMinutes * 60_000)
  const timezoneOffset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 16)
}

export function OnCallPage() {
  const [services, setServices] = useState<Service[]>([])
  const [teams, setTeams] = useState<Team[]>([])
  const [users, setUsers] = useState<AssignableUser[]>([])
  const [serviceId, setServiceId] = useState('')
  const [teamId, setTeamId] = useState('')
  const [userId, setUserId] = useState('')
  const [startsAt, setStartsAt] = useState(() => localDateTime(-1))
  const [endsAt, setEndsAt] = useState(() => localDateTime(8 * 60))
  const [active, setActive] = useState<OnCallAssignment | null>()
  const [feedback, setFeedback] = useState('')
  const [busy, setBusy] = useState(false)
  const manager = ['MANAGER', 'ADMIN'].includes(getRole() ?? '')

  const load = useCallback(async () => {
    try {
      const [loadedServices, loadedTeams, loadedUsers] = await Promise.all([getServices(), getTeams(), getAssignableUsers()])
      setServices(loadedServices); setTeams(loadedTeams); setUsers(loadedUsers)
      setFeedback('')
    } catch { setFeedback('Unable to load on-call configuration. Please try again.') }
  }, [])

  useEffect(() => {
    const timer = window.setTimeout(() => { void load() }, 0)
    return () => window.clearTimeout(timer)
  }, [load])
  useEffect(() => {
    if (!teamId) { queueMicrotask(() => setActive(undefined)); return }
    getActiveOnCall(teamId).then(setActive).catch(() => setActive(undefined))
  }, [teamId])

  const selectedService = services.find(service => service.id === serviceId)
  const teamUsers = useMemo(() => users.filter(user => user.teamId === teamId), [users, teamId])

  async function saveServiceTeam() {
    if (!serviceId || !teamId) return
    setBusy(true); setFeedback('')
    try {
      const updated = await assignServiceTeam(serviceId, teamId)
      setServices(current => current.map(item => item.id === updated.id ? updated : item))
      setFeedback(`${updated.name} is now owned by ${updated.teamName}.`)
    } catch { setFeedback('Unable to update service ownership.') } finally { setBusy(false) }
  }

  async function submitSchedule(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!teamId || !userId) return
    setBusy(true); setFeedback('')
    try {
      const scheduled = await scheduleOnCall(teamId, userId, new Date(startsAt).toISOString(), new Date(endsAt).toISOString())
      setActive(scheduled); setFeedback(`${scheduled.userName} is scheduled for ${scheduled.teamName}.`)
    } catch { setFeedback('Unable to schedule on-call coverage. Check that the time window does not overlap.') } finally { setBusy(false) }
  }

  if (!manager) return <main className="workspace"><h1>On-call routing</h1><p>Only managers can configure on-call coverage.</p></main>

  return <main className="workspace on-call-page">
    <Link className="back-link" to="/incidents">← All incidents</Link>
    <p>PULSE / ON-CALL</p><h1>On-call routing</h1>
    <p>Link each service to a team, then schedule the active person who should automatically own service incidents.</p>
    {feedback && <p className="on-call-feedback">{feedback}</p>}
    <div className="on-call-grid">
      <section className="summary-card">
        <h2>Service ownership</h2>
        <label>Service<select value={serviceId} onChange={event => { setServiceId(event.target.value); const service = services.find(item => item.id === event.target.value); if (service?.teamId) setTeamId(service.teamId) }}><option value="">Select a service</option>{services.map(service => <option key={service.id} value={service.id}>{service.name}{service.teamName ? ` · ${service.teamName}` : ''}</option>)}</select></label>
        <label>Owning team<select value={teamId} onChange={event => setTeamId(event.target.value)}><option value="">Select a team</option>{teams.map(team => <option key={team.id} value={team.id}>{team.name}</option>)}</select></label>
        <button type="button" disabled={busy || !serviceId || !teamId || selectedService?.teamId === teamId} onClick={() => { void saveServiceTeam() }}>Save service team</button>
      </section>
      <section className="summary-card">
        <h2>Schedule coverage</h2>
        <form className="on-call-form" onSubmit={submitSchedule}>
          <label>On-call person<select required value={userId} onChange={event => setUserId(event.target.value)}><option value="">Select a team member</option>{teamUsers.map(user => <option key={user.id} value={user.id}>{user.name} · {user.role}</option>)}</select></label>
          <label>Starts at<input required type="datetime-local" value={startsAt} onChange={event => setStartsAt(event.target.value)} /></label>
          <label>Ends at<input required type="datetime-local" value={endsAt} onChange={event => setEndsAt(event.target.value)} /></label>
          <button disabled={busy || !teamId || !userId}>{busy ? 'Saving…' : 'Confirm on-call schedule'}</button>
        </form>
      </section>
      <section className="summary-card active-on-call">
        <h2>Active now</h2>
        {!teamId ? <p>Select a team to view coverage.</p> : active === undefined ? <p>Loading active coverage…</p> : active ? <><strong>{active.userName}</strong><p>{active.teamName}<br /><small>Until {new Date(active.endsAt).toLocaleString()}</small></p></> : <p>No active on-call person is scheduled for this team.</p>}
      </section>
    </div>
  </main>
}
