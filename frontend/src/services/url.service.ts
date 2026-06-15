import { api } from './api';
import type { AnalyticsResponse, CreateUrlRequest, UrlResponse } from '../types';

export async function createUrl(request: CreateUrlRequest) {
  const { data } = await api.post<UrlResponse>('/api/urls', request);
  return data;
}

export async function fetchUrls() {
  const { data } = await api.get<UrlResponse[]>('/api/urls');
  return data;
}

export async function fetchAnalytics(shortCode: string) {
  const { data } = await api.get<AnalyticsResponse>(`/api/analytics/${shortCode}`);
  return data;
}

export async function deleteUrl(id: string) {
  await api.delete(`/api/urls/${id}`);
}
