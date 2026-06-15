import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createUrl, deleteUrl, fetchUrls } from '../services/url.service';
import type { CreateUrlRequest } from '../types';

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
