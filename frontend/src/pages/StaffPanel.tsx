import React, { useState, useEffect } from 'react';
import { 
  Users, 
  Car, 
  Calendar,
  FileText,
  Search,
  Eye,
  Edit,
  Image,
  Plus,
  X
} from 'lucide-react';
import { AdminUser, AdminVehicle, PaginatedResponse } from '../types/admin';
import * as adminService from '../services/adminService';
import * as staffService from '../services/staffService';
import VehicleModal from '../components/VehicleModal';

const StaffPanel: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'vehicles' | 'customers'>('vehicles');
  const [users, setUsers] = useState<PaginatedResponse<AdminUser> | null>(null);
  const [vehicles, setVehicles] = useState<PaginatedResponse<AdminVehicle> | null>(null);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  
  // Image management states
  const [showImageModal, setShowImageModal] = useState(false);
  const [selectedVehicleId, setSelectedVehicleId] = useState<number | null>(null);
  const [vehicleImages, setVehicleImages] = useState<string[]>([]);
  const [newImageUrl, setNewImageUrl] = useState('');

  // Vehicle editing states
  const [showVehicleModal, setShowVehicleModal] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState<AdminVehicle | null>(null);
  const [vehicleReferences, setVehicleReferences] = useState<any>(null);

  useEffect(() => {
    if (activeTab === 'customers') {
      loadUsers();
    } else if (activeTab === 'vehicles') {
      loadVehicles();
      if (!vehicleReferences) {
        loadVehicleReferences();
      }
    }
  }, [activeTab, currentPage]);

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

  const loadVehicleReferences = async () => {
    try {
      const data = await adminService.getVehicleReferences();
      setVehicleReferences(data);
    } catch (error) {
      console.error('Error loading vehicle references:', error);
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

  // Image management functions
  const openImageModal = async (vehicleId: number) => {
    setSelectedVehicleId(vehicleId);
    setShowImageModal(true);
    await loadVehicleImages(vehicleId);
  };

  const loadVehicleImages = async (vehicleId: number) => {
    try {
      const images = await staffService.getVehicleImages(vehicleId);
      setVehicleImages(images);
    } catch (error) {
      console.error('Araç resimleri yüklenirken hata:', error);
    }
  };

  const addVehicleImage = async () => {
    if (!newImageUrl.trim() || !selectedVehicleId) return;

    try {
      const result = await staffService.addVehicleImage(selectedVehicleId, newImageUrl);
      if (result.success) {
        setNewImageUrl('');
        await loadVehicleImages(selectedVehicleId);
        alert('Resim başarıyla eklendi');
      } else {
        alert('Resim eklenemedi: ' + result.message);
      }
    } catch (error) {
      console.error('Resim eklenirken hata:', error);
      alert('Resim eklenirken bir hata oluştu');
    }
  };

  const removeVehicleImage = async (imageUrl: string) => {
    if (!selectedVehicleId) return;

    if (window.confirm('Bu resmi silmek istediğinizden emin misiniz?')) {
      try {
        const result = await staffService.removeVehicleImage(selectedVehicleId, imageUrl);
        if (result.success) {
          await loadVehicleImages(selectedVehicleId);
          alert('Resim başarıyla silindi');
        } else {
          alert('Resim silinemedi: ' + result.message);
        }
      } catch (error) {
        console.error('Resim silinirken hata:', error);
        alert('Resim silinirken bir hata oluştu');
      }
    }
  };

  const closeImageModal = () => {
    setShowImageModal(false);
    setSelectedVehicleId(null);
    setVehicleImages([]);
    setNewImageUrl('');
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
                    <button className="text-gray-600 hover:text-gray-900" title="Detayları Görüntüle">
                      <Eye className="h-4 w-4" />
                    </button>
                    <button 
                      onClick={() => openImageModal(vehicle.vehicleId)}
                      className="text-blue-600 hover:text-blue-900"
                      title="Resim Yönetimi"
                    >
                      <Image className="h-4 w-4" />
                    </button>
                    <button 
                      onClick={() => {
                        setEditingVehicle(vehicle);
                        setShowVehicleModal(true);
                      }}
                      className="text-green-600 hover:text-green-900" 
                      title="Düzenle"
                    >
                      <Edit className="h-4 w-4" />
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

  const renderCustomers = () => (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white">Müşteri Bilgileri</h2>
        <div className="flex gap-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <input
              type="text"
              placeholder="Müşteri ara..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && searchUsers()}
              className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button
            onClick={searchUsers}
            className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
          >
            Ara
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-4 text-left table-header">Müşteri</th>
              <th className="px-6 py-4 text-left table-header">Email</th>
              <th className="px-6 py-4 text-left table-header">Telefon</th>
              <th className="px-6 py-4 text-left table-header">Toplam Kiralama</th>
              <th className="px-6 py-4 text-left table-header">Durum</th>
              <th className="px-6 py-4 text-left table-header">İşlemler</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {users?.content.map((user) => (
              <tr key={user.userId}>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div>
                      <div className="text-sm font-medium">
                        {user.firstName} {user.lastName}
                      </div>
                      <div className="text-sm">@{user.username}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {user.email}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {user.phoneNumber || '-'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  {user.totalRentals}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                    user.isActive 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {user.isActive ? 'Aktif' : 'Pasif'}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div className="flex space-x-2">
                    <button className="text-gray-600 hover:text-gray-900">
                      <Eye className="h-4 w-4" />
                    </button>
                    <button className="text-gray-600 hover:text-gray-900">
                      <Edit className="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {users && users.totalPages > 1 && (
        <div className="flex justify-between items-center">
          <div className="pagination-info">
            Toplam {users.totalElements} müşteriden {users.content.length} tanesi gösteriliyor
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
              {currentPage + 1} / {users.totalPages}
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
      )}
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-white">Staff Panel</h1>
        </div>

        {/* Navigation Tabs */}
        <div className="border-b border-gray-200 mb-8">
          <nav className="-mb-px flex space-x-8">
            {[
              { id: 'vehicles', label: 'Araçlar', icon: Car },
              { id: 'customers', label: 'Müşteriler', icon: Users },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  setActiveTab(tab.id as any);
                  setCurrentPage(0);
                  setSearchTerm('');
                }}
                className={`${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-white hover:text-gray-300 hover:border-gray-300'
                } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center space-x-2`}
              >
                <tab.icon className="h-4 w-4" />
                <span>{tab.label}</span>
              </button>
            ))}
          </nav>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
            <p className="text-white ml-4">Yükleniyor...</p>
          </div>
        )}

        {/* Tab Content */}
        {!loading && (
          <>
            {activeTab === 'vehicles' && renderVehicles()}
            {activeTab === 'customers' && renderCustomers()}
          </>
        )}

        {/* Image Management Modal */}
        {showImageModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-4xl max-h-[90vh] overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold">Araç Resim Yönetimi</h3>
                <button
                  onClick={closeImageModal}
                  className="text-gray-500 hover:text-gray-700"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>

              {/* Add New Image */}
              <div className="mb-6">
                <h4 className="text-md font-medium mb-2">Yeni Resim Ekle</h4>
                <div className="flex gap-2">
                  <input
                    type="url"
                    placeholder="Resim URL'si girin..."
                    value={newImageUrl}
                    onChange={(e) => setNewImageUrl(e.target.value)}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  <button
                    onClick={addVehicleImage}
                    disabled={!newImageUrl.trim()}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                  >
                    <Plus className="h-4 w-4" />
                    Ekle
                  </button>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  Örnek: https://example.com/images/car1.jpg
                </p>
              </div>

              {/* Current Images */}
              <div>
                <h4 className="text-md font-medium mb-2">Mevcut Resimler</h4>
                {vehicleImages.length === 0 ? (
                  <p className="text-gray-500 text-center py-8">Henüz resim eklenmemiş</p>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {vehicleImages.map((imageUrl, index) => (
                      <div key={index} className="relative group">
                        <img
                          src={imageUrl}
                          alt={`Araç resmi ${index + 1}`}
                          className="w-full h-48 object-cover rounded-lg border"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src = 'https://via.placeholder.com/300x200?text=Resim+Yüklenemedi';
                          }}
                        />
                        <button
                          onClick={() => removeVehicleImage(imageUrl)}
                          className="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                        >
                          <X className="h-4 w-4" />
                        </button>
                        <div className="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 text-white p-2 rounded-b-lg opacity-0 group-hover:opacity-100 transition-opacity">
                          <p className="text-xs truncate">{imageUrl}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Vehicle Edit Modal */}
        {showVehicleModal && (
          <VehicleModal
            vehicle={editingVehicle}
            references={vehicleReferences}
            onSave={(data) => handleVehicleUpdate(editingVehicle!.vehicleId, data)}
            onClose={() => {
              setShowVehicleModal(false);
              setEditingVehicle(null);
            }}
          />
        )}
      </div>
    </div>
  );
};

export default StaffPanel; 