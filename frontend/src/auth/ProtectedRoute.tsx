import { Navigate } from 'react-router'
import type { ReactNode } from 'react'
import { getAccessToken } from './session'

type ProtectedRouteProps = {
  children: ReactNode
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  if (!getAccessToken()) {
    return <Navigate to="/login" replace />
  }

  return children
}
