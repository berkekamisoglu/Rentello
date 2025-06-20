import React, { useState, useEffect } from 'react';
import { 
  Users, 
  Car, 
  DollarSign, 
  BarChart3, 
  Calendar,
  Settings,
  Search,
  Filter,
  ChevronDown,
  Edit,
  Trash2,
  Eye,
  ToggleLeft,
  ToggleRight,
  Plus,
  Trash,
  Key,
  User
} from 'lucide-react';
import { DashboardStats, AdminUser, AdminVehicle, AdminRental, PaginatedResponse, Role } from '../types/admin';
import * as adminService from '../services/adminService';
import VehicleModal from '../components/VehicleModal';
import UserModal from '../components/UserModal';

const AdminPanel: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'users' | 'vehicles' | 'rentals' | 'debug'>('dashboard');
  const [dashboardStats, setDashboardStats] = useState<DashboardStats | null>(null);
  const [users, setUsers] = useState<PaginatedResponse<AdminUser> | null>(null);
  const [vehicles, setVehicles] = useState<PaginatedResponse<AdminVehicle> | null>(null);
  const [rentals, setRentals] = useState<PaginatedResponse<AdminRental> | null>(null);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [roles, setRoles] = useState<Role[]>([]);
  const [editingUserId, setEditingUserId] = useState<number | null>(null);
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [showVehicleModal, setShowVehicleModal] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState<AdminVehicle | null>(null);
  const [vehicleReferences, setVehicleReferences] = useState<any>(null);
  const [showUserModal, setShowUserModal] = useState(false);
  const [editingUser, setEditingUser] = useState<AdminUser | null>(null);
  const [debugLoading, setDebugLoading] = useState(false);

  useEffect(() => {
    if (activeTab === 'dashboard') {
      loadDashboardStats();
      loadRentals(); // Ödeme özeti için rentals verisini yükle
    } else if (activeTab === 'users') {
      loadUsers();
      if (roles.length === 0) {
        loadRoles();
      }
    } else if (activeTab === 'vehicles') {
      loadVehicles();
      if (!vehicleReferences) {
        loadVehicleReferences();
      }
    } else if (activeTab === 'rentals') {
      loadRentals();
    }
  }, [activeTab, currentPage]);

  const loadRoles = async () => {
    try {
      const rolesData = await adminService.getAllRoles();
      setRoles(rolesData);
    } catch (error) {
      console.error('Roller yüklenirken hata:', error);
    }
  };

  const loadDashboardStats = async () => {
    try {
      setLoading(true);
      const stats = await adminService.getDashboardStats();
      console.log('Dashboard stats received:', stats);
      setDashboardStats(stats);
    } catch (error) {
      console.error('Dashboard stats yüklenirken hata:', error);
      // Gerçek veri alınamadığında boş stats göster
      setDashboardStats(null);
    } finally {
      setLoading(false);
    }
  };

  const loadUsers = async () => {
    try {
      setLoading(true);
      const usersData = await adminService.getAllUsers(currentPage, 10);
      setUsers(usersData);
    } catch (error) {
      console.error('Kullanıcılar yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadVehicles = async () => {
    try {
      setLoading(true);
      const vehiclesData = await adminService.getAllVehicles(currentPage, 10);
      setVehicles(vehiclesData);
    } catch (error) {
      console.error('Araçlar yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadRentals = async () => {
    try {
      setLoading(true);
      const rentalsData = await adminService.getAllRentals(currentPage, 10);
      setRentals(rentalsData);
    } catch (error) {
      console.error('Kiralamalar yüklenirken hata:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadVehicleReferences = async () => {
    try {
      console.log('Loading vehicle references...');
      const data = await adminService.getVehicleReferences();
      console.log('Vehicle references loaded:', data);
      setVehicleReferences(data);
    } catch (error) {
      console.error('Error loading vehicle references:', error);
    }
  };

  const handleUserStatusToggle = async (userId: number) => {
    try {
      await adminService.toggleUserStatus(userId);
      loadUsers(); // Refresh the list
    } catch (error) {
      console.error('Kullanıcı durumu güncellenirken hata:', error);
    }
  };

  const handleVehicleStatusToggle = async (vehicleId: number) => {
    try {
      await adminService.toggleVehicleStatus(vehicleId);
      loadVehicles(); // Refresh the list
    } catch (error) {
      console.error('Araç durumu güncellenirken hata:', error);
    }
  };

  const handleRoleUpdate = async (userId: number, roleId: number) => {
    try {
      const result = await adminService.updateUserRole(userId, roleId);
      if (result.success) {
        loadUsers(); // Refresh the list
        setEditingUserId(null);
        alert('Kullanıcı rolü başarıyla güncellendi');
      } else {
        alert('Rol güncelleme hatası: ' + result.message);
      }
    } catch (error) {
      console.error('Rol güncelleme hatası:', error);
      alert('Rol güncellenirken bir hata oluştu');
    }
  };

  const handleResetPassword = async (userId: number) => {
    const newPassword = prompt('Yeni şifreyi girin:');
    if (!newPassword) return;

    try {
      await adminService.resetUserPassword(userId, newPassword);
      alert('Şifre başarıyla sıfırlandı!');
    } catch (error) {
      console.error('Şifre sıfırlama hatası:', error);
      alert('Şifre sıfırlanırken bir hata oluştu.');
    }
  };

  const handleDeleteUser = async (userId: number, userName: string) => {
    const confirmDelete = window.confirm(
      `"${userName}" kullanıcısını silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.`
    );
    
    if (!confirmDelete) return;

    try {
      await adminService.deleteUser(userId);
      alert('Kullanıcı başarıyla silindi!');
      loadUsers(); // Refresh the user list
    } catch (error: any) {
      console.error('Kullanıcı silme hatası:', error);
      const errorMessage = error.response?.data?.error || 'Kullanıcı silinirken bir hata oluştu.';
      alert(errorMessage);
    }
  };

  const handleCreateUser = () => {
    setEditingUser(null);
    setShowUserModal(true);
  };

  const handleSaveUser = async (userData: any) => {
    try {
      await adminService.createUser(userData);
      alert('Kullanıcı başarıyla oluşturuldu!');
      setShowUserModal(false);
      loadUsers(); // Refresh the user list
    } catch (error: any) {
      console.error('Kullanıcı oluşturma hatası:', error);
      const errorMessage = error.response?.data?.error || 'Kullanıcı oluşturulurken bir hata oluştu.';
      alert(errorMessage);
    }
  };

  const searchUsers = async () => {
    if (!searchTerm.trim()) {
      loadUsers();
      return;
    }
    try {
      setLoading(true);
      const usersData = await adminService.searchUsers(searchTerm, 0, 10);
      setUsers(usersData);
      setCurrentPage(0);
    } catch (error) {
      console.error('Kullanıcı arama hatası:', error);
    } finally {
      setLoading(false);
    }
  };

  const searchVehicles = async () => {
    if (!searchTerm.trim()) {
      loadVehicles();
      return;
    }
    try {
      setLoading(true);
      const vehiclesData = await adminService.searchVehicles(searchTerm, 0, 10);
      setVehicles(vehiclesData);
      setCurrentPage(0);
    } catch (error) {
      console.error('Araç arama hatası:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: 'TRY'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('tr-TR');
  };

  const handleVehicleCreate = async (vehicleData: any) => {
    try {
      await adminService.createVehicle(vehicleData);
      setShowVehicleModal(false);
      setEditingVehicle(null);
      loadVehicles();
      alert('Araç başarıyla oluşturuldu!');
    } catch (error) {
      console.error('Error creating vehicle:', error);
      alert('Araç oluşturulurken hata oluştu!');
    }
  };

  const handleVehicleUpdate = async (vehicleId: number, vehicleData: any) => {
    try {
      await adminService.updateVehicle(vehicleId, vehicleData);
      setShowVehicleModal(false);
      setEditingVehicle(null);
      loadVehicles();
      alert('Araç başarıyla güncellendi!');
    } catch (error) {
      console.error('Error updating vehicle:', error);
      alert('Araç güncellenirken hata oluştu!');
    }
  };

  const handleVehicleDelete = async (vehicleId: number, vehicleName: string) => {
    if (window.confirm(`${vehicleName} aracını silmek istediğinizden emin misiniz?`)) {
      try {
        await adminService.deleteVehicle(vehicleId);
        loadVehicles();
        alert('Araç başarıyla silindi!');
      } catch (error) {
        console.error('Error deleting vehicle:', error);
        alert('Araç silinirken hata oluştu!');
      }
    }
  };

  const handleSaveVehicle = async (vehicleData: any) => {
    try {
      if (editingVehicle) {
        await adminService.updateVehicle(editingVehicle.vehicleId, vehicleData);
        alert('Araç başarıyla güncellendi!');
      } else {
        await adminService.createVehicle(vehicleData);
        alert('Araç başarıyla oluşturuldu!');
      }
      setShowVehicleModal(false);
      setEditingVehicle(null);
      loadVehicles(); // Refresh the vehicle list
    } catch (error: any) {
      console.error('Araç kaydetme hatası:', error);
      const errorMessage = error.response?.data?.error || 'Araç kaydedilirken bir hata oluştu.';
      alert(errorMessage);
    }
  };

  const renderDashboard = () => (
    <div className="space-y-8 animate-fade-in-up">
      <div className="flex items-center justify-between">
        <h1 className="text-white text-4xl font-bold">Dashboard</h1>
        <div className="text-white text-sm">
          Son güncelleme: {new Date().toLocaleString('tr-TR')}
        </div>
      </div>

      {/* Stats Cards */}
      <div className="dashboard-grid">
        <div className="stats-card glow-gold">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white text-sm font-medium">Toplam Kullanıcı</p>
              <p className="text-3xl font-bold text-white mt-2">
                {dashboardStats?.totalUsers || 0}
              </p>
            </div>
            <div className="stats-icon">
              <Users className="h-6 w-6" />
            </div>
          </div>
        </div>

        <div className="stats-card glow-gold">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white text-sm font-medium">Toplam Araç</p>
              <p className="text-3xl font-bold text-white mt-2">
                {dashboardStats?.totalVehicles || 0}
              </p>
            </div>
            <div className="stats-icon">
              <Car className="h-6 w-6" />
            </div>
          </div>
        </div>

        <div className="stats-card glow-gold">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white text-sm font-medium">Aktif Kiralama</p>
              <p className="text-3xl font-bold text-white mt-2">
                {dashboardStats?.activeRentals || 0}
              </p>
            </div>
            <div className="stats-icon">
              <Calendar className="h-6 w-6" />
            </div>
          </div>
        </div>

        <div className="stats-card glow-gold">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white text-sm font-medium">Aylık Gelir</p>
              <p className="text-3xl font-bold text-white mt-2">
                ${dashboardStats?.monthlyRevenue?.toLocaleString('en-US', { minimumFractionDigits: 2 }) || '0.00'}
              </p>
            </div>
            <div className="stats-icon">
              <DollarSign className="h-6 w-6" />
            </div>
          </div>
        </div>
      </div>

      {/* Ödeme Özeti Kartları */}
      <div className="mt-8">
        <h2 className="text-white text-2xl font-bold mb-6">Ödeme Özeti</h2>
        <div className="dashboard-grid">
          <div className="stats-card glow-green">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-white text-sm font-medium">Toplam Kiralama Tutarı</p>
                <p className="text-3xl font-bold text-white mt-2">
                  ${rentals?.content?.reduce((total, rental) => total + rental.totalCost, 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) || '0.00'}
                </p>
                <p className="text-white text-xs mt-1 opacity-80">
                  {rentals?.content?.length || 0} kiralama
                </p>
              </div>
              <div className="stats-icon">
                <DollarSign className="h-6 w-6" />
              </div>
            </div>
          </div>

          <div className="stats-card glow-green">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-white text-sm font-medium">Toplam Ödenen</p>
                <p className="text-3xl font-bold text-white mt-2">
                  ${rentals?.content?.reduce((total, rental) => total + rental.totalPaid, 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) || '0.00'}
                </p>
                <p className="text-white text-xs mt-1 opacity-80">
                  Tahsil edilen tutar
                </p>
              </div>
              <div className="stats-icon">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="stats-card glow-orange">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-white text-sm font-medium">Kalan Borç</p>
                <p className="text-3xl font-bold text-white mt-2">
                  ${rentals?.content?.reduce((total, rental) => total + rental.remainingAmount, 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) || '0.00'}
                </p>
                <p className="text-white text-xs mt-1 opacity-80">
                  Tahsil edilecek tutar
                </p>
              </div>
              <div className="stats-icon">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="stats-card glow-blue">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-white text-sm font-medium">Ödeme Oranı</p>
                <p className="text-3xl font-bold text-white mt-2">
                  {(() => {
                    const totalCost = rentals?.content?.reduce((total, rental) => total + rental.totalCost, 0) || 0;
                    const totalPaid = rentals?.content?.reduce((total, rental) => total + rental.totalPaid, 0) || 0;
                    const percentage = totalCost > 0 ? ((totalPaid / totalCost) * 100) : 0;
                    return `${percentage.toFixed(1)}%`;
                  })()}
                </p>
                <p className="text-white text-xs mt-1 opacity-80">
                  Tahsilat oranı
                </p>
              </div>
              <div className="stats-icon">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const renderUsers = () => {
    if (loading) {
      return (
        <div className="flex justify-center items-center py-12">
          <div className="loading-spinner"></div>
        </div>
      );
    }

    if (!users || users.content.length === 0) {
      return (
        <div className="card p-12 text-center">
          <Users className="h-16 w-16 text-white mx-auto mb-4 opacity-50" />
          <h3 className="text-white text-2xl font-semibold mb-2">Kullanıcı Bulunamadı</h3>
          <p className="text-white">Henüz kayıtlı kullanıcı bulunmuyor.</p>
        </div>
      );
    }

    return (
      <div className="space-y-6 animate-fade-in-up">
        <div className="flex items-center justify-between">
          <h2 className="text-white text-3xl font-bold">Kullanıcı Yönetimi</h2>
        </div>

        {/* Search and Add User */}
        <div className="card-header">
          <div className="flex justify-between items-center">
            <div className="search-container">
              <Search className="search-icon" />
              <input
                type="text"
                placeholder="Kullanıcı ara..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && searchUsers()}
                className="search-input"
              />
            </div>
            <div className="flex space-x-3">
              <button
                onClick={searchUsers}
                className="btn-secondary"
              >
                Ara
              </button>
              <button
                onClick={handleCreateUser}
                className="btn-primary flex items-center space-x-2"
              >
                <Plus className="h-4 w-4" />
                <span>Kullanıcı Ekle</span>
              </button>
            </div>
          </div>
        </div>

        {/* Users Table */}
        <div className="table-container">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-700">
              <thead>
                <tr>
                  <th className="px-6 py-4 text-left table-header">Kullanıcı</th>
                  <th className="px-6 py-4 text-left table-header">İletişim</th>
                  <th className="px-6 py-4 text-left table-header">Rol</th>
                  <th className="px-6 py-4 text-left table-header">Durum</th>
                  <th className="px-6 py-4 text-left table-header">Kayıt Tarihi</th>
                  <th className="px-6 py-4 text-left table-header">İşlemler</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700">
                {users.content.map((user) => (
                  <tr key={user.userId} className="table-row">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center space-x-3">
                        <div className="stats-icon w-10 h-10">
                          <User className="h-5 w-5" />
                        </div>
                        <div>
                          <div className="font-medium">
                            {user.firstName} {user.lastName}
                          </div>
                          <div className="text-sm">@{user.username}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm">{user.email}</div>
                        <div className="text-sm">{user.phoneNumber}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {editingUserId === user.userId ? (
                        <select
                          value={selectedRoleId || user.roleId}
                          onChange={(e) => setSelectedRoleId(Number(e.target.value))}
                          className="select-field text-sm"
                        >
                          {roles.map((role) => (
                            <option key={role.roleId} value={role.roleId}>
                              {role.roleName}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <span className="badge-warning">
                          {user.roleName}
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`badge ${user.isActive ? 'badge-success' : 'badge-danger'}`}>
                        {user.isActive ? 'Aktif' : 'Pasif'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {new Date(user.createdDate).toLocaleDateString('tr-TR')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center space-x-2">
                        {editingUserId === user.userId ? (
                          <>
                            <button
                              onClick={() => handleRoleUpdate(user.userId, selectedRoleId || user.roleId)}
                              className="btn-success text-xs px-3 py-1"
                            >
                              Kaydet
                            </button>
                            <button
                              onClick={() => {
                                setEditingUserId(null);
                                setSelectedRoleId(null);
                              }}
                              className="btn-secondary text-xs px-3 py-1"
                            >
                              İptal
                            </button>
                          </>
                        ) : (
                          <>
                            <button
                              onClick={() => {
                                setEditingUserId(user.userId);
                                setSelectedRoleId(user.roleId);
                              }}
                              className="action-edit"
                              title="Rol Düzenle"
                            >
                              <Edit className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleResetPassword(user.userId)}
                              className="action-view"
                              title="Şifre Sıfırla"
                            >
                              <Key className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleDeleteUser(user.userId, `${user.firstName} ${user.lastName}`)}
                              className="action-delete"
                              title="Sil"
                            >
                              <Trash className="h-4 w-4" />
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <div className="card-header">
            <div className="flex justify-between items-center">
              <div className="pagination-info">
                Toplam {users.totalElements} kullanıcıdan {users.content.length} tanesi gösteriliyor
              </div>
              <div className="flex space-x-2">
                <button
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={users.first}
                  className="pagination-button"
                >
                  Önceki
                </button>
                <span className="pagination-info">
                  Sayfa {users.number + 1} / {users.totalPages}
                </span>
                <button
                  onClick={() => setCurrentPage(Math.min(users.totalPages - 1, currentPage + 1))}
                  disabled={users.last}
                  className="pagination-button"
                >
                  Sonraki
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderVehicles = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Araç Yönetimi</h2>
        <div className="flex gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <input
              type="text"
              placeholder="Araç ara..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && searchVehicles()}
              className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button
            onClick={searchVehicles}
            className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
          >
            Ara
          </button>
          <button
            onClick={() => setShowVehicleModal(true)}
            className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors flex items-center gap-2"
          >
            <Plus className="h-4 w-4" />
            Araç Ekle
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-4 text-left table-header">Araç</th>
              <th className="px-6 py-4 text-left table-header">Plaka</th>
              <th className="px-6 py-4 text-left table-header">Kategori</th>
              <th className="px-6 py-4 text-left table-header">Günlük Ücret</th>
              <th className="px-6 py-4 text-left table-header">Durum</th>
              <th className="px-6 py-4 text-left table-header">İşlemler</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {vehicles?.content.map((vehicle) => (
              <tr key={vehicle.vehicleId}>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div>
                      <div className="text-sm font-medium">
                        {vehicle.brandName} {vehicle.modelName}
                      </div>
                      <div className="text-sm">
                        {vehicle.year} - {vehicle.color}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {vehicle.licensePlate}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                    {vehicle.categoryName}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {formatCurrency(vehicle.dailyRate)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                    vehicle.isActive 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {vehicle.statusName}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleVehicleStatusToggle(vehicle.vehicleId)}
                      className="text-indigo-600 hover:text-indigo-900"
                      title="Durumu Değiştir"
                    >
                      {vehicle.isActive ? <ToggleRight className="h-4 w-4" /> : <ToggleLeft className="h-4 w-4" />}
                    </button>
                    <button 
                      onClick={() => {
                        setEditingVehicle(vehicle);
                        setShowVehicleModal(true);
                      }}
                      className="text-blue-600 hover:text-blue-900"
                      title="Düzenle"
                    >
                      <Edit className="h-4 w-4" />
                    </button>
                    <button 
                      onClick={() => handleVehicleDelete(vehicle.vehicleId, `${vehicle.brandName} ${vehicle.modelName}`)}
                      className="text-red-600 hover:text-red-900"
                      title="Sil"
                    >
                      <Trash className="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {vehicles && vehicles.totalPages > 1 && (
        <div className="flex justify-between items-center">
                    <div className="pagination-info">
            Toplam {vehicles.totalElements} araçtan {vehicles.content.length} tanesi gösteriliyor
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={vehicles.first}
              className="pagination-button"
            >
              Önceki
            </button>
            <span className="pagination-info">
              {currentPage + 1} / {vehicles.totalPages}
            </span>
            <button
              onClick={() => setCurrentPage(Math.min(vehicles.totalPages - 1, currentPage + 1))}
              disabled={vehicles.last}
              className="pagination-button"
            >
              Sonraki
            </button>
          </div>
        </div>
      )}
    </div>
  );

  const renderRentals = () => {
    if (loading) {
      return (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
        </div>
      );
    }

    return (
      <div className="space-y-6">
        {/* Search and Actions */}
        <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
          <div className="flex-1 max-w-md">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Kiralama ara..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleRentalSearch()}
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent w-full"
              />
            </div>
          </div>
          <button
            onClick={handleRentalSearch}
            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center space-x-2"
          >
            <Search className="h-4 w-4" />
            <span>Ara</span>
          </button>
        </div>

        {/* Rentals Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Müşteri
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Araç
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tarihler
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Durum
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tutar
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ödeme
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {rentals?.content?.map((rental) => (
                  <tr key={rental.rentalId} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      #{rental.rentalId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {rental.customerName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {rental.vehicleBrand} {rental.vehicleModel}
                      <div className="text-xs text-gray-500">{rental.vehiclePlate}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <div>{formatDate(rental.startDate)}</div>
                      <div className="text-xs text-gray-500">→ {formatDate(rental.endDate)}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        rental.statusName === 'Aktif' ? 'bg-green-100 text-green-800' :
                        rental.statusName === 'Tamamlandi' ? 'bg-blue-100 text-blue-800' :
                        rental.statusName === 'Rezerve Edildi' ? 'bg-yellow-100 text-yellow-800' :
                        rental.statusName === 'Gecikmis' ? 'bg-red-100 text-red-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {rental.statusName}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <div className="font-medium">
                        ${rental.totalCost.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        rental.paymentStatus === 'Paid' ? 'bg-green-100 text-green-800' :
                        rental.paymentStatus === 'Pending' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {rental.paymentStatus === 'Paid' ? 'Ödendi' : 
                         rental.paymentStatus === 'Pending' ? 'Beklemede' : 'Ödenmedi'}
                      </span>
                      <div className="text-xs text-gray-500 mt-1">
                        Ödenen: ${rental.totalPaid.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </div>
                      <div className="text-xs text-gray-500">
                        Kalan: ${rental.remainingAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {rentals && rentals.totalPages > 1 && (
            <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
              <div className="flex-1 flex justify-between sm:hidden">
                <button
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                  className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
                >
                  Önceki
                </button>
                <button
                  onClick={() => setCurrentPage(Math.min(rentals.totalPages - 1, currentPage + 1))}
                  disabled={currentPage >= rentals.totalPages - 1}
                  className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
                >
                  Sonraki
                </button>
              </div>
              <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm text-gray-700">
                    Toplam <span className="font-medium">{rentals.totalElements}</span> kayıt,{' '}
                    <span className="font-medium">{currentPage + 1}</span> / <span className="font-medium">{rentals.totalPages}</span> sayfa
                  </p>
                </div>
                <div>
                  <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                    <button
                      onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                      disabled={currentPage === 0}
                      className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                    >
                      Önceki
                    </button>
                    <button
                      onClick={() => setCurrentPage(Math.min(rentals.totalPages - 1, currentPage + 1))}
                      disabled={currentPage >= rentals.totalPages - 1}
                      className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                    >
                      Sonraki
                    </button>
                  </nav>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

  const renderDebug = () => (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          <Settings className="h-5 w-5 mr-2 text-red-600" />
          Sistem Bakım ve Debug İşlemleri
        </h3>
        
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-yellow-800">
                Dikkat!
              </h3>
              <div className="mt-2 text-sm text-yellow-700">
                <p>Bu işlemler veritabanını doğrudan etkiler ve geri alınamaz. Lütfen dikkatli kullanın.</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Kiralama Temizleme */}
          <div className="border border-gray-200 rounded-lg p-4">
            <h4 className="text-md font-medium text-gray-900 mb-2 flex items-center">
              <Trash className="h-4 w-4 mr-2 text-red-500" />
              Eski Kiralama Kayıtları
            </h4>
            <p className="text-sm text-gray-600 mb-4">
              Geçmiş tarihli ve tamamlanmamış kiralama kayıtlarını "Tamamlandı" olarak işaretler.
              Bu işlem araçların müsaitlik durumunu düzeltir.
            </p>
            <button
              onClick={handleCleanupOldRentals}
              disabled={debugLoading}
              className="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors flex items-center justify-center space-x-2"
            >
              {debugLoading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  <span>Temizleniyor...</span>
                </>
              ) : (
                <>
                  <Trash className="h-4 w-4" />
                  <span>Eski Kayıtları Temizle</span>
                </>
              )}
            </button>
          </div>

          {/* Araç Debug */}
          <div className="border border-gray-200 rounded-lg p-4">
            <h4 className="text-md font-medium text-gray-900 mb-2 flex items-center">
              <Car className="h-4 w-4 mr-2 text-blue-500" />
              Araç Debug Bilgisi
            </h4>
            <p className="text-sm text-gray-600 mb-4">
              Belirli bir aracın kiralama durumunu ve aktif kayıtlarını kontrol eder.
            </p>
            <div className="flex space-x-2">
              <input
                type="number"
                placeholder="Araç ID"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    const vehicleId = parseInt((e.target as HTMLInputElement).value);
                    if (vehicleId) handleVehicleDebugInfo(vehicleId);
                  }
                }}
              />
              <button
                onClick={() => {
                  const input = document.querySelector('input[placeholder="Araç ID"]') as HTMLInputElement;
                  const vehicleId = parseInt(input.value);
                  if (vehicleId) handleVehicleDebugInfo(vehicleId);
                }}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
              >
                <Eye className="h-4 w-4" />
                <span>Kontrol Et</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const handleRentalSearch = async () => {
    if (!searchTerm.trim()) {
      loadRentals();
      return;
    }

    try {
      setLoading(true);
      const searchResults = await adminService.searchRentals(searchTerm, currentPage, 10);
      setRentals(searchResults);
    } catch (error) {
      console.error('Kiralama arama hatası:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCleanupOldRentals = async () => {
    const confirmCleanup = window.confirm(
      'Geçmiş tarihli ve tamamlanmamış kiralama kayıtlarını temizlemek istediğinizden emin misiniz? Bu işlem geri alınamaz.'
    );
    
    if (!confirmCleanup) return;

    try {
      setDebugLoading(true);
      const response = await fetch('http://localhost:8080/api/database-integration/debug/cleanup-old-rentals', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const result = await response.json();
      
      if (result.success) {
        alert(`✅ Başarılı! ${result.updatedRecords} kiralama kaydı temizlendi.`);
        // Rentals sekmesindeyse listeyi yenile
        if (activeTab === 'rentals') {
          loadRentals();
        }
      } else {
        alert(`❌ Hata: ${result.message}`);
      }
    } catch (error) {
      console.error('Temizleme hatası:', error);
      alert('❌ Temizleme işlemi sırasında bir hata oluştu.');
    } finally {
      setDebugLoading(false);
    }
  };

  const handleVehicleDebugInfo = async (vehicleId: number) => {
    try {
      const response = await fetch(`http://localhost:8080/api/database-integration/debug/vehicle-rental-status/${vehicleId}`);
      const result = await response.json();
      
      const debugInfo = JSON.stringify(result, null, 2);
      alert(`Vehicle ${vehicleId} Debug Info:\n\n${debugInfo}`);
    } catch (error) {
      console.error('Debug info hatası:', error);
      alert('❌ Debug bilgisi alınırken hata oluştu.');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-white text-5xl font-bold mb-2">Admin Panel</h1>
          <p className="text-white text-lg">Sistem yönetimi ve kontrol merkezi</p>
        </div>

        {/* Navigation Tabs */}
        <div className="card-header mb-8">
          <nav className="flex space-x-2 overflow-x-auto">
            {[
              { id: 'dashboard', label: 'Dashboard', icon: BarChart3 },
              { id: 'users', label: 'Kullanıcılar', icon: Users },
              { id: 'vehicles', label: 'Araçlar', icon: Car },
              { id: 'rentals', label: 'Kiralamalar', icon: Calendar },
              { id: 'debug', label: 'Debug', icon: Settings },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`nav-tab flex items-center space-x-2 whitespace-nowrap ${
                  activeTab === tab.id ? 'nav-tab-active' : ''
                }`}
              >
                <tab.icon className="h-5 w-5" />
                <span>{tab.label}</span>
              </button>
            ))}
          </nav>
        </div>

        {/* Content */}
        <div className="animate-fade-in-up">
          {activeTab === 'dashboard' && renderDashboard()}
          {activeTab === 'users' && renderUsers()}
          {activeTab === 'vehicles' && renderVehicles()}
          {activeTab === 'rentals' && renderRentals()}
          {activeTab === 'debug' && renderDebug()}
        </div>

        {/* Modals */}
        {showVehicleModal && (
          <VehicleModal
            onClose={() => setShowVehicleModal(false)}
            onSave={handleSaveVehicle}
            vehicle={editingVehicle}
            references={vehicleReferences}
            onReferencesRefresh={loadVehicleReferences}
          />
        )}
        
        {showUserModal && (
          <UserModal
            isOpen={showUserModal}
            onClose={() => setShowUserModal(false)}
            onSave={handleSaveUser}
            roles={roles}
          />
        )}
      </div>
    </div>
  );
};

export default AdminPanel; 