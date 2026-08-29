import { Navigate, Route, Routes } from 'react-router'
import { IncidentsPage } from './pages/IncidentsPage'
import { LoginPage } from './pages/LoginPage'
import { IncidentDetailPage } from './pages/IncidentDetailPage'

function App() {
  return <Routes><Route path="/login" element={<LoginPage />} /><Route path="/incidents" element={<IncidentsPage />} /><Route path="/incidents/:id" element={<IncidentDetailPage />} /><Route path="*" element={<Navigate to="/login" replace />} /></Routes>
}

export default App
