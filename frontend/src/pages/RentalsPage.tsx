import React, { useState, useEffect, useCallback } from 'react';
import { Car, Calendar, MapPin, Clock, CheckCircle, XCircle, AlertCircle, Plus, User, CreditCard } from 'lucide-react';
import rentalService, { Rental } from '../services/rentalService';
import { useAuth } from '../contexts/AuthContext';

interface RentalCardProps {
  rental: Rental;
  onStatusUpdate?: (rentalId: number, newStatus: number) => void;
  onPayment?: (rentalId: number) => void;
  onComplete?: (rentalId: number) => void;
}

const RentalCard: React.FC<RentalCardProps> = ({ rental, onStatusUpdate, onPayment, onComplete }) => {
  const getStatusIcon = (statusName: string) => {
    switch (statusName.toLowerCase()) {
      case 'rezerve edildi':
        return <Clock className="w-5 h-5 text-orange-500" />;
      case 'aktif':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'gecikmis':
        return <AlertCircle className="w-5 h-5 text-red-500" />;
      case 'tamamlandi':
        return <CheckCircle className="w-5 h-5 text-blue-500" />;
      case 'iptal edildi':
        return <XCircle className="w-5 h-5 text-red-500" />;
      case 'odendi':
        return <CheckCircle className="w-5 h-5 text-green-600" />;
      default:
        return <AlertCircle className="w-5 h-5 text-gray-500" />;
    }
  };

  const getStatusColor = (statusName: string) => {
    switch (statusName.toLowerCase()) {
      case 'rezerve edildi':
        return 'bg-orange-100 text-orange-800';
      case 'aktif':
        return 'bg-green-100 text-green-800';
      case 'gecikmis':
        return 'bg-red-100 text-red-800';
      case 'tamamlandi':
        return 'bg-blue-100 text-blue-800';
      case 'iptal edildi':
        return 'bg-red-100 text-red-800';
      case 'odendi':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('tr-TR');
  };

  const calculateDuration = (startDate: string, endDate: string) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
      <div className="flex justify-between items-start mb-4">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-blue-100 rounded-lg">
            <Car className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              {rental.vehicle.model.brand.brandName} {rental.vehicle.model.modelName}
            </h3>
            <p className="text-sm text-gray-600">{rental.vehicle.vehicleRegistration}</p>
          </div>
        </div>
        <div className={`flex items-center space-x-1 px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(rental.rentalStatus.statusName)}`}>
          {getStatusIcon(rental.rentalStatus.statusName)}
          <span>{rental.rentalStatus.statusName}</span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="flex items-center space-x-2 text-sm text-gray-600">
          <Calendar className="w-4 h-4" />
          <div>
            <span className="block font-medium">Başlangıç</span>
            <span>{formatDate(rental.plannedPickupDate)}</span>
          </div>
        </div>
        <div className="flex items-center space-x-2 text-sm text-gray-600">
          <Calendar className="w-4 h-4" />
          <div>
            <span className="block font-medium">Bitiş</span>
            <span>{formatDate(rental.plannedReturnDate)}</span>
          </div>
        </div>
        <div className="flex items-center space-x-2 text-sm text-gray-600">
          <Clock className="w-4 h-4" />
          <div>
            <span className="block font-medium">Süre</span>
            <span>{calculateDuration(rental.plannedPickupDate, rental.plannedReturnDate)} gün</span>
          </div>
        </div>
        <div className="flex items-center space-x-2 text-sm text-gray-600">
          <CreditCard className="w-4 h-4" />
          <div>
            <span className="block font-medium">Toplam</span>
            <span>₺{rental.totalAmount}</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="flex items-center space-x-2 text-sm text-gray-600">
          <MapPin className="w-4 h-4" />
          <div>
            <span className="block font-medium">Alış</span>
            <span>{rental.pickupLocation?.locationName || 'Belirtilmemiş'}</span>
          </div>
        </div>
        <div className="flex items-center space-x-2 text-sm text-gray-600">
          <MapPin className="w-4 h-4" />
          <div>
            <span className="block font-medium">Teslim</span>
            <span>{rental.returnLocation?.locationName || 'Belirtilmemiş'}</span>
          </div>
        </div>
      </div>

      <div className="flex justify-between items-center pt-4 border-t">
        <span className="text-sm text-gray-500">
          Oluşturulma: {formatDate(rental.createdDate)}
        </span>
        <div className="flex space-x-2">
          {/* Rezerve edilenler için ödeme butonu - ID 1 */}
          {(() => {
            const statusName = rental.rentalStatus.statusName.toLowerCase();
            return statusName === 'rezerve edildi';
          })() && (
            <button 
              onClick={() => onPayment && onPayment(rental.rentalId)}
              className="px-3 py-1 text-sm bg-green-100 text-green-700 rounded hover:bg-green-200 transition flex items-center space-x-1"
            >
              <CreditCard className="w-4 h-4" />
              <span>Ödeme Yap</span>
            </button>
          )}
          
          {/* Aktif kiralamalar için tamamlama butonu - ID 2, ID 3 */}
          {(() => {
            const statusName = rental.rentalStatus.statusName.toLowerCase();
            return statusName === 'aktif' || statusName === 'gecikmis';
          })() && (
            <>
              <button 
                onClick={() => onComplete && onComplete(rental.rentalId)}
                className="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200 transition flex items-center space-x-1"
              >
                <CheckCircle className="w-4 h-4" />
                <span>Tamamlandı</span>
              </button>
              <button 
                onClick={() => onStatusUpdate && onStatusUpdate(rental.rentalId, 5)} // Cancelled status (ID 5)
                className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200 transition"
              >
                İptal Et
              </button>
            </>
          )}
          
          <button className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition">
            Detaylar
          </button>
        </div>
      </div>
    </div>
  );
};

