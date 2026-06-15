import { FormEvent, useMemo, useState } from 'react';
import { login } from '../services/auth.service';
import { useAdminDeleteUrl, useAdminUrls } from '../hooks/useUrls';
import { useAppToast } from '../hooks/useToast';
import { AdminToolbar } from '../components/admin/AdminToolbar';
import { Pagination } from '../components/admin/Pagination';
import { UrlsTable } from '../components/tables/UrlsTable';
import { Card } from '../components/ui/Card';
import { EmptyState } from '../components/ui/EmptyState';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

const pageSize = 6;

export function AdminPage() {
  const [adminToken, setAdminToken] = useState<string | null>(null);
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);
  const { data: urls = [], isLoading } = useAdminUrls(adminToken);
  const deleteMutation = useAdminDeleteUrl(adminToken);
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

  async function handleAdminLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAuthError(null);

    try {
      const response = await login({ email: adminEmail, password: adminPassword });
      if (response.role !== 'ADMIN') {
        throw new Error('Admin role required');
      }
      setAdminToken(response.token);
      setIsAuthenticated(true);
      setAdminPassword('');
      toast.success('Admin access granted.');
    } catch {
      setAuthError('Admin authentication failed. Use the configured admin email and password.');
      setIsAuthenticated(false);
      setAdminToken(null);
    }
  }

  return (
    <div className="space-y-6">
      {!isAuthenticated ? (
        <Card className="max-w-lg mx-auto p-6">
          <h1 className="text-2xl font-semibold text-slate-900">Admin access required</h1>
          <p className="mt-2 text-sm text-slate-600">
            Enter the admin email and password configured in your backend secrets. This token is kept in memory only and will be cleared when you leave or refresh this page.
          </p>
          <form className="mt-6 space-y-4" onSubmit={handleAdminLogin}>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Admin email</label>
              <Input type="email" value={adminEmail} onChange={(e) => setAdminEmail(e.target.value)} required />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Admin password</label>
              <Input type="password" value={adminPassword} onChange={(e) => setAdminPassword(e.target.value)} required minLength={8} />
            </div>
            {authError ? <p className="text-sm text-red-600">{authError}</p> : null}
            <Button type="submit" className="w-full">Authenticate Admin</Button>
          </form>
        </Card>
      ) : (
        <>
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-2xl font-semibold text-slate-900">Admin console</h1>
              <p className="mt-1 text-sm text-slate-500">Your admin session is temporary and is not persisted.</p>
            </div>
            <Button variant="ghost" onClick={() => {
              setIsAuthenticated(false);
              setAdminToken(null);
              setAdminEmail('');
              setAdminPassword('');
              setAuthError(null);
            }}>
              Logout admin
            </Button>
          </div>

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
        </>
      )}
    </div>
  );
}
