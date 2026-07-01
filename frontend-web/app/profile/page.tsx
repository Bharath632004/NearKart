'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { useAuthStore } from '@/store/authStore';
import { ShoppingBag, Heart, LogOut, User } from 'lucide-react';
import Link from 'next/link';
import toast from 'react-hot-toast';

export default function ProfilePage() {
  const { user, isLoggedIn, logout, updateProfile } = useAuthStore();
  const router = useRouter();
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ name: user?.name || '', phone: user?.phone || '', address: user?.address || '' });

  function handleSave(e: React.FormEvent) {
    e.preventDefault();
    updateProfile(form);
    setEditing(false);
    toast.success('Profile updated!');
  }

  function handleLogout() {
    logout();
    toast.success('Logged out!');
    router.push('/');
  }

  if (!isLoggedIn) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <Navbar />
        <main className="flex-1 flex flex-col items-center justify-center gap-4">
          <User size={56} className="text-gray-300" />
          <h2 className="text-xl font-semibold text-gray-700">Please log in to view your profile</h2>
          <div className="flex gap-3">
            <Link href="/login"><Button>Login</Button></Link>
            <Link href="/register"><Button variant="outline">Register</Button></Link>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-3xl mx-auto w-full px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-8">My Profile</h1>
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
          <div className="flex items-center gap-4 mb-6">
            <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center text-2xl font-bold text-green-700">
              {(user?.name || 'U')[0].toUpperCase()}
            </div>
            <div>
              <h2 className="text-lg font-semibold text-gray-900">{user?.name}</h2>
              <p className="text-sm text-gray-500">{user?.email}</p>
            </div>
          </div>

          {editing ? (
            <form onSubmit={handleSave} className="flex flex-col gap-4">
              <Input label="Name" value={form.name} onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))} />
              <Input label="Phone" value={form.phone} onChange={(e) => setForm(f => ({ ...f, phone: e.target.value }))} />
              <div>
                <label className="text-sm font-medium text-gray-700 block mb-1">Default Address</label>
                <textarea
                  value={form.address}
                  onChange={(e) => setForm(f => ({ ...f, address: e.target.value }))}
                  rows={2}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-300"
                />
              </div>
              <div className="flex gap-3">
                <Button type="submit">Save Changes</Button>
                <Button type="button" variant="outline" onClick={() => setEditing(false)}>Cancel</Button>
              </div>
            </form>
          ) : (
            <div className="flex flex-col gap-2 text-sm text-gray-700">
              <p><span className="font-medium">Phone:</span> {user?.phone || '—'}</p>
              <p><span className="font-medium">Address:</span> {user?.address || '—'}</p>
              <Button variant="outline" className="mt-3 w-fit" onClick={() => setEditing(true)}>Edit Profile</Button>
            </div>
          )}
        </div>

        {/* Quick links */}
        <div className="grid sm:grid-cols-2 gap-4 mb-6">
          <Link href="/orders" className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 flex items-center gap-4 hover:shadow-md transition">
            <ShoppingBag size={28} className="text-green-600" />
            <div>
              <p className="font-semibold text-gray-800">My Orders</p>
              <p className="text-xs text-gray-500">Track and view past orders</p>
            </div>
          </Link>
          <Link href="/wishlist" className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 flex items-center gap-4 hover:shadow-md transition">
            <Heart size={28} className="text-red-500" />
            <div>
              <p className="font-semibold text-gray-800">Wishlist</p>
              <p className="text-xs text-gray-500">Your saved items</p>
            </div>
          </Link>
        </div>

        <Button variant="danger" onClick={handleLogout} className="w-full">
          <LogOut size={16} /> Logout
        </Button>
      </main>
      <Footer />
    </div>
  );
}
