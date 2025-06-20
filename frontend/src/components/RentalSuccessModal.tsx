import React from 'react';
import { CheckCircle, X, Calendar, MapPin, Car, DollarSign, Clock, User, FileText } from 'lucide-react';

interface RentalSuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  rentalData: {
    rentalId: number;
    vehicleName: string;
    vehicleRegistration: string;
    customerName: string;
    startDate: string;
    endDate: string;
    totalAmount: number;
    pickupLocation: string;
    returnLocation: string;
    totalDays: number;
  } | null;
}

const RentalSuccessModal: React.FC<RentalSuccessModalProps> = ({ 
  isOpen, 
  onClose, 
  rentalData 
}) => {
  if (!isOpen || !rentalData) return null;

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('tr-TR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-2 sm:p-4">
      <div className="bg-white rounded-xl max-w-2xl w-full max-h-[95vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-green-100 rounded-full">
              <CheckCircle className="w-8 h-8 text-green-600" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">Kiralama Başarılı!</h2>
              <p className="text-gray-600">Rezervasyonunuz onaylandı</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="w-5 h-5 text-gray-500" />
          </button>
        </div>

        {/* Content */}
        <div className="p-4 sm:p-6 space-y-4">
          {/* Rental ID */}
          <div className="text-center bg-green-50 rounded-lg p-4">
            <h3 className="text-lg font-semibold text-green-800 mb-2">
              Rezervasyon Numaranız
            </h3>
            <div className="text-3xl font-bold text-green-600">
              #{rentalData.rentalId}
            </div>
            <p className="text-sm text-green-700 mt-2">
              Bu numarayı not alın veya ekran görüntüsü alın
            </p>
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {/* Left Column */}
            <div className="space-y-4">
              {/* Vehicle Info */}
              <div className="bg-gray-50 rounded-lg p-3">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center text-sm">
                  <Car className="w-4 h-4 mr-2 text-gray-600" />
                  Araç Bilgileri
                </h4>
                <div className="space-y-1 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Araç:</span>
                    <span className="font-medium">{rentalData.vehicleName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Plaka:</span>
                    <span className="font-medium">{rentalData.vehicleRegistration}</span>
                  </div>
                </div>
              </div>

              {/* Rental Details */}
              <div className="bg-blue-50 rounded-lg p-3">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center text-sm">
                  <Calendar className="w-4 h-4 mr-2 text-blue-600" />
                  Kiralama Detayları
                </h4>
                <div className="space-y-1 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Alış:</span>
                    <span className="font-medium text-xs">{formatDate(rentalData.startDate)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">İade:</span>
                    <span className="font-medium text-xs">{formatDate(rentalData.endDate)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Süre:</span>
                    <span className="font-medium">{rentalData.totalDays} gün</span>
                  </div>
                </div>
              </div>

              {/* Location Info */}
              <div className="bg-orange-50 rounded-lg p-3">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center text-sm">
                  <MapPin className="w-4 h-4 mr-2 text-orange-600" />
                  Lokasyon
                </h4>
                <div className="space-y-1 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Alış:</span>
                    <span className="font-medium text-xs">{rentalData.pickupLocation}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">İade:</span>
                    <span className="font-medium text-xs">{rentalData.returnLocation}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Right Column */}
            <div className="space-y-4">
              {/* Payment Info */}
              <div className="bg-green-50 rounded-lg p-3">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center text-sm">
                  <DollarSign className="w-4 h-4 mr-2 text-green-600" />
                  Ödeme Bilgileri
                </h4>
                <div className="space-y-1 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Toplam Tutar:</span>
                    <span className="font-bold text-lg text-green-600">
                      ${(rentalData.totalAmount || 0).toFixed(2)}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Ödeme Durumu:</span>
                    <span className="font-medium text-orange-600">Beklemede</span>
                  </div>
                </div>
              </div>

              {/* Customer Info */}
              <div className="bg-purple-50 rounded-lg p-3">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center text-sm">
                  <User className="w-4 h-4 mr-2 text-purple-600" />
                  Müşteri
                </h4>
                <div className="text-sm">
                  <span className="font-medium">{rentalData.customerName}</span>
                </div>
              </div>

              {/* Important Notes - Compact */}
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
                <h4 className="font-semibold text-gray-900 mb-2 flex items-center text-sm">
                  <FileText className="w-4 h-4 mr-2 text-yellow-600" />
                  Önemli Notlar
                </h4>
                <ul className="text-xs text-gray-700 space-y-1">
                  <li>• 30 dk önce ofiste bulunun</li>
                  <li>• Ehliyet ve kimlik getirin</li>
                  <li>• Hasar kontrolü yapılacak</li>
                  <li>• İade saatini geçmeyin</li>
                </ul>
              </div>
            </div>
          </div>


        </div>

        {/* Footer */}
        <div className="flex flex-col sm:flex-row items-center justify-between p-4 sm:p-6 border-t border-gray-200 bg-gray-50 gap-3">
          <div className="text-xs sm:text-sm text-gray-600 flex items-center">
            <Clock className="w-4 h-4 inline mr-1" />
            Rezervasyon: {new Date().toLocaleString('tr-TR')}
          </div>
          <div className="flex space-x-3 w-full sm:w-auto">
            <button
              onClick={() => window.print()}
              className="flex-1 sm:flex-none px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm"
            >
              Yazdır
            </button>
            <button
              onClick={onClose}
              className="flex-1 sm:flex-none px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm"
            >
              Tamam
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RentalSuccessModal; 