'use client';
import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { ShoppingCart, Eye, EyeOff, Phone } from 'lucide-react';
import { useCartStore } from '@/store/cartStore';

export default function LoginPage() {
  const router = useRouter();
  const [tab, setTab] = useState<'phone' | 'email'>('phone');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [loading, setLoading] = useState(false);

  const sendOtp = () => {
    if (!phone || phone.length < 10) { toast.error('Enter a valid 10-digit number'); return; }
    setLoading(true);
    setTimeout(() => { setOtpSent(true); setLoading(false); toast.success('OTP sent to +91 ' + phone); }, 1000);
  };

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      toast.success('Welcome back! 🎉');
      router.push('/');
    }, 1200);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 to-orange-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 mb-3">
            <div className="w-10 h-10 bg-primary-500 rounded-xl flex items-center justify-center">
              <ShoppingCart className="w-6 h-6 text-white" />
            </div>
            <span className="text-2xl font-bold text-gray-900">Near<span className="text-primary-500">Kart</span></span>
          </div>
          <p className="text-gray-500 text-sm">Your neighbourhood, delivered fast</p>
        </div>

        <div className="card p-8">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Sign In</h2>

          {/* Tabs */}
          <div className="flex bg-gray-100 rounded-xl p-1 mb-6">
            {(['phone','email'] as const).map(t => (
              <button key={t} onClick={() => setTab(t)}
                className={`flex-1 py-2 rounded-lg text-sm font-medium transition-all ${
                  tab === t ? 'bg-white text-primary-600 shadow-sm' : 'text-gray-500'
                }`}>
                {t === 'phone' ? '📱 Phone' : '📧 Email'}
              </button>
            ))}
          </div>

          <form onSubmit={handleLogin} className="space-y-4">
            {tab === 'phone' ? (
              <>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Phone Number</label>
                  <div className="flex gap-2">
                    <span className="flex items-center px-3 bg-gray-100 border border-gray-200 rounded-xl text-sm text-gray-600">+91</span>
                    <input value={phone} onChange={e => setPhone(e.target.value)}
                      className="input-field" placeholder="9876543210" maxLength={10} />
                  </div>
                </div>
                {!otpSent ? (
                  <button type="button" onClick={sendOtp} disabled={loading}
                    className="btn-primary w-full flex items-center justify-center gap-2">
                    {loading ? <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : <Phone className="w-4 h-4" />}
                    Send OTP
                  </button>
                ) : (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Enter OTP</label>
                      <input value={otp} onChange={e => setOtp(e.target.value)}
                        className="input-field tracking-widest text-center text-lg" placeholder="• • • • • •" maxLength={6} />
                    </div>
                    <button type="submit" disabled={loading} className="btn-primary w-full">
                      {loading ? 'Verifying...' : 'Verify & Login'}
                    </button>
                  </>
                )}
              </>
            ) : (
              <>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input type="email" value={email} onChange={e => setEmail(e.target.value)}
                    className="input-field" placeholder="you@example.com" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
                  <div className="relative">
                    <input type={showPw ? 'text' : 'password'} value={password} onChange={e => setPassword(e.target.value)}
                      className="input-field pr-10" placeholder="••••••••" />
                    <button type="button" onClick={() => setShowPw(!showPw)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                      {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>
                <div className="text-right">
                  <Link href="/forgot-password" className="text-primary-500 text-sm hover:underline">Forgot password?</Link>
                </div>
                <button type="submit" disabled={loading} className="btn-primary w-full">
                  {loading ? 'Signing in...' : 'Sign In'}
                </button>
              </>
            )}
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Don&apos;t have an account?{' '}
            <Link href="/register" className="text-primary-500 font-semibold hover:underline">Sign Up</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