const RentalsPage: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const [rentals, setRentals] = useState<Rental[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'reserved' | 'active' | 'completed' | 'all'>('all');

  const loadRentals = useCallback(async () => {
    try {
      console.log('🔄 loadRentals called for user:', user?.userId);
      setIsLoading(true);
      setError(null);
      const rentalsData = await rentalService.getUserRentals(user?.userId);
      const rentalsArray = Array.isArray(rentalsData) ? rentalsData : [];
      
      console.log('📊 Loaded rentals:', rentalsArray.length);
      rentalsArray.forEach(rental => {
        console.log(`📋 Rental ${rental.rentalId}: status="${rental.rentalStatus.statusName}"`);
      });
      
      setRentals(rentalsArray);
    } catch (error) {
      console.error('❌ Error loading rentals:', error);
      setError('Kiralama verilerini yüklerken bir hata oluştu.');
      setRentals([]);
    } finally {
      setIsLoading(false);
    }
  }, [user?.userId]);

  useEffect(() => {
    if (isAuthenticated && user) {
      loadRentals();
    }
  }, [isAuthenticated, user, loadRentals]);

  const handleStatusUpdate = async (rentalId: number, newStatus: number) => {
    try {
      await rentalService.updateRentalStatus(rentalId, newStatus);
      loadRentals(); // Refresh the list
    } catch (error) {
      console.error('Error updating rental status:', error);
      alert('Durum güncellenirken bir hata oluştu.');
    }
  };

  const handlePayment = async (rentalId: number) => {
    try {
      console.log('💳 handlePayment called for rental:', rentalId);
      
      // Ödeme işlemi simülasyonu - gerçek uygulamada ödeme gateway'i entegrasyonu olacak
      const confirmed = window.confirm('Ödeme işlemini onaylıyor musunuz? Bu işlem kiralama durumunu "Aktif" olarak güncelleyecektir.');
      
      if (confirmed) {
        console.log('✅ User confirmed payment for rental:', rentalId);
        
        // Status 2 = Aktif olarak varsayıyoruz
        console.log('📡 Calling updateRentalStatus API with status 2 (Aktif)...');
        await rentalService.updateRentalStatus(rentalId, 2);
        
        console.log('✅ Payment API call successful');
        alert('✅ Ödeme başarıyla tamamlandı! Kiralama aktif duruma geçirildi.');
        
        console.log('🔄 Reloading rentals after payment...');
        // Kısa bir delay ekleyelim ki backend işlemi tamamlansın
        setTimeout(() => {
          loadRentals(); // Refresh the list
        }, 500);
      } else {
        console.log('❌ User cancelled payment');
      }
    } catch (error) {
      console.error('❌ Error processing payment:', error);
      alert('❌ Ödeme işlemi sırasında bir hata oluştu.');
    }
  };

  const handleComplete = async (rentalId: number) => {
    try {
      console.log('🔄 handleComplete called for rental:', rentalId);
      
      const confirmed = window.confirm('Kiralama tamamlandı olarak işaretlensin mi? Bu işlem araç durumunu "Müsait" olarak güncelleyecektir.');
      
      if (confirmed) {
        console.log('✅ User confirmed completion for rental:', rentalId);
        
        // Status 4 = Tamamlandı (veritabanında ID 4)
        console.log('📡 Calling updateRentalStatus API with status 4 (Tamamlandi)...');
        await rentalService.updateRentalStatus(rentalId, 4);
        
        console.log('✅ API call successful, showing success message');
        alert('✅ Kiralama başarıyla tamamlandı! Araç tekrar müsait duruma geçirildi.');
        
        console.log('🔄 Reloading rentals after completion...');
        // Backend transaction'ının tamamlanması için daha uzun delay
        setTimeout(() => {
          loadRentals(); // Refresh the list
        }, 1500);
      } else {
        console.log('❌ User cancelled completion');
      }
    } catch (error) {
      console.error('❌ Error completing rental:', error);
      alert('❌ Kiralama tamamlanırken bir hata oluştu.');
    }
  };

  const filteredRentals = rentals.filter(rental => {
    const statusName = rental.rentalStatus.statusName.toLowerCase();
    
    switch (activeTab) {
      case 'reserved':
        // ID 1: Rezerve Edildi
        return statusName === 'rezerve edildi';
      case 'active':
        // ID 2: Aktif, ID 3: Gecikmis
        const isActive = statusName === 'aktif' || statusName === 'gecikmis';
        
        // Debug: Aktif sekmesinde hangi kiralamalar görünüyor
        if (isActive) {
          console.log(`🟢 Active rental found: ${rental.rentalId} with status "${rental.rentalStatus.statusName}"`);
        }
        
        return isActive;
      case 'completed':
        // ID 4: Tamamlandi, ID 6: Odendi
        return statusName === 'tamamlandi' || statusName === 'odendi';
      case 'all':
      default:
        return true;
    }
  });

  if (!isAuthenticated) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <User className="mx-auto h-12 w-12 text-gray-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">Giriş Yapmanız Gerekiyor</h3>
          <p className="text-gray-600 mb-4">Kiralamalarınızı görüntülemek için lütfen giriş yapın.</p>
          <button 
            onClick={() => window.location.href = '/login'}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Giriş Yap
          </button>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-center items-center min-h-64">
          <div className="loading-spinner"></div>
          <span className="ml-3 text-gray-600">Kiralamalar yükleniyor...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-800">Kiralamalarım</h1>
        <button 
          onClick={() => window.location.href = '/vehicles'}
          className="flex items-center space-x-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
        >
          <Plus size={20} />
          <span>Yeni Kiralama</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex space-x-1 mb-6 bg-gray-100 p-1 rounded-lg w-fit">
        <button
          onClick={() => setActiveTab('all')}
          className={`px-4 py-2 rounded-md text-sm font-medium transition ${
            activeTab === 'all'
              ? 'bg-white text-blue-600 shadow-sm'
              : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          Tümü ({rentals.length})
        </button>
        <button
          onClick={() => setActiveTab('reserved')}
          className={`px-4 py-2 rounded-md text-sm font-medium transition ${
            activeTab === 'reserved'
              ? 'bg-white text-orange-600 shadow-sm'
              : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          Rezerve Edilenler ({rentals.filter(r => {
            const statusName = r.rentalStatus.statusName.toLowerCase();
            return statusName === 'rezerve edildi';
          }).length})
        </button>
        <button
          onClick={() => setActiveTab('active')}
          className={`px-4 py-2 rounded-md text-sm font-medium transition ${
            activeTab === 'active'
              ? 'bg-white text-green-600 shadow-sm'
              : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          Aktif ({rentals.filter(r => {
            const statusName = r.rentalStatus.statusName.toLowerCase();
            return statusName === 'aktif' || statusName === 'gecikmis';
          }).length})
        </button>
        <button
          onClick={() => setActiveTab('completed')}
          className={`px-4 py-2 rounded-md text-sm font-medium transition ${
            activeTab === 'completed'
              ? 'bg-white text-blue-600 shadow-sm'
              : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          Tamamlanan ({rentals.filter(r => {
            const statusName = r.rentalStatus.statusName.toLowerCase();
            return statusName === 'tamamlandi' || statusName === 'odendi';
          }).length})
        </button>
      </div>

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <div className="flex items-center">
            <XCircle className="w-5 h-5 text-red-400 mr-2" />
            <span className="text-red-800">{error}</span>
          </div>
        </div>
      )}

      {/* Rentals List */}
      {filteredRentals.length === 0 ? (
        <div className="text-center py-12">
          <Car className="mx-auto h-12 w-12 text-gray-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            {activeTab === 'reserved' ? 'Rezerve edilmiş kiralama bulunamadı' :
             activeTab === 'active' ? 'Aktif kiralama bulunamadı' : 
             activeTab === 'completed' ? 'Tamamlanan kiralama bulunamadı' : 
             'Hiç kiralama bulunamadı'}
          </h3>
          <p className="text-gray-600 mb-4">Araç kiralamaya başlamak için yeni bir kiralama oluşturun.</p>
          <button 
            onClick={() => window.location.href = '/vehicles'}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Araçları Görüntüle
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6">
          {filteredRentals.map(rental => (
            <RentalCard 
              key={rental.rentalId} 
              rental={rental} 
              onStatusUpdate={handleStatusUpdate}
              onPayment={handlePayment}
              onComplete={handleComplete}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default RentalsPage; 