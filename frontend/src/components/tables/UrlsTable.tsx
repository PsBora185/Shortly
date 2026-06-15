import { Link2, Trash2 } from 'lucide-react';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
import { Skeleton } from '../ui/Skeleton';
import { UrlCopy } from '../ui/UrlCopy';
import type { UrlResponse } from '../../types';
import { formatDateTime } from '../../utils/format';

export function UrlsTable({
  urls,
  loading,
  onDelete,
}: {
  urls: UrlResponse[];
  loading?: boolean;
  onDelete?: (id: string) => void;
}) {
  if (loading) {
    return (
      <Card>
        <div className="space-y-4">
          <Skeleton className="h-7 w-44" />
          <div className="grid gap-3">
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className="h-16 w-full" />
            ))}
          </div>
        </div>
      </Card>
    );
  }

  if (urls.length === 0) {
    return (
      <Card>
        <div className="flex flex-col items-center justify-center py-10 text-center">
          <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-50 text-brand-600">
            <Link2 className="h-5 w-5" />
          </div>
          <h3 className="text-lg font-semibold text-slate-900">No URLs yet</h3>
          <p className="mt-2 text-sm text-slate-500">Create your first short URL to see it here.</p>
        </div>
      </Card>
    );
  }

  return (
    <Card className="overflow-hidden p-0">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50 text-left text-xs uppercase tracking-[0.22em] text-slate-500">
            <tr>
              <th className="px-6 py-4">Original URL</th>
              <th className="px-6 py-4">Short URL</th>
              <th className="px-6 py-4">Created</th>
              <th className="px-6 py-4">Clicks</th>
              <th className="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 bg-white">
            {urls.map((url) => (
              <tr key={url.id} className="align-top">
                <td className="px-6 py-4">
                  <p className="max-w-sm truncate font-medium text-slate-900">{url.originalUrl}</p>
                  <p className="text-xs text-slate-500">ID: {url.id}</p>
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-2">
                    <a href={url.shortUrl} target="_blank" rel="noreferrer" className="break-all text-sm font-medium text-brand-600 hover:underline">
                      {url.shortUrl}
                    </a>
                    <UrlCopy value={url.shortUrl} />
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-slate-600">{formatDateTime(url.createdAt)}</td>
                <td className="px-6 py-4 text-sm font-semibold text-slate-900">{url.clicks}</td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    {onDelete ? (
                      <Button variant="danger" onClick={() => onDelete(url.id)} className="gap-2">
                        <Trash2 className="h-4 w-4" />
                        Delete
                      </Button>
                    ) : null}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
