import { cn } from '../../utils/cn';
import type { ReactNode } from 'react';

export function Badge({ className, children }: { className?: string; children: ReactNode }) {
  return <span className={cn('inline-flex items-center rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700', className)}>{children}</span>;
}
