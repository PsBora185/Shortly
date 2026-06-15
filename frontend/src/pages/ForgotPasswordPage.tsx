import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ArrowRight } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { useAuthState } from '../hooks/useAuth';
import { useAppToast } from '../hooks/useToast';

export function ForgotPasswordPage() {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const auth = useAuthState();
  const toast = useAppToast();
  const navigate = useNavigate();

  async function handleSendOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await auth.sendForgotPasswordOtp({ email });
      toast.success('Reset code sent to your email.');
      setStep(2);
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined) ?? 'Failed to send OTP.'
        : 'Failed to send OTP.';
      toast.error(message);
    }
  }

  async function handleVerifyOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await auth.verifyOtp({ email, otp });
      toast.success('Code verified. Set your new password.');
      setStep(3);
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined) ?? 'Invalid or expired OTP.'
        : 'Invalid or expired OTP.';
      toast.error(message);
    }
  }

  async function handleResetPassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      // Reusing register request DTO shape for reset (email, password) - fullName is ignored by backend
      await auth.resetPassword({ fullName: '', email, password: newPassword });
      toast.success('Password reset successfully. Please log in.');
      navigate('/login');
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data?.message as string | undefined) ?? 'Password reset failed.'
        : 'Password reset failed.';
      toast.error(message);
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-7xl items-center px-4 py-12 sm:px-6 lg:px-8">
      <div className="grid w-full gap-10 lg:grid-cols-2">
        <div className="flex items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.28em] text-brand-600">Recovery</p>
            <h1 className="mt-4 text-4xl font-semibold tracking-tight text-slate-950">Reset your password.</h1>
            <p className="mt-4 max-w-xl text-base leading-8 text-slate-500">
              We'll send a secure code to your email to help you regain access.
            </p>
          </div>
        </div>

        <Card>
          {step === 1 && (
            <form className="space-y-4" onSubmit={handleSendOtp}>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Email Address</label>
                <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </div>
              <Button type="submit" className="w-full">
                Send Reset Code
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
              <p className="text-center text-sm text-slate-500">
                Remembered it? <Link to="/login" className="font-medium text-brand-600">Sign in</Link>
              </p>
            </form>
          )}

          {step === 2 && (
            <form className="space-y-4" onSubmit={handleVerifyOtp}>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Verification Code</label>
                <Input type="text" placeholder="123456" value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} />
              </div>
              <Button type="submit" className="w-full">
                Verify Code
              </Button>
              <Button type="button" variant="outline" className="w-full" onClick={() => setStep(1)}>
                Back
              </Button>
            </form>
          )}

          {step === 3 && (
            <form className="space-y-4" onSubmit={handleResetPassword}>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">New Password</label>
                <Input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required minLength={8} maxLength={72} />
              </div>
              <Button type="submit" className="w-full">
                Reset Password
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}
