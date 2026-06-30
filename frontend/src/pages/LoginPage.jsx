import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { login } from '../redux/authSlice';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Box, TextField, Button, Typography, Alert } from '@mui/material';

export default function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);
  const [form, setForm] = useState({ email: '', password: '' });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(login(form));
    if (!result.error) navigate('/');
  };

  return (
    <Container maxWidth="xs">
      <Box sx={{ mt: 8, display: 'flex', flexDirection: 'column', gap: 2 }}>
        <Typography variant="h4" textAlign="center">🛒 NearKart</Typography>
        <Typography variant="h6" textAlign="center">Login</Typography>
        {error && <Alert severity="error">{error}</Alert>}
        <form onSubmit={handleSubmit}>
          <TextField fullWidth label="Email" name="email" type="email"
            value={form.email} onChange={handleChange} margin="normal" required />
          <TextField fullWidth label="Password" name="password" type="password"
            value={form.password} onChange={handleChange} margin="normal" required />
          <Button fullWidth variant="contained" type="submit" sx={{ mt: 2 }}
            disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </Button>
        </form>
        <Typography textAlign="center">
          New user? <Link to="/register">Register here</Link>
        </Typography>
      </Box>
    </Container>
  );
}
