import { api } from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, SendOtpRequest, VerifyOtpRequest, MessageResponse } from '../types';

export async function sendOtp(request: SendOtpRequest) {
  const { data } = await api.post<MessageResponse>('/api/auth/send-otp', request);
  return data;
}

export async function sendForgotPasswordOtp(request: SendOtpRequest) {
  const { data } = await api.post<MessageResponse>('/api/auth/forgot-password-otp', request);
  return data;
}

export async function verifyOtp(request: VerifyOtpRequest) {
  const { data } = await api.post<MessageResponse>('/api/auth/verify-otp', request);
  return data;
}

export async function register(request: RegisterRequest) {
  const { data } = await api.post<AuthResponse>('/api/auth/register', request);
  return data;
}

export async function resetPassword(request: RegisterRequest) {
  const { data } = await api.post<MessageResponse>('/api/auth/reset-password', request);
  return data;
}

export async function login(request: LoginRequest) {
  const { data } = await api.post<AuthResponse>('/api/auth/login', request);
  return data;
}
