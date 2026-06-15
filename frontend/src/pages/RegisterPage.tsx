import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ArrowRight } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { useAuthState } from '../hooks/useAuth';
import { useAppToast } from '../hooks/useToast';

export function RegisterPage() {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const auth = useAuthState();
  const toast = useAppToast();
  const navigate = useNavigate();

  async function handleSendOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (loading) return;
    setLoading(true);
    try {
      await auth.sendOtp({ email });
      toast.success('Verification code sent to your email.');
      setStep(2);
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined) ?? 'Failed to send OTP.'
        : 'Failed to send OTP.';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (loading) return;
    setLoading(true);
    try {
      await auth.verifyOtp({ email, otp });
      toast.success('Email verified successfully.');
      setStep(3);
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined) ?? 'Invalid or expired OTP.'
        : 'Invalid or expired OTP.';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (loading) return;
    setLoading(true);
    try {
      await auth.registerUser({ fullName, email, password });
      toast.success('Account created successfully.');
      navigate('/app');
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined) ?? 'Registration failed.'
        : 'Registration failed.';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-7xl items-center px-4 py-12 sm:px-6 lg:px-8">
      <div className="grid w-full gap-10 lg:grid-cols-2">
        <div className="flex items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.28em] text-brand-600">Get started</p>
            <h1 className="mt-4 text-4xl font-semibold tracking-tight text-slate-950">Create your workspace account.</h1>
            <p className="mt-4 max-w-xl text-base leading-8 text-slate-500">
              Your account unlocks the dashboard, analytics, and admin workflows.
            </p>
          </div>
        </div>

        <Card>
          {step === 1 && (
            <form className="space-y-4" onSubmit={handleSendOtp}>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Full name</label>
                <Input value={fullName} onChange={(e) => setFullName(e.target.value)} required maxLength={120} />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Email</label>
                <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required maxLength={320} />
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Sending...' : 'Send Verification Code'}
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
              <p className="text-center text-sm text-slate-500">
                Already have an account? <Link to="/login" className="font-medium text-brand-600">Sign in</Link>
              </p>
            </form>
          )}

          {step === 2 && (
            <form className="space-y-4" onSubmit={handleVerifyOtp}>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Verification Code</label>
                <Input type="text" placeholder="123456" value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} />
                <p className="mt-2 text-xs text-slate-500">
                  Enter the 6-digit code sent to {email}.
                </p>
              </div>
              <div className="flex flex-col gap-2">
                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? 'Verifying...' : 'Verify Email'}
                </Button>
                <Button type="button" variant="outline" className="w-full" onClick={() => setStep(1)}>
                  Back
                </Button>
              </div>
            </form>
          )}

          {step === 3 && (
            <form className="space-y-4" onSubmit={handleRegister}>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Set a Password</label>
                <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} maxLength={72} />
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Creating account...' : 'Complete Registration'}
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}
