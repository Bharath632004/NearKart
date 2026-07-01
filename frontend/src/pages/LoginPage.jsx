import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { loginUser, clearError } from '../redux/authSlice';
import ErrorMsg from '../components/ErrorMsg';

export default function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, role } = useSelector((s) => s.auth);
  const [form, setForm] = useState({ email: '', password: '' });

  useEffect(() => {
    if (role) navigate(`/${role.toLowerCase()}`);
  }, [role, navigate]);

  // fix: clear stale Redux error when leaving this page
  useEffect(() => {
    return () => { dispatch(clearError()); };
  }, [dispatch]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    dispatch(clearError());
    dispatch(loginUser(form));
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h2 style={styles.title}>🛒 NearKart Login</h2>
        <ErrorMsg msg={error} />
        <form onSubmit={handleSubmit}>
          <div style={styles.field}>
            <label style={styles.label}>Email</label>
            <input name="email" type="email" value={form.email} onChange={handleChange}
              style={styles.input} placeholder="you@example.com" required />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Password</label>
            <input name="password" type="password" value={form.password} onChange={handleChange}
              style={styles.input} placeholder="••••••••" required />
          </div>
          <button type="submit" style={styles.btn} disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        <p style={styles.foot}>Don't have an account? <Link to="/register" style={styles.footLink}>Register</Link></p>
      </div>
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f0f4f8' },
  card: { background: '#fff', borderRadius: 12, padding: 36, width: 360, boxShadow: '0 4px 20px rgba(0,0,0,0.1)' },
  title: { textAlign: 'center', color: '#1a1a2e', marginBottom: 24 },
  field: { marginBottom: 16 },
  label: { display: 'block', marginBottom: 6, color: '#555', fontSize: 14 },
  input: { width: '100%', padding: '10px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, boxSizing: 'border-box' },
  btn: { width: '100%', padding: '12px', background: '#e94560', color: '#fff', border: 'none', borderRadius: 8, fontSize: 16, cursor: 'pointer', marginTop: 8 },
  foot: { textAlign: 'center', marginTop: 16, fontSize: 14, color: '#666' },
  footLink: { color: '#e94560' },
};
