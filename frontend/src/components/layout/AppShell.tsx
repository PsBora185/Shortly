import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { BarChart3, LayoutDashboard, Link2, Shield, Sparkles } from 'lucide-react';
import { Toaster } from 'react-hot-toast';
import { cn } from '../../utils/cn';
import { Button } from '../ui/Button';
import { useAuthState } from '../../hooks/useAuth';

const navItems = [
  { to: '/app', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/analytics', label: 'Analytics', icon: BarChart3 },
  { to: '/admin', label: 'Admin', icon: Shield },
];

export function AppShell() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const auth = useAuthState();

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(100,112,255,0.08),_transparent_32%),linear-gradient(to_bottom,_#f8fafc,_#ffffff)] text-slate-900">
      <Toaster position="top-right" />
      <header className="sticky top-0 z-30 border-b border-slate-200/70 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
          <NavLink to="/" className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-900 text-white shadow-soft">
              <Link2 className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.24em] text-brand-600">Shortly</p>
              <p className="text-sm text-slate-500">URL Shortener Platform</p>
            </div>
          </NavLink>

          <div className="flex items-center gap-3">
            {auth.user ? (
              <div className="hidden items-center gap-3 rounded-full border border-slate-200 bg-white px-3 py-2 text-sm sm:flex">
                <Sparkles className="h-4 w-4 text-brand-600" />
                <span>{auth.user.fullName}</span>
              </div>
            ) : null}
            <Button
              variant="ghost"
              onClick={() => {
                auth.logout();
                navigate('/login');
              }}
            >
              Logout
            </Button>
          </div>
        </div>
      </header>

      <div className="mx-auto grid max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[240px_1fr] lg:px-8">
        <aside className="lg:sticky lg:top-24 lg:h-fit">
          <nav className="rounded-3xl border border-slate-200 bg-white p-3 shadow-soft">
            {navItems
              .filter((item) => item.to !== '/admin' || auth.isAdmin)
              .map((item) => {
                const Icon = item.icon;
                const active = pathname === item.to;
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    className={cn(
                      'mb-1 flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition last:mb-0',
                      active ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
                    )}
                  >
                    <Icon className="h-4 w-4" />
                    {item.label}
                  </NavLink>
                );
              })}
          </nav>
        </aside>

        <main className="pb-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
