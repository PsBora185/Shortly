export type AuthProvider = 'LOCAL' | 'GOOGLE';
export type UserRole = 'USER' | 'ADMIN';

export interface AuthResponse {
  token: string;
  tokenType: 'Bearer';
  expiresInSeconds: number;
  expiresAt: string;
  email: string;
  fullName: string;
  role: UserRole;
  provider: AuthProvider;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SendOtpRequest {
  email: string;
}

export interface VerifyOtpRequest {
  email: string;
  otp: string;
}

export interface MessageResponse {
  message: string;
}

export interface CreateUrlRequest {
  originalUrl: string;
}

export interface UrlResponse {
  id: string;
  originalUrl: string;
  shortCode: string;
  shortUrl: string;
  createdAt: string;
  expiresAt?: string | null;
  clicks: number;
  lastAccessed?: string | null;
}

export interface AnalyticsResponse {
  shortCode: string;
  clicks: number;
  createdAt: string;
  lastAccessed?: string | null;
  expiresAt?: string | null;
}

export interface AuthUser {
  email: string;
  fullName: string;
  role: UserRole;
  provider: AuthProvider;
}

export interface DashboardStats {
  totalUrls: number;
  totalClicks: number;
  averageClicks: number;
}
