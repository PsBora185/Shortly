import { Button } from '../ui/Button';

export function Pagination({
  page,
  totalPages,
  onPrev,
  onNext,
}: {
  page: number;
  totalPages: number;
  onPrev: () => void;
  onNext: () => void;
}) {
  return (
    <div className="flex items-center justify-between rounded-3xl border border-slate-200 bg-white px-4 py-3 shadow-soft">
      <p className="text-sm text-slate-500">
        Page <span className="font-medium text-slate-900">{page}</span> of <span className="font-medium text-slate-900">{totalPages}</span>
      </p>
      <div className="flex gap-2">
        <Button variant="ghost" onClick={onPrev} disabled={page === 1}>
          Previous
        </Button>
        <Button variant="ghost" onClick={onNext} disabled={page === totalPages}>
          Next
        </Button>
      </div>
    </div>
  );
}
