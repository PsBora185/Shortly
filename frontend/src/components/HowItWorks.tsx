import { ArrowRight, Link2, LogIn, Share2 } from 'lucide-react';
import { Card } from './ui/Card';

const steps = [
  { icon: LogIn, title: 'Authenticate', copy: 'Register or log in with email and password.' },
  { icon: Link2, title: 'Create links', copy: 'Paste a long URL and get a branded short link.' },
  { icon: Share2, title: 'Measure and manage', copy: 'Review analytics, search links, and clean up old ones.' },
];

export function HowItWorks() {
  return (
    <section className="mx-auto max-w-7xl px-4 pb-20 sm:px-6 lg:px-8">
      <div className="mb-10 max-w-2xl">
        <p className="text-sm font-semibold uppercase tracking-[0.28em] text-brand-600">How it works</p>
        <h2 className="mt-3 text-3xl font-semibold tracking-tight text-slate-950 sm:text-4xl">
          A straightforward workflow from link to insight.
        </h2>
      </div>

      <div className="grid gap-5 lg:grid-cols-3">
        {steps.map((step, index) => {
          const Icon = step.icon;
          return (
            <Card key={step.title} className="relative overflow-hidden">
              <div className="absolute right-4 top-4 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-500">
                0{index + 1}
              </div>
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-950 text-white">
                <Icon className="h-5 w-5" />
              </div>
              <h3 className="mt-5 text-lg font-semibold text-slate-900">{step.title}</h3>
              <p className="mt-2 text-sm leading-7 text-slate-500">{step.copy}</p>
              <ArrowRight className="mt-5 h-4 w-4 text-brand-600" />
            </Card>
          );
        })}
      </div>
    </section>
  );
}
