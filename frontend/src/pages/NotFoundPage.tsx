import { ArrowLeft, Home } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../components/ui/Button';

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <div className="max-w-lg text-center">
        <p className="text-sm font-semibold uppercase tracking-[0.3em] text-brand-600">404</p>
        <h1 className="mt-4 text-4xl font-semibold tracking-tight text-slate-950">Page not found</h1>
        <p className="mt-4 text-base leading-8 text-slate-500">
          The page you were looking for does not exist. Head back to the dashboard or return to the landing page.
        </p>
        <div className="mt-8 flex justify-center gap-3">
          <Button asChild>
            <Link to="/app">
              <Home className="mr-2 h-4 w-4" />
              Dashboard
            </Link>
          </Button>
          <Button variant="ghost" asChild>
            <Link to="/">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Home
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
