import { useMemo, useState } from 'react';
import { useDeleteUrl, useUrls } from '../hooks/useUrls';
import { useAppToast } from '../hooks/useToast';
import { AdminToolbar } from '../components/admin/AdminToolbar';
import { Pagination } from '../components/admin/Pagination';
import { UrlsTable } from '../components/tables/UrlsTable';
import { Card } from '../components/ui/Card';
import { EmptyState } from '../components/ui/EmptyState';
import { Button } from '../components/ui/Button';

const pageSize = 6;

export function AdminPage() {
  const { data: urls = [], isLoading } = useUrls();
  const deleteMutation = useDeleteUrl();
  const toast = useAppToast();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [filter, setFilter] = useState<'all' | 'popular' | 'recent'>('all');

  const filtered = useMemo(() => {
    let items = [...urls];
    const query = search.trim().toLowerCase();

    if (query) {
      items = items.filter((url) =>
        [url.originalUrl, url.shortUrl, url.shortCode, url.id].some((field) => field.toLowerCase().includes(query)),
      );
    }

    if (filter === 'popular') {
      items = items.filter((url) => url.clicks >= 50);
    }

    if (filter === 'recent') {
      items = items.sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt)).slice(0, 12);
    }

    return items;
  }, [urls, search, filter]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const paged = filtered.slice((page - 1) * pageSize, page * pageSize);

  async function handleDelete(id: string) {
    const confirmed = window.confirm('Delete this URL?');
    if (!confirmed) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(id);
      toast.success('URL deleted.');
    } catch {
      toast.error('Failed to delete the URL.');
    }
  }

  return (
    <div className="space-y-6">
      <AdminToolbar search={search} onSearch={(value) => { setSearch(value); setPage(1); }} onClear={() => { setSearch(''); setPage(1); }} />

      <div className="flex flex-wrap gap-2">
        {(['all', 'popular', 'recent'] as const).map((item) => (
          <Button key={item} variant={filter === item ? 'primary' : 'ghost'} onClick={() => { setFilter(item); setPage(1); }}>
            {item[0].toUpperCase() + item.slice(1)}
          </Button>
        ))}
      </div>

      {paged.length === 0 ? (
        <EmptyState
          title="No matching URLs"
          description="Try a different search term or reset the filters to see all links."
        />
      ) : (
        <UrlsTable urls={paged} loading={isLoading} onDelete={handleDelete} />
      )}

      <Pagination
        page={page}
        totalPages={totalPages}
        onPrev={() => setPage((current) => Math.max(1, current - 1))}
        onNext={() => setPage((current) => Math.min(totalPages, current + 1))}
      />
    </div>
  );
}
