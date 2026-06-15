import { Search } from 'lucide-react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';

export function AdminToolbar({
  search,
  onSearch,
  onClear,
}: {
  search: string;
  onSearch: (value: string) => void;
  onClear: () => void;
}) {
  return (
    <div className="flex flex-col gap-3 rounded-3xl border border-slate-200 bg-white p-4 shadow-soft md:flex-row md:items-center md:justify-between">
      <div className="relative flex-1">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
        <Input
          className="pl-10"
          placeholder="Search by original URL, short code, or id"
          value={search}
          onChange={(event) => onSearch(event.target.value)}
        />
      </div>
      <Button variant="ghost" onClick={onClear}>
        Clear
      </Button>
    </div>
  );
}
