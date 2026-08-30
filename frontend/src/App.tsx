import { useEffect } from 'react'
import { Navigate, Route, Routes, useNavigate } from 'react-router'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { IncidentsPage } from './pages/IncidentsPage'
import { LoginPage } from './pages/LoginPage'
import { IncidentDetailPage } from './pages/IncidentDetailPage'

function SessionExpiryRedirect() {
  const navigate = useNavigate()

  useEffect(() => {
    const redirectToLogin = () => {
      navigate('/login', { replace: true, state: { sessionExpired: true } })
    }

    window.addEventListener('pulse:session-expired', redirectToLogin)
    return () => window.removeEventListener('pulse:session-expired', redirectToLogin)
  }, [navigate])

  return null
}

function App() {
  return <><SessionExpiryRedirect /><Routes><Route path="/login" element={<LoginPage />} /><Route path="/incidents" element={<ProtectedRoute><IncidentsPage /></ProtectedRoute>} /><Route path="/incidents/:id" element={<ProtectedRoute><IncidentDetailPage /></ProtectedRoute>} /><Route path="*" element={<Navigate to="/login" replace />} /></Routes></>
}

export default App
