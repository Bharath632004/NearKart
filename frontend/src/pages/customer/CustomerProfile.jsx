import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getProfileApi, updateProfileApi } from '../../api/authApi';

export default function CustomerProfile() {
  const [profile, setProfile] = useState({ name: '', email: '', phone: '', address: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    getProfileApi().then(r => setProfile(r.data))
      .catch(() => setError('Failed to load profile'))
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (e) => setProfile({ ...profile, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault(); setSaving(true); setError(''); setSuccess(false);
    try { await updateProfileApi(profile); setSuccess(true); setTimeout(() => setSuccess(false), 2000); }
    catch { setError('Failed to update profile'); }
    finally { setSaving(false); }
  };

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <div style={styles.card}>
          <div style={styles.avatar}>👤</div>
          <h2 style={styles.heading}>My Profile</h2>
          {loading ? <Loader /> : (
            <>
              <ErrorMsg msg={error} />
              {success && <div style={styles.success}>✅ Profile updated!</div>}
              <form onSubmit={handleSubmit}>
                {[['name','Full Name','text'],['email','Email','email'],['phone','Phone','tel'],['address','Address','text']].map(([n,l,t]) => (
                  <div style={styles.field} key={n}>
                    <label style={styles.label}>{l}</label>
                    <input name={n} type={t} value={profile[n] || ''} onChange={handleChange} style={styles.input} />
                  </div>
                ))}
                <button type="submit" style={styles.btn} disabled={saving}>{saving ? 'Saving...' : 'Update Profile'}</button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '40px 32px', display: 'flex', justifyContent: 'center', background: '#f0f4f8', minHeight: '90vh' },
  card: { background: '#fff', borderRadius: 12, padding: 32, width: 420, boxShadow: '0 4px 16px rgba(0,0,0,0.1)', height: 'fit-content' },
  avatar: { fontSize: 56, textAlign: 'center', marginBottom: 8 },
  heading: { textAlign: 'center', color: '#1a1a2e', marginBottom: 20 },
  field: { marginBottom: 14 },
  label: { display: 'block', fontSize: 13, color: '#555', marginBottom: 5 },
  input: { width: '100%', padding: '9px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box' },
  btn: { width: '100%', padding: '12px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 600, marginTop: 8 },
  success: { background: '#eaffea', color: '#1a8c1a', border: '1px solid #a3e4a3', borderRadius: 6, padding: '8px 14px', marginBottom: 12 },
};
