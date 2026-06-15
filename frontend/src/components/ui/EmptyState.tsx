import { SearchX } from 'lucide-react';
import { Card } from './Card';

export function EmptyState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <Card className="flex flex-col items-center justify-center py-16 text-center">
      <SearchX className="mb-4 h-10 w-10 text-slate-400" />
      <h3 className="text-lg font-semibold text-slate-900">{title}</h3>
      <p className="mt-2 max-w-md text-sm text-slate-500">{description}</p>
    </Card>
  );
}
