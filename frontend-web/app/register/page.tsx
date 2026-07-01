'use client';
import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { ShoppingCart, Eye, EyeOff, CheckCircle } from 'lucide-react';

export default function RegisterPage() {
  const router = useRouter();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '', confirm: '', pincode: '' });
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);

  const update = (k: string, v: string) => setForm(p => ({ ...p, [k]: v }));

  const handleStep1 = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name.trim() || !form.phone || !form.email) { toast.error('Fill all required fields'); return; }
    if (form.phone.length < 10) { toast.error('Enter a valid 10-digit phone number'); return; }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email)) { toast.error('Enter a valid email address'); return; }
    if (form.pincode && form.pincode.length < 6) { toast.error('Enter a valid 6-digit pincode'); return; }
    setStep(2);
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.password || form.password.length < 8) { toast.error('Password must be at least 8 characters'); return; }
    if (form.password !== form.confirm) { toast.error('Passwords do not match'); return; }
    setLoading(true);
    setTimeout(() => { setLoading(false); setStep(3); }, 1500);
  };

  if (step === 3) return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 to-orange-100 flex items-center justify-center p-4">
      <div className="card p-10 text-center max-w-sm w-full animate-bounce-in">
        <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
        <h2 className="text-xl font-bold mb-2">Account Created! 🎉</h2>
        <p className="text-gray-500 text-sm mb-6">Welcome to NearKart, {form.name}!</p>
        <button onClick={() => router.push('/')} className="btn-primary w-full">Start Shopping</button>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 to-orange-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 mb-3">
            <div className="w-10 h-10 bg-primary-500 rounded-xl flex items-center justify-center">
              <ShoppingCart className="w-6 h-6 text-white" />
            </div>
            <span className="text-2xl font-bold text-gray-900">Near<span className="text-primary-500">Kart</span></span>
          </div>
        </div>

        <div className="card p-8">
          <div className="flex items-center mb-6">
            {[1, 2].map(s => (
              <div key={s} className="flex items-center">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${
                  s <= step ? 'bg-primary-500 text-white' : 'bg-gray-200 text-gray-400'
                }`}>{s}</div>
                {s < 2 && <div className={`w-16 h-1 mx-1 ${ step > s ? 'bg-primary-500' : 'bg-gray-200'}`} />}
              </div>
            ))}
          </div>

          {step === 1 ? (
            <form onSubmit={handleStep1} className="space-y-4">
              <h2 className="text-xl font-bold mb-4">Personal Info</h2>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Full Name <span className="text-red-500">*</span></label>
                <input value={form.name} onChange={e => update('name', e.target.value)} className="input-field" placeholder="Bharath C" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Phone <span className="text-red-500">*</span></label>
                <div className="flex gap-2">
                  <span className="flex items-center px-3 bg-gray-100 border border-gray-200 rounded-xl text-sm text-gray-600">+91</span>
                  <input value={form.phone} onChange={e => update('phone', e.target.value.replace(/\D/g, ''))} className="input-field" placeholder="9876543210" maxLength={10} inputMode="numeric" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email <span className="text-red-500">*</span></label>
                <input type="email" value={form.email} onChange={e => update('email', e.target.value)} className="input-field" placeholder="you@example.com" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Pincode</label>
                <input value={form.pincode} onChange={e => update('pincode', e.target.value.replace(/\D/g, ''))} className="input-field" placeholder="500001" maxLength={6} inputMode="numeric" />
              </div>
              <button type="submit" className="btn-primary w-full">Continue →</button>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-4">
              <h2 className="text-xl font-bold mb-4">Set Password</h2>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Password <span className="text-red-500">*</span></label>
                <div className="relative">
                  <input type={showPw ? 'text' : 'password'} value={form.password} onChange={e => update('password', e.target.value)}
                    className="input-field pr-10" placeholder="Min 8 characters" />
                  <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                    {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Password <span className="text-red-500">*</span></label>
                <input type="password" value={form.confirm} onChange={e => update('confirm', e.target.value)} className="input-field" placeholder="Re-enter password" />
              </div>
              <div className="flex gap-3">
                <button type="button" onClick={() => setStep(1)} className="btn-outline flex-1">← Back</button>
                <button type="submit" disabled={loading} className="btn-primary flex-1">
                  {loading ? 'Creating...' : 'Create Account'}
                </button>
              </div>
            </form>
          )}
          <p className="text-center text-sm text-gray-500 mt-6">
            Already have an account?{' '}
            <Link href="/login" className="text-primary-500 font-semibold hover:underline">Sign In</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
