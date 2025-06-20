import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/api';
import { User } from '../types';
import { 
  User as UserIcon, 
  Mail, 
  Phone, 
  MapPin, 
  Calendar, 
  Shield, 
  Edit3, 
  Save, 
  X, 
  Eye, 
  EyeOff,
  Key,
  Settings,
  CheckCircle
} from 'lucide-react';

const ProfilePage: React.FC = () => {
  const { user, login } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [editData, setEditData] = useState<Partial<User>>({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  
  // Password change state
  const [passwordData, setPasswordData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [showPasswords, setShowPasswords] = useState({
    old: false,
    new: false,
    confirm: false
  });

  useEffect(() => {
    if (user) {
      setEditData({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phoneNumber: user.phoneNumber,
        address: user.address,
        dateOfBirth: user.dateOfBirth
      });
    }
  }, [user]);

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await authService.updateProfile(user.userId, editData);
      // Refresh user data
      const updatedUser = await authService.getProfile();
      // Update auth context by re-logging (this will refresh the user data)
      setSuccess('Profil başarıyla güncellendi!');
      setIsEditing(false);
      
      // Show success message for 3 seconds
      setTimeout(() => setSuccess(''), 3000);
    } catch (error: any) {
      setError(error.response?.data?.message || 'Profil güncellenirken bir hata oluştu');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError('Yeni şifreler eşleşmiyor');
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setError('Yeni şifre en az 6 karakter olmalıdır');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await authService.changePassword(passwordData.oldPassword, passwordData.newPassword);
      setSuccess('Şifre başarıyla değiştirildi!');
      setIsChangingPassword(false);
      setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
      
      // Show success message for 3 seconds
      setTimeout(() => setSuccess(''), 3000);
    } catch (error: any) {
      setError(error.response?.data?.message || 'Şifre değiştirilirken bir hata oluştu');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Belirtilmemiş';
    return new Date(dateString).toLocaleDateString('tr-TR');
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700 flex items-center justify-center">
        <div className="text-white text-xl">Kullanıcı bilgileri yükleniyor...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        
        {/* Header */}
        <div className="text-center mb-8 animate-fade-in-up">
          <div className="stats-icon mx-auto mb-6 animate-pulse-glow">
            <Settings className="h-8 w-8 text-green-300" />
          </div>
          <h1 className="text-4xl font-bold text-white mb-2">Profil Yönetimi</h1>
          <p className="text-green-200">Kişisel bilgilerinizi görüntüleyin ve güncelleyin</p>
        </div>

        {/* Success/Error Messages */}
        {success && (
          <div className="mb-6 p-4 bg-green-800 bg-opacity-80 border border-green-600 text-green-200 rounded-lg flex items-center animate-slide-in-right">
            <CheckCircle className="h-5 w-5 mr-2 text-green-300" />
            {success}
          </div>
        )}

        {error && (
          <div className="mb-6 p-4 bg-red-900 bg-opacity-80 border border-red-600 text-red-200 rounded-lg animate-slide-in-right">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Profile Info Card */}
          <div className="lg:col-span-2">
            <div className="bg-green-900 bg-opacity-80 backdrop-blur-sm rounded-lg shadow-xl border border-green-700 p-6 animate-slide-in-left">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-white flex items-center">
                  <UserIcon className="h-6 w-6 mr-2 text-green-400" />
                  Kişisel Bilgiler
                </h2>
                {!isEditing && !isChangingPassword && (
                  <button
                    onClick={() => setIsEditing(true)}
                    className="btn-secondary flex items-center space-x-2"
                  >
                    <Edit3 className="h-4 w-4" />
                    <span>Düzenle</span>
                  </button>
                )}
              </div>

              {isEditing ? (
                <form onSubmit={handleEditSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="form-group">
                      <label className="form-label">Ad</label>
                      <input
                        type="text"
                        value={editData.firstName || ''}
                        onChange={(e) => setEditData({...editData, firstName: e.target.value})}
                        className="form-input"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Soyad</label>
                      <input
                        type="text"
                        value={editData.lastName || ''}
                        onChange={(e) => setEditData({...editData, lastName: e.target.value})}
                        className="form-input"
                        required
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">E-posta</label>
                    <input
                      type="email"
                      value={editData.email || ''}
                      onChange={(e) => setEditData({...editData, email: e.target.value})}
                      className="form-input"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Telefon</label>
                    <input
                      type="tel"
                      value={editData.phoneNumber || ''}
                      onChange={(e) => setEditData({...editData, phoneNumber: e.target.value})}
                      className="form-input"
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Adres</label>
                    <textarea
                      value={editData.address || ''}
                      onChange={(e) => setEditData({...editData, address: e.target.value})}
                      className="form-input"
                      rows={3}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Doğum Tarihi</label>
                    <input
                      type="date"
                      value={editData.dateOfBirth || ''}
                      onChange={(e) => setEditData({...editData, dateOfBirth: e.target.value})}
                      className="form-input"
                    />
                  </div>

                  <div className="flex space-x-3 pt-4">
                    <button
                      type="submit"
                      disabled={loading}
                      className="btn-primary flex items-center space-x-2"
                    >
                      <Save className="h-4 w-4" />
                      <span>{loading ? 'Kaydediliyor...' : 'Kaydet'}</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setIsEditing(false);
                        setError('');
                        // Reset edit data
                        setEditData({
                          firstName: user.firstName,
                          lastName: user.lastName,
                          email: user.email,
                          phoneNumber: user.phoneNumber,
                          address: user.address,
                          dateOfBirth: user.dateOfBirth
                        });
                      }}
                      className="btn-secondary flex items-center space-x-2"
                    >
                      <X className="h-4 w-4" />
                      <span>İptal</span>
                    </button>
                  </div>
                </form>
              ) : (
                <div className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="flex items-center space-x-3 p-4 bg-green-900 bg-opacity-70 rounded-lg border border-green-700">
                      <UserIcon className="h-5 w-5 text-green-300" />
                      <div>
                        <p className="text-green-200 text-sm font-medium">Ad Soyad</p>
                        <p className="text-white font-semibold">{user.firstName} {user.lastName}</p>
                      </div>
                    </div>
                    <div className="flex items-center space-x-3 p-4 bg-green-900 bg-opacity-70 rounded-lg border border-green-700">
                      <Mail className="h-5 w-5 text-green-300" />
                      <div>
                        <p className="text-green-200 text-sm font-medium">E-posta</p>
                        <p className="text-white font-semibold">{user.email}</p>
                      </div>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="flex items-center space-x-3 p-4 bg-green-900 bg-opacity-70 rounded-lg border border-green-700">
                      <Phone className="h-5 w-5 text-green-300" />
                      <div>
                        <p className="text-green-200 text-sm font-medium">Telefon</p>
                        <p className="text-white font-semibold">{user.phoneNumber || 'Belirtilmemiş'}</p>
                      </div>
                    </div>
                    <div className="flex items-center space-x-3 p-4 bg-green-900 bg-opacity-70 rounded-lg border border-green-700">
                      <Calendar className="h-5 w-5 text-green-300" />
                      <div>
                        <p className="text-green-200 text-sm font-medium">Doğum Tarihi</p>
                        <p className="text-white font-semibold">{formatDate(user.dateOfBirth)}</p>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-start space-x-3 p-4 bg-green-900 bg-opacity-70 rounded-lg border border-green-700">
                    <MapPin className="h-5 w-5 text-green-300 mt-1" />
                    <div className="flex-1">
                      <p className="text-green-200 text-sm font-medium">Adres</p>
                      <p className="text-white font-semibold">{user.address || 'Belirtilmemiş'}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            
            {/* Account Info Card */}
            <div className="bg-green-900 bg-opacity-80 backdrop-blur-sm rounded-lg shadow-xl border border-green-700 p-6 animate-slide-in-right">
              <h3 className="text-xl font-bold text-white mb-4 flex items-center">
                <Shield className="h-5 w-5 mr-2 text-green-300" />
                Hesap Bilgileri
              </h3>
              <div className="space-y-4">
                <div className="p-3 bg-green-900 bg-opacity-60 rounded-lg border border-green-700">
                  <p className="text-green-200 text-sm font-medium">Kullanıcı Adı</p>
                  <p className="text-white font-semibold">@{user.username}</p>
                </div>
                <div className="p-3 bg-green-900 bg-opacity-60 rounded-lg border border-green-700">
                  <p className="text-green-200 text-sm font-medium">Rol</p>
                  <span className="inline-flex px-3 py-1 text-sm font-semibold rounded-full bg-green-700 text-white mt-1 border border-green-500">
                    {user.userRole?.roleName || user.role}
                  </span>
                </div>
                <div className="p-3 bg-green-900 bg-opacity-60 rounded-lg border border-green-700">
                  <p className="text-green-200 text-sm font-medium">Hesap Durumu</p>
                  <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full mt-1 border ${
                    user.isActive ? 'bg-green-700 text-white border-green-500' : 'bg-red-700 text-white border-red-500'
                  }`}>
                    {user.isActive ? 'Aktif' : 'Pasif'}
                  </span>
                </div>
                <div className="p-3 bg-green-900 bg-opacity-60 rounded-lg border border-green-700">
                  <p className="text-green-200 text-sm font-medium">Kayıt Tarihi</p>
                  <p className="text-white font-semibold">{formatDate(user.createdDate)}</p>
                </div>
              </div>
            </div>

            {/* Password Change Card */}
            <div className="bg-green-900 bg-opacity-80 backdrop-blur-sm rounded-lg shadow-xl border border-green-700 p-6 animate-slide-in-right">
              <h3 className="text-xl font-bold text-white mb-4 flex items-center">
                <Key className="h-5 w-5 mr-2 text-green-300" />
                Güvenlik
              </h3>
              
              {!isChangingPassword ? (
                <button
                  onClick={() => setIsChangingPassword(true)}
                  className="btn-secondary w-full flex items-center justify-center space-x-2"
                >
                  <Key className="h-4 w-4" />
                  <span>Şifre Değiştir</span>
                </button>
              ) : (
                <form onSubmit={handlePasswordChange} className="space-y-4">
                  <div className="form-group">
                    <label className="form-label">Mevcut Şifre</label>
                    <div className="relative">
                      <input
                        type={showPasswords.old ? 'text' : 'password'}
                        value={passwordData.oldPassword}
                        onChange={(e) => setPasswordData({...passwordData, oldPassword: e.target.value})}
                        className="form-input pr-10"
                        required
                      />
                      <button
                        type="button"
                        onClick={() => setShowPasswords({...showPasswords, old: !showPasswords.old})}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                      >
                        {showPasswords.old ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      </button>
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Yeni Şifre</label>
                    <div className="relative">
                      <input
                        type={showPasswords.new ? 'text' : 'password'}
                        value={passwordData.newPassword}
                        onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
                        className="form-input pr-10"
                        required
                        minLength={6}
                      />
                      <button
                        type="button"
                        onClick={() => setShowPasswords({...showPasswords, new: !showPasswords.new})}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                      >
                        {showPasswords.new ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      </button>
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Yeni Şifre (Tekrar)</label>
                    <div className="relative">
                      <input
                        type={showPasswords.confirm ? 'text' : 'password'}
                        value={passwordData.confirmPassword}
                        onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
                        className="form-input pr-10"
                        required
                        minLength={6}
                      />
                      <button
                        type="button"
                        onClick={() => setShowPasswords({...showPasswords, confirm: !showPasswords.confirm})}
                        className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                      >
                        {showPasswords.confirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      </button>
                    </div>
                  </div>

                  <div className="flex flex-col space-y-2">
                    <button
                      type="submit"
                      disabled={loading}
                      className="btn-primary w-full flex items-center justify-center space-x-2"
                    >
                      <Save className="h-4 w-4" />
                      <span>{loading ? 'Değiştiriliyor...' : 'Şifre Değiştir'}</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setIsChangingPassword(false);
                        setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
                        setError('');
                      }}
                      className="btn-secondary w-full flex items-center justify-center space-x-2"
                    >
                      <X className="h-4 w-4" />
                      <span>İptal</span>
                    </button>
                  </div>
                </form>
              )}
            </div>

          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage; 