import { Navigate, Outlet } from 'react-router-dom';
import { useAuthState } from '../hooks/useAuth';

export function ProtectedRoute() {
  const auth = useAuthState();
  return auth.isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}
