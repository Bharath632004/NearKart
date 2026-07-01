import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { registerUser, clearError } from '../redux/authSlice';
import ErrorMsg from '../components/ErrorMsg';

export default function RegisterPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((s) => s.auth);
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'CUSTOMER', phone: '', address: '' });
  const [success, setSuccess] = useState(false);

  // fix: clear stale Redux error when leaving this page
  useEffect(() => {
    return () => { dispatch(clearError()); };
  }, [dispatch]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    dispatch(clearError());
    const res = await dispatch(registerUser(form));
    if (registerUser.fulfilled.match(res)) {
      setSuccess(true);
      setTimeout(() => navigate('/login'), 1500);
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h2 style={styles.title}>🛒 Create Account</h2>
        {success && <div style={styles.success}>✅ Registered successfully! Redirecting...</div>}
        <ErrorMsg msg={error} />
        <form onSubmit={handleSubmit}>
          {[['name','Name','text','Full Name'],['email','Email','email','you@example.com'],
            ['phone','Phone','tel','9XXXXXXXXX'],['address','Address','text','Your address'],
            ['password','Password','password','••••••••']].map(([name, label, type, ph]) => (
            <div style={styles.field} key={name}>
              <label style={styles.label}>{label}</label>
              <input name={name} type={type} value={form[name]} onChange={handleChange}
                style={styles.input} placeholder={ph} required />
            </div>
          ))}
          <div style={styles.field}>
            <label style={styles.label}>Role</label>
            <select name="role" value={form.role} onChange={handleChange} style={styles.input}>
              <option value="CUSTOMER">Customer</option>
              <option value="MERCHANT">Merchant</option>
              <option value="DELIVERY">Delivery Partner</option>
            </select>
          </div>
          <button type="submit" style={styles.btn} disabled={loading}>
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>
        <p style={styles.foot}>Already have an account? <Link to="/login" style={styles.footLink}>Login</Link></p>
      </div>
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f0f4f8' },
  card: { background: '#fff', borderRadius: 12, padding: 36, width: 400, boxShadow: '0 4px 20px rgba(0,0,0,0.1)' },
  title: { textAlign: 'center', color: '#1a1a2e', marginBottom: 20 },
  field: { marginBottom: 14 },
  label: { display: 'block', marginBottom: 5, color: '#555', fontSize: 14 },
  input: { width: '100%', padding: '9px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box' },
  btn: { width: '100%', padding: '12px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, fontSize: 16, cursor: 'pointer', marginTop: 8 },
  success: { background: '#eaffea', color: '#1a8c1a', border: '1px solid #a3e4a3', borderRadius: 6, padding: '10px 16px', marginBottom: 12 },
  foot: { textAlign: 'center', marginTop: 16, fontSize: 14, color: '#666' },
  footLink: { color: '#e94560' },
};
