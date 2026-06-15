import { ChartNoAxesCombined, ClipboardList, Gauge, Shield, WandSparkles, Workflow } from 'lucide-react';
import { Card } from './ui/Card';

const features = [
  { icon: WandSparkles, title: 'Fast shortening', copy: 'Create short links in one step with instant copy support.' },
  { icon: ClipboardList, title: 'URL inventory', copy: 'Track original URLs, shortened versions, dates, and clicks.' },
  { icon: Gauge, title: 'Analytics', copy: 'Understand click volume and performance patterns quickly.' },
  { icon: Shield, title: 'Secure access', copy: 'Your links and data are kept private and secure.' },
  { icon: ChartNoAxesCombined, title: 'Admin controls', copy: 'Search, filter, paginate, and delete URLs from one place.' },
  { icon: Workflow, title: 'High Availability', copy: 'Built to scale with your link shortening needs.' },
];

export function FeatureGrid() {
  return (
    <section className="mx-auto max-w-7xl px-4 pb-20 sm:px-6 lg:px-8">
      <div className="mb-10 max-w-2xl">
        <p className="text-sm font-semibold uppercase tracking-[0.28em] text-brand-600">Features</p>
        <h2 className="mt-3 text-3xl font-semibold tracking-tight text-slate-950 sm:text-4xl">
          Everything you need to manage your links.
        </h2>
      </div>

      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        {features.map((feature) => {
          const Icon = feature.icon;
          return (
            <Card key={feature.title} className="transition hover:-translate-y-1 hover:shadow-xl">
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-50 text-brand-600">
                <Icon className="h-5 w-5" />
              </div>
              <h3 className="mt-5 text-lg font-semibold text-slate-900">{feature.title}</h3>
              <p className="mt-2 text-sm leading-7 text-slate-500">{feature.copy}</p>
            </Card>
          );
        })}
      </div>
    </section>
  );
}
