import { useMemo, useState } from 'react';
import { Card } from '../components/ui/Card';
import { StatCard } from '../components/analytics/StatCard';
import { UrlShortenForm } from '../components/forms/UrlShortenForm';
import { UrlsTable } from '../components/tables/UrlsTable';
import { useUrls } from '../hooks/useUrls';
import { formatRelative } from '../utils/format';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { useAppToast } from '../hooks/useToast';

export function DashboardPage() {
  const { data: urls = [], isLoading } = useUrls();
  const toast = useAppToast();
  const [selectedUrl, setSelectedUrl] = useState<string | null>(null);

  const sortedRecent = useMemo(() => [...urls].slice(0, 5), [urls]);

  return (
    <div className="space-y-6">
      <section className="grid gap-4 md:grid-cols-3">
        <StatCard label="Total URLs" value={urls.length} />
        <StatCard label="Total Clicks" value={urls.reduce((sum, url) => sum + url.clicks, 0)} />
        <StatCard label="Average Clicks" value={urls.length ? (urls.reduce((sum, url) => sum + url.clicks, 0) / urls.length).toFixed(1) : '0.0'} />
      </section>

      <div className="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
        <UrlShortenForm onCreated={() => toast.success('A new URL is ready in the table below.')} />

        <Card>
          <h2 className="text-xl font-semibold text-slate-900">Generated URL</h2>
          <p className="mt-1 text-sm text-slate-500">The newest short URL is shown here after creation.</p>
          <div className="mt-6 rounded-3xl border border-dashed border-slate-200 bg-slate-50 p-6">
            {selectedUrl ? (
              <div>
                <p className="text-sm text-slate-500">Selected short URL</p>
                <p className="mt-2 break-all text-lg font-semibold text-slate-950">{selectedUrl}</p>
              </div>
            ) : (
              <p className="text-sm text-slate-500">Create a URL to preview it here.</p>
            )}
          </div>
          <div className="mt-4 flex items-center gap-3">
            <Button variant="secondary" onClick={() => setSelectedUrl(urls[0]?.shortUrl ?? null)} disabled={!urls.length}>
              Preview latest URL
            </Button>
          </div>
        </Card>
      </div>

      <Card>
        <div className="mb-5 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-slate-900">Recent URLs</h2>
            <p className="mt-1 text-sm text-slate-500">Your latest links and current click counts.</p>
          </div>
          <Badge>{sortedRecent.length} items</Badge>
        </div>
        <UrlsTable urls={sortedRecent} loading={isLoading} />
      </Card>

      {urls.length > 0 ? (
        <Card>
          <h3 className="text-lg font-semibold text-slate-900">Recent activity</h3>
          <div className="mt-4 space-y-3">
            {urls.slice(0, 4).map((url) => (
              <div key={url.id} className="flex items-center justify-between rounded-2xl border border-slate-200 px-4 py-3">
                <div>
                  <p className="font-medium text-slate-900">{url.shortCode}</p>
                  <p className="text-sm text-slate-500">{formatRelative(url.lastAccessed ?? url.createdAt)}</p>
                </div>
                <Badge>{url.clicks} clicks</Badge>
              </div>
            ))}
          </div>
        </Card>
      ) : null}
    </div>
  );
}
