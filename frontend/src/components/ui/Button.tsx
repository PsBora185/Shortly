import { cloneElement, isValidElement, type ButtonHTMLAttributes, type PropsWithChildren, type ReactElement } from 'react';
import { cn } from '../../utils/cn';

type ButtonProps = PropsWithChildren<ButtonHTMLAttributes<HTMLButtonElement>> & {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  asChild?: boolean;
};

export function Button({ className, variant = 'primary', children, asChild, ...props }: ButtonProps) {
  const base =
    'inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-medium transition focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60';

  const variants = {
    primary: 'bg-slate-900 text-white shadow-soft hover:bg-slate-800',
    secondary: 'bg-brand-600 text-white hover:bg-brand-500',
    ghost: 'bg-transparent text-slate-700 hover:bg-slate-100',
    danger: 'bg-red-600 text-white hover:bg-red-500',
  };

  if (asChild && isValidElement(children)) {
    const child = children as ReactElement<{ className?: string }>;
    return cloneElement(child, {
      className: cn(base, variants[variant], className, child.props.className),
    });
  }

  return (
    <button className={cn(base, variants[variant], className)} {...props}>
      {children}
    </button>
  );
}
