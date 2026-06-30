import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import Loader from '../../components/Loader';
import { getAllUsersApi } from '../../api/adminApi';
import { getAdminReportsApi } from '../../api/adminApi';

export default function AdminDashboard() {
  const [report, setReport] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAdminReportsApi().then(r => setReport(r.data || {}))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const stats = [
    { label: 'Total Users', value: report.totalUsers || 0, icon: '👥', link: '/admin/users' },
    { label: 'Merchants', value