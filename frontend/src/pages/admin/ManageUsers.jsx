import React, { useEffect, useState } from 'react';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import ErrorMsg from '../../components/ErrorMsg';
import { getAllUsersApi, updateUserStatusApi, deleteUserApi } from '../../api/adminApi';

export default function ManageUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  const load = () => getAllUsersApi().then(r => setUsers(r.data || []))
    .catch(() => setError('Failed to load users')).finally(() => setLoading(false));

  useEffect(() => { load(); }, []);

  const handleToggle = async (user) => {
    try { await updateUserStatusApi(user.id, user.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE'); load(); }
    catch { setError('Update failed'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this user?')) return;
    try { await deleteUserApi(id); load(); } catch { setError('Delete failed'); }
  };

  const roles = ['ALL', 'CUSTOMER', 'MERCHANT', 'DELIVERY', 'ADMIN'];
  const filtered = users.filter(u =>
    (roleFilter === 'ALL' || u.role === roleFilter) &&
    (u.name?.toLowerCase().includes(search.toLowerCase()) || u.email?.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div>
      <Navbar />
      <div style={styles.page}>
        <h2 style={styles.heading}>👥 Manage Users</h2>
        <div style={styles.toolbar}>
          <input style={styles.search} placeholder="Search by name or email..."
            value={search} onChange={e => setSearch(e.target.value)} />
          <div style={styles.filters}>
            {roles.map(r => (
              <button key={r} style={{ ...styles.fBtn, background: roleFilter === r ? '#e94560' : '#eee', color: roleFilter === r ? '#fff' : '#333' }}
                onClick={() => setRoleFilter(r)}>{r}</button>
            ))}
          </div>
        </div>
        <ErrorMsg msg={error} />
        {loading ? <Loader /> : filtered.length === 0 ? <p style={{ color: '#888' }}>No users found.</p> : (
          <table style={styles.table}>
            <thead><tr style={styles.th}>{['Name','Email','Role','Phone','Status','Actions'].map(h => <th key={h} style={styles.thCell}>{h}</th>)}</tr></thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u.id} style={styles.tr}>
                  <td style={styles.td}>{u.name}</td>
                  <td style={styles.td}>{u.email}</td>
                  <td style={styles.td}><span style={styles.roleBadge}>{u.role}</span></td>
                  <td style={styles.td}>{u.phone || '-'}</td>
                  <td style={styles.td}><span style={{ ...styles.badge, background: u.status === 'ACTIVE' ? '#22c55e' : '#ef4444' }}>{u.status}</span></td>
                  <td style={styles.td}>
                    <button style={styles.toggleBtn} onClick={() => handleToggle(u)}>
                      {u.status === 'ACTIVE' ? 'Block' : 'Unblock'}
                    </button>
                    <button style={styles.delBtn} onClick={() => handleDelete(u.id)}>🗑</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '24px 32px', maxWidth: 1100, margin: '0 auto' },
  heading: { color: '#1a1a2e', marginBottom: 16 },
  toolbar: { display: 'flex', gap: 16, alignItems: 'center', marginBottom: 16, flexWrap: 'wrap' },
  search: { padding: '9px 14px', border: '1px solid #ddd', borderRadius: 8, fontSize: 14, minWidth: 250 },
  filters: { display: 'flex', gap: 6, flexWrap: 'wrap' },
  fBtn: { padding: '6px 12px', border: 'none', borderRadius: 16, cursor: 'pointer', fontSize: 12 },
  table: { width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 10, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.07)' },
  th: { background: '#1a1a2e', color: '#fff' },
  thCell: { padding: '10px 14px', textAlign: 'left', fontSize: 13 },
  tr: { borderBottom: '1px solid #f0f0f0' },
  td: { padding: '10px 14px', fontSize: 14 },
  badge: { borderRadius: 12, padding: '2px 10px', color: '#fff', fontSize: 12 },
  roleBadge: { background: '#e0e7ff', color: '#4f46e5', borderRadius: 10, padding: '2px 8px', fontSize: 12 },
  toggleBtn: { background: '#f59e0b', color: '#fff', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer', marginRight: 6, fontSize: 12 },
  delBtn: { background: '#ef4444', color: '#fff', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer', fontSize: 12 },
};
