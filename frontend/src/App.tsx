import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VehiclesPage from './pages/VehiclesPage';
import RentalsPage from './pages/RentalsPage';
import ProfilePage from './pages/ProfilePage';
import AdminPanel from './pages/AdminPanel';
import StaffPanel from './pages/StaffPanel';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="register" element={<RegisterPage />} />
              <Route path="vehicles" element={<VehiclesPage />} />
              
              {/* Protected Routes */}
              <Route path="rentals" element={
                <ProtectedRoute>
                  <RentalsPage />
                </ProtectedRoute>
              } />
              <Route path="profile" element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              } />
              <Route path="admin" element={
                <ProtectedRoute roles={['Yonetici', 'Mudur']}>
                  <AdminPanel />
                </ProtectedRoute>
              } />
              <Route path="staff" element={
                <ProtectedRoute roles={['Yonetici', 'Mudur', 'Personel']}>
                  <StaffPanel />
                </ProtectedRoute>
              } />
            </Route>
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App; 