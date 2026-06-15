import axios from 'axios';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createUrl, deleteUrl, fetchUrls, updateUrl } from '../services/url.service';
import type { CreateUrlRequest, UrlResponse } from '../types';

const createAdminApi = (token: string) =>
  axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

export function useUrls() {
  return useQuery({
    queryKey: ['urls'],
    queryFn: fetchUrls,
  });
}

export function useCreateUrl() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateUrlRequest) => createUrl(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['urls'] });
    },
  });
}

export function useDeleteUrl() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => deleteUrl(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['urls'] });
    },
  });
}

export function useUpdateUrl() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, originalUrl }: { id: string; originalUrl: string }) => updateUrl(id, { originalUrl }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['urls'] });
    },
  });
}

export function useAdminUrls(adminToken: string | null) {
  return useQuery<UrlResponse[]>({
    queryKey: ['admin-urls'],
    queryFn: async () => {
      if (!adminToken) {
        throw new Error('Admin access required');
      }
      const { data } = await createAdminApi(adminToken).get<UrlResponse[]>('/api/urls');
      return data;
    },
    enabled: Boolean(adminToken),
  });
}

export function useAdminDeleteUrl(adminToken: string | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      if (!adminToken) {
        throw new Error('Admin access required');
      }
      await createAdminApi(adminToken).delete(`/api/urls/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-urls'] });
    },
  });
}
