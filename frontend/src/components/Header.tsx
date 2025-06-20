import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Car, User, LogOut, Settings, Shield, Users } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

const Header: React.FC = () => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Use hasRole function for better role checking
  const isAdmin = hasRole(['YONETİCİ', 'Yonetici', 'admin', 'ADMIN']);
  const isManager = hasRole(['MUDUR', 'Mudur', 'manager', 'MANAGER']);
  const isStaff = hasRole(['CALISAN', 'Personel', 'staff', 'STAFF']);
  const isAdminOrStaff = isAdmin || isManager || isStaff;

  console.log('User role:', user?.userRole?.roleName); // Debug log
  console.log('Is Admin:', isAdmin, 'Is Manager:', isManager, 'Is Staff:', isStaff);

  return (
    <header className="header-container sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="logo-container group">
            <span className="logo-text">RENTELLO</span>
          </Link>

          {/* Navigation */}
          <nav className="hidden md:flex items-center space-x-1">
            <Link
              to="/"
              className="nav-tab text-gray-800 hover:text-green-600"
            >
              Ana Sayfa
            </Link>
            <Link
              to="/vehicles"
              className="nav-tab text-gray-800 hover:text-green-600"
            >
              Araçlar
            </Link>
            {user && !isAdminOrStaff && (
              <Link
                to="/rentals"
                className="nav-tab text-gray-800 hover:text-green-600"
              >
                Kiralama
              </Link>
            )}
            {user && isAdmin && (
              <Link
                to="/admin"
                className="nav-tab text-gray-800 hover:text-green-600 flex items-center space-x-2"
              >
                <Shield className="h-4 w-4" />
                <span>Admin Panel</span>
              </Link>
            )}
            {user && (isManager || isStaff) && (
              <Link
                to="/staff"
                className="nav-tab text-gray-800 hover:text-green-600 flex items-center space-x-2"
              >
                <Users className="h-4 w-4" />
                <span>Personel Panel</span>
              </Link>
            )}
          </nav>

          {/* User Menu */}
          <div className="flex items-center space-x-4">
            {user ? (
              <div className="flex items-center space-x-4">
                <div className="hidden sm:flex items-center space-x-3">
                  <div className="stats-icon">
                    <User className="h-4 w-4" />
                  </div>
                  <div className="text-right">
                    <p className="text-gray-800 font-medium text-sm">
                      {user.firstName} {user.lastName}
                    </p>
                    <p className="text-green-600 text-xs">
                      {user.userRole?.roleName}
                    </p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Link
                    to="/profile"
                    className="action-edit"
                    title="Profil"
                  >
                    <Settings className="h-5 w-5" />
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="action-delete"
                    title="Çıkış Yap"
                  >
                    <LogOut className="h-5 w-5" />
                  </button>
                </div>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <Link
                  to="/login"
                  className="btn-secondary text-sm px-4 py-2"
                >
                  Giriş Yap
                </Link>
                <Link
                  to="/register"
                  className="btn-primary text-sm px-4 py-2"
                >
                  Kayıt Ol
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile Navigation */}
      <div className="md:hidden border-t border-green-400/30">
        <div className="px-4 py-3 space-y-2">
          <Link
            to="/"
            className="block nav-tab text-gray-800 hover:text-green-600 text-sm"
          >
            Ana Sayfa
          </Link>
          <Link
            to="/vehicles"
            className="block nav-tab text-gray-800 hover:text-green-600 text-sm"
          >
            Araçlar
          </Link>
          {user && !isAdminOrStaff && (
            <Link
              to="/rentals"
              className="block nav-tab text-gray-800 hover:text-green-600 text-sm"
            >
              Kiralama
            </Link>
          )}
          {user && isAdmin && (
            <Link
              to="/admin"
              className="block nav-tab text-gray-800 hover:text-green-600 text-sm"
            >
              Admin Panel
            </Link>
          )}
          {user && (isManager || isStaff) && (
            <Link
              to="/staff"
              className="block nav-tab text-gray-800 hover:text-green-600 text-sm"
            >
              Personel Panel
            </Link>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header; 