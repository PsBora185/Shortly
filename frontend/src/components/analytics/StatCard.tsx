import { Card } from '../ui/Card';

export function StatCard({
  label,
  value,
  delta,
}: {
  label: string;
  value: string | number;
  delta?: string;
}) {
  return (
    <Card>
      <p className="text-sm font-medium text-slate-500">{label}</p>
      <div className="mt-3 flex items-end justify-between gap-4">
        <p className="text-3xl font-semibold text-slate-950">{value}</p>
        {delta ? <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">{delta}</span> : null}
      </div>
    </Card>
  );
}
