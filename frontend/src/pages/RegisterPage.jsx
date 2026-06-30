import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { register } from '../redux/authSlice';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Box, TextField, Button, Typography, Alert, MenuItem } from '@mui/material';

export default function RegisterPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'CUSTOMER' });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(register(form));
    if (!result.error) navigate('/');
  };

  return (
    <Container maxWidth="xs">
      <Box sx={{ mt: 6, display: 'flex', flexDirection: 'column', gap: 2 }}>
        <Typography variant="h4" textAlign="center">🛒 NearKart</Typography>
        <Typography variant="h6" textAlign="center">Create Account</Typography>
        {error && <Alert severity="error">{error}</Alert>}
        <form onSubmit={handleSubmit}>
          <TextField fullWidth label="Full Name" name="name" value={form.name} onChange={handleChange} margin="normal" required />
          <TextField fullWidth label="Email" name="email" type="email" value={form.email} onChange={handleChange} margin="normal" required />
          <TextField fullWidth label="Phone" name="phone" value={form.phone} onChange={handleChange} margin="normal" />
          <TextField fullWidth label="Password" name="password" type="password" value={form.password} onChange={handleChange} margin="normal" required />
          <TextField fullWidth select label="Role" name="role" value={form.role} onChange={handleChange} margin="normal">
            <MenuItem value="CUSTOMER">Customer</MenuItem>
            <MenuItem value="MERCHANT">Merchant</MenuItem>
            <MenuItem value="DELIVERY_AGENT">Delivery Agent</MenuItem>
          </TextField>
          <Button fullWidth variant="contained" type="submit" sx={{ mt: 2 }} disabled={loading}>
            {loading ? 'Registering...' : 'Register'}
          </Button>
        </form>
        <Typography textAlign="center">
          Already have an account? <Link to="/login">Login</Link>
        </Typography>
      </Box>
    </Container>
  );
}
