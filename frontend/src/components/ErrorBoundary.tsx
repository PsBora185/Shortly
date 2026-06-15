import { Component, type ErrorInfo, type ReactNode } from 'react';
import { AlertTriangle } from 'lucide-react';
import { Button } from './ui/Button';
import { Card } from './ui/Card';

type Props = {
  children: ReactNode;
};

type State = {
  hasError: boolean;
};

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Frontend error boundary caught:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
          <Card className="max-w-lg text-center">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-red-50 text-red-600">
              <AlertTriangle className="h-6 w-6" />
            </div>
            <h1 className="mt-4 text-2xl font-semibold text-slate-900">Something went wrong</h1>
            <p className="mt-2 text-sm text-slate-500">
              The app hit an unexpected problem. Refresh the page or go back to the dashboard.
            </p>
            <Button className="mt-6" onClick={() => window.location.reload()}>
              Reload
            </Button>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}
