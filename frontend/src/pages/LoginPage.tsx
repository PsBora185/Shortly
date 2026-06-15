import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ArrowRight } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { useAuthState } from '../hooks/useAuth';
import { useAppToast } from '../hooks/useToast';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const auth = useAuthState();
  const toast = useAppToast();
  const navigate = useNavigate();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await auth.loginUser({ email, password });
      toast.success('Welcome back.');
      navigate('/app');
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined) ?? 'Login failed. Check your credentials.'
        : 'Login failed. Check your credentials.';
      toast.error(message);
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-7xl items-center px-4 py-12 sm:px-6 lg:px-8">
      <div className="grid w-full gap-10 lg:grid-cols-2">
        <div className="flex items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.28em] text-brand-600">Welcome back</p>
            <h1 className="mt-4 text-4xl font-semibold tracking-tight text-slate-950">Log in to manage your short links.</h1>
            <p className="mt-4 max-w-xl text-base leading-8 text-slate-500">
              Use your email and password to access your dashboard, analytics, and admin tools.
            </p>
          </div>
        </div>

        <Card>
          <form className="space-y-4" onSubmit={submit}>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Email</label>
              <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Password</label>
              <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} maxLength={72} />
            </div>
            <Button type="submit" className="w-full">
              Login
              <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
            <p className="text-center text-sm text-slate-500 flex flex-col gap-2">
              <Link to="/forgot-password" className="font-medium text-brand-600">Forgot your password?</Link>
              <span>No account? <Link to="/register" className="font-medium text-brand-600">Create one</Link></span>
            </p>
          </form>
        </Card>
      </div>
    </div>
  );
}
