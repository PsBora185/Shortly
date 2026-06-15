import { useQuery } from '@tanstack/react-query';
import { fetchAnalytics } from '../services/url.service';

export function useAnalytics(shortCode?: string) {
  return useQuery({
    queryKey: ['analytics', shortCode],
    enabled: Boolean(shortCode),
    queryFn: () => fetchAnalytics(shortCode!),
  });
}
