import { useState, type FormEvent } from 'react';
import { useCreateUrl } from '../../hooks/useUrls';
import { useAppToast } from '../../hooks/useToast';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';
import { Input } from '../ui/Input';

export function UrlShortenForm({ onCreated }: { onCreated?: () => void }) {
  const [originalUrl, setOriginalUrl] = useState('');
  const createMutation = useCreateUrl();
  const toast = useAppToast();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsed = (() => {
      try {
        return new URL(originalUrl);
      } catch {
        return null;
      }
    })();

    if (!parsed || !['http:', 'https:'].includes(parsed.protocol)) {
      toast.error('Please enter a valid http or https URL.');
      return;
    }

    try {
      await createMutation.mutateAsync({ originalUrl });
      toast.success('Short URL created successfully.');
      setOriginalUrl('');
      onCreated?.();
    } catch {
      toast.error('Failed to create short URL. Please try again.');
    }
  }

  return (
    <Card>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Create Short URL</h2>
          <p className="mt-1 text-sm text-slate-500">Paste a long URL and generate a clean short link instantly.</p>
        </div>
        <Input
          type="url"
          placeholder="https://example.com/articles/devops"
          value={originalUrl}
          onChange={(event) => setOriginalUrl(event.target.value)}
          required
        />
        <div className="flex flex-wrap gap-3">
          <Button type="submit" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Shortening...' : 'Shorten URL'}
          </Button>
          <Button type="button" variant="ghost" onClick={() => setOriginalUrl('')}>
            Clear
          </Button>
        </div>
      </form>
    </Card>
  );
}
