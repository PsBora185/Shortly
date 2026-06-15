import { Edit3, Link2, Trash2, X } from 'lucide-react';
import { useMemo, useState } from 'react';
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
  onUpdate,
}: {
  urls: UrlResponse[];
  loading?: boolean;
  onDelete?: (id: string) => void;
  onUpdate?: (id: string, originalUrl: string) => void;
}) {
  const [editingUrlId, setEditingUrlId] = useState<string | null>(null);
  const [editOriginalUrl, setEditOriginalUrl] = useState('');

  const editingUrl = useMemo(
    () => urls.find((url) => url.id === editingUrlId) ?? null,
    [editingUrlId, urls],
  );

  const handleStartEdit = (url: UrlResponse) => {
    setEditingUrlId(url.id);
    setEditOriginalUrl(url.originalUrl);
  };

  const handleCancelEdit = () => {
    setEditingUrlId(null);
    setEditOriginalUrl('');
  };

  const handleSaveEdit = () => {
    if (editingUrlId && editOriginalUrl.trim()) {
      onUpdate?.(editingUrlId, editOriginalUrl.trim());
      handleCancelEdit();
    }
  };

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
      <div className="hidden lg:block overflow-x-auto">
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
                  <div className="flex flex-wrap gap-2">
                    <Button variant="secondary" onClick={() => handleStartEdit(url)} className="gap-2">
                      <Edit3 className="h-4 w-4" />
                      Edit
                    </Button>
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

      <div className="space-y-4 lg:hidden p-4">
        {urls.map((url) => (
          <div key={url.id} className="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold text-slate-900">{url.originalUrl}</p>
                <a href={url.shortUrl} target="_blank" rel="noreferrer" className="mt-2 block break-all text-sm font-medium text-brand-600 hover:underline">
                  {url.shortUrl}
                </a>
                <p className="mt-2 text-xs text-slate-500">Created: {formatDateTime(url.createdAt)}</p>
                <p className="mt-1 text-xs text-slate-500">Clicks: {url.clicks}</p>
              </div>
              <div className="flex flex-col gap-2">
                <Button variant="secondary" onClick={() => handleStartEdit(url)} className="gap-2">
                  <Edit3 className="h-4 w-4" />
                </Button>
                {onDelete ? (
                  <Button variant="danger" onClick={() => onDelete(url.id)} className="gap-2">
                    <Trash2 className="h-4 w-4" />
                  </Button>
                ) : null}
              </div>
            </div>
          </div>
        ))}
      </div>

      {editingUrl ? (
        <Card className="mx-4 mb-4 rounded-3xl border border-brand-200 bg-brand-50 p-4 shadow-sm lg:mx-0">
          <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h3 className="text-lg font-semibold text-slate-900">Edit URL</h3>
                <p className="mt-1 text-sm text-slate-600">Update the destination for your short link.</p>
              </div>
              <Button variant="ghost" onClick={handleCancelEdit} className="gap-2">
                <X className="h-4 w-4" />
                Cancel
              </Button>
            </div>
            <div className="grid gap-4 sm:grid-cols-[1.2fr_0.8fr]">
              <input
                type="url"
                value={editOriginalUrl}
                onChange={(event) => setEditOriginalUrl(event.target.value)}
                className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                placeholder="https://example.com/updated-destination"
              />
              <div className="flex flex-wrap gap-2">
                <Button variant="primary" onClick={handleSaveEdit} className="min-w-[120px]">
                  Save
                </Button>
                <Button variant="ghost" onClick={handleCancelEdit} className="min-w-[120px]">
                  Cancel
                </Button>
              </div>
            </div>
          </div>
        </Card>
      ) : null}
    </Card>
  );
}
