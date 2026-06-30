import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './redux/store';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

function PrivateRoute({ children }) {
  const token = localStorage.getItem('nearkart_token');
  return token ? children : <Navigate to="/login" />;
}

export default function App() {
  return (
    <Provider store={store}>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={
            <PrivateRoute>
              <div style={{ padding: 32 }}>
                <h2>🛒 Welcome to NearKart</h2>
                <p>Shop listing page coming soon...</p>
              </div>
            </PrivateRoute>
          } />
        </Routes>
      </Router>
    </Provider>
  );
}
