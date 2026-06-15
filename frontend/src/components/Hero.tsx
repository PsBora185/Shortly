import { ArrowRight, Link2, ShieldCheck, Zap } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from './ui/Button';

export function Hero() {
  return (
    <section className="mx-auto grid max-w-7xl gap-10 px-4 py-16 sm:px-6 lg:grid-cols-2 lg:px-8 lg:py-24">
      <div className="max-w-2xl">
        <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-600 shadow-soft">
          <Zap className="h-4 w-4 text-brand-600" />
          Lightning-fast URL shortener
        </div>
        <h1 className="text-5xl font-semibold tracking-tight text-slate-950 sm:text-6xl">
          Short links.
          <span className="block text-brand-600">Clear analytics.</span>
          <span className="block">Better engagement.</span>
        </h1>
        <p className="mt-6 max-w-xl text-lg leading-8 text-slate-600">
          A powerful platform to create short URLs, track click performance, and manage your links effortlessly.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Button asChild className="rounded-full px-5 py-3 text-base">
            <Link to="/register">
              Shorten Your First URL
              <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
          <Button variant="ghost" asChild className="rounded-full px-5 py-3 text-base">
            <Link to="/login">Sign in</Link>
          </Button>
        </div>
        
      </div>

      <div className="relative">
        <div className="absolute inset-0 -z-10 rounded-[2rem] bg-hero blur-3xl" />
        <div className="rounded-[2rem] border border-slate-200 bg-white p-4 shadow-soft">
          <div className="rounded-[1.6rem] bg-slate-950 p-6 text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-400">Dashboard Preview</p>
                <h3 className="mt-1 text-2xl font-semibold">Create, track, optimize</h3>
              </div>
              <div className="rounded-full bg-brand-600/20 px-3 py-1 text-xs text-brand-200">Live</div>
            </div>
            <div className="mt-6 grid gap-4 sm:grid-cols-3">
              {[
                ['URLs', '1,284'],
                ['Clicks', '48.3k'],
                ['CTR', '72%'],
              ].map(([label, value]) => (
                <div key={label} className="rounded-2xl bg-white/5 p-4">
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-400">{label}</p>
                  <p className="mt-2 text-2xl font-semibold">{value}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
