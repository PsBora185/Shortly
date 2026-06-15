import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createUrl, deleteUrl, fetchUrls, updateUrl } from '../services/url.service';
import { api } from '../services/api';
import type { CreateUrlRequest, UrlResponse } from '../types';

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

export function useAdminUrls(enabled = true) {
  return useQuery<UrlResponse[]>({
    queryKey: ['admin-urls'],
    queryFn: async () => {
      const { data } = await api.get<UrlResponse[]>('/api/urls');
      return data;
    },
    enabled: Boolean(enabled),
    staleTime: 1000 * 60,
  });
}

export function useAdminDeleteUrl() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/urls/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-urls'] });
    },
  });
}
