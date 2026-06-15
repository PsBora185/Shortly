import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../types';
import { login, register, sendOtp, sendForgotPasswordOtp, verifyOtp, resetPassword } from '../services/auth.service';

const TOKEN_KEY = 'shortly-token';
const USER_KEY = 'shortly-user';

interface AuthContextValue {
  token: string | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  sendOtp: typeof sendOtp;
  sendForgotPasswordOtp: typeof sendForgotPasswordOtp;
  verifyOtp: typeof verifyOtp;
  resetPassword: typeof resetPassword;
  loginUser: (request: LoginRequest) => Promise<AuthResponse>;
  registerUser: (request: RegisterRequest) => Promise<AuthResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState<AuthUser | null>(() => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  });

  useEffect(() => {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  }, [user]);

  // Sync across browser tabs
  useEffect(() => {
    const sync = () => {
      setToken(localStorage.getItem(TOKEN_KEY));
      const raw = localStorage.getItem(USER_KEY);
      setUser(raw ? (JSON.parse(raw) as AuthUser) : null);
    };
    window.addEventListener('storage', sync);
    return () => window.removeEventListener('storage', sync);
  }, []);

  function handleAuthResponse(response: AuthResponse) {
    setToken(response.token);
    setUser({
      email: response.email,
      fullName: response.fullName,
      role: response.role,
      provider: response.provider,
    });
  }

  const value = useMemo<AuthContextValue>(() => ({
    token,
    user,
    isAuthenticated: Boolean(token),
    isAdmin: user?.role === 'ADMIN',
    sendOtp,
    sendForgotPasswordOtp,
    verifyOtp,
    resetPassword,
    async loginUser(request) {
      const response = await login(request);
      handleAuthResponse(response);
      return response;
    },
    async registerUser(request) {
      const response = await register(request);
      handleAuthResponse(response);
      return response;
    },
    logout() {
      setToken(null);
      setUser(null);
    },
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
