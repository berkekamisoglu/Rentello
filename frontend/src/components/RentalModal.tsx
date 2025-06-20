import React, { useState, useEffect } from 'react';
import { X, Calendar, MapPin, Car, DollarSign, Clock, User, CreditCard } from 'lucide-react';
import { Vehicle } from '../types';
import pricingService, { PricingBreakdown } from '../services/pricingService';

interface RentalModalProps {
  vehicle: Vehicle | null;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (rentalData: {
    vehicleId: number;
    startDate: string;
    endDate: string;
    totalAmount: number;
    pickupLocation: string;
    returnLocation: string;
  }) => void;
  user: any;
}

const RentalModal: React.FC<RentalModalProps> = ({ 
  vehicle, 
  isOpen, 
  onClose, 
  onConfirm, 
  user 
}) => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [pickupTime, setPickupTime] = useState('10:00');
  const [returnTime, setReturnTime] = useState('18:00');
  const [pickupLocation, setPickupLocation] = useState('');
  const [returnLocation, setReturnLocation] = useState('');
  const [pricingBreakdown, setPricingBreakdown] = useState<PricingBreakdown | null>(null);
  const [isCalculating, setIsCalculating] = useState(false);
  const [notes, setNotes] = useState('');

  useEffect(() => {
    if (vehicle && isOpen) {
      console.log('Selected vehicle:', vehicle);
      
      // Set default dates (tomorrow and 8 days later to ensure valid dates)
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const nextWeek = new Date();
      nextWeek.setDate(nextWeek.getDate() + 8);
      
      setStartDate(tomorrow.toISOString().split('T')[0]);
      setEndDate(nextWeek.toISOString().split('T')[0]);
      
      // Set default locations
      setPickupLocation(vehicle.currentLocation?.locationName || '');
      setReturnLocation(vehicle.currentLocation?.locationName || '');
    }
  }, [vehicle, isOpen]);

  useEffect(() => {
    if (startDate && endDate && vehicle) {
      calculatePricing();
    }
  }, [startDate, endDate, vehicle]);

  const calculatePricing = async () => {
    console.log('calculatePricing called with:', { vehicle: !!vehicle, startDate, endDate });
    
    if (!vehicle || !startDate || !endDate) {
      console.log('Missing required data for pricing calculation');
      return;
    }
    
    setIsCalculating(true);
    try {
      // Try to get pricing from backend first
      const breakdown = await pricingService.getPricingBreakdown(
        vehicle.vehicleId,
        startDate,
        endDate
      );
      console.log('Backend pricing breakdown:', breakdown);
      
      // Convert backend format to frontend format
      const backendData = breakdown as any;
      const mappedBreakdown = {
        basePrice: (backendData.baseRate || 0) * (backendData.totalDays || 0),
        weekendSurcharge: (backendData.weekendDays || 0) * (backendData.baseRate || 0) * 0.2,
        seasonalAdjustment: 0,
        discountAmount: 0,
        taxAmount: (backendData.totalPrice || 0) * 0.15, // Estimate tax from total
        totalPrice: backendData.totalPrice || 0,
        totalDays: backendData.totalDays || 0,
        averageRate: backendData.averageRate || 0,
        breakdown: []
      };
      
      console.log('Mapped breakdown:', mappedBreakdown);
      setPricingBreakdown(mappedBreakdown);
    } catch (error) {
      console.error('Error calculating pricing:', error);
      // Fallback calculation with proper values
      const days = pricingService.calculateDays(startDate, endDate);
      const basePrice = vehicle.dailyRentalRate * days;
      
      // Calculate weekend surcharge (20% for weekend days)
      let weekendSurcharge = 0;
      let weekendDays = 0;
      
      const start = new Date(startDate);
      const end = new Date(endDate);
      
      for (let d = new Date(start); d < end; d.setDate(d.getDate() + 1)) {
        const dayOfWeek = d.getDay();
        if (dayOfWeek === 0 || dayOfWeek === 6) { // Sunday = 0, Saturday = 6
          weekendDays++;
          weekendSurcharge += vehicle.dailyRentalRate * 0.2;
        }
      }
      
      const subtotal = basePrice + weekendSurcharge;
      const taxAmount = subtotal * 0.18;
      const totalPrice = subtotal + taxAmount;
      
      // Debug logging
      console.log('Pricing calculation:', {
        days,
        basePrice,
        weekendDays,
        weekendSurcharge,
        subtotal,
        taxAmount,
        totalPrice,
        dailyRate: vehicle.dailyRentalRate
      });
      
      setPricingBreakdown({
        basePrice: basePrice,
        weekendSurcharge: weekendSurcharge,
        seasonalAdjustment: 0,
        discountAmount: 0,
        taxAmount: taxAmount,
        totalPrice: totalPrice,
        totalDays: days,
        averageRate: totalPrice / days,
        breakdown: []
      });
    } finally {
      setIsCalculating(false);
    }
  };

  const handleConfirm = () => {
    if (!vehicle || !startDate || !endDate || !pricingBreakdown) return;

    onConfirm({
      vehicleId: vehicle.vehicleId,
      startDate: `${startDate}T${pickupTime}:00`,
      endDate: `${endDate}T${returnTime}:00`,
      totalAmount: pricingBreakdown.totalPrice,
      pickupLocation,
      returnLocation
    });
  };

  const isValidDates = () => {
    if (!startDate || !endDate) {
      console.log('❌ Invalid dates: missing startDate or endDate', { startDate, endDate });
      return false;
    }
    const start = new Date(startDate);
    const end = new Date(endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const isValid = start >= today && end > start;
    console.log('📅 Date validation:', { 
      startDate, 
      endDate, 
      start: start.toISOString(), 
      end: end.toISOString(), 
      today: today.toISOString(), 
      startAfterToday: start >= today,
      endAfterStart: end > start,
      isValid 
    });
    
    return isValid;
  };

  if (!isOpen || !vehicle) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-2 sm:p-4">
      <div className="bg-white rounded-xl max-w-4xl w-full max-h-[95vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-green-100 rounded-lg">
              <Car className="w-6 h-6 text-green-600" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">Araç Kiralama</h2>
              <p className="text-gray-600">
                {vehicle.model?.brand?.brandName} {vehicle.model?.modelName}
              </p>
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
          {/* Vehicle Info */}
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center space-x-4">
              <div className="w-20 h-20 rounded-lg overflow-hidden flex-shrink-0">
                {(() => {
                  try {
                    const images = vehicle.imageUrls ? JSON.parse(vehicle.imageUrls) : [];
                    return images.length > 0 ? (
                      <img
                        src={images[0]}
                        alt={`${vehicle.model?.brand?.brandName} ${vehicle.model?.modelName}`}
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          const target = e.target as HTMLImageElement;
                          target.style.display = 'none';
                          target.nextElementSibling?.classList.remove('hidden');
                        }}
                      />
                    ) : (
                      <div className="w-full h-full bg-gray-200 flex items-center justify-center">
                        <Car className="w-8 h-8 text-gray-500" />
                      </div>
                    );
                  } catch {
                    return (
                      <div className="w-full h-full bg-gray-200 flex items-center justify-center">
                        <Car className="w-8 h-8 text-gray-500" />
                      </div>
                    );
                  }
                })()}
                <div className="w-full h-full bg-gray-200 flex items-center justify-center hidden">
                  <Car className="w-8 h-8 text-gray-500" />
                </div>
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-gray-900 truncate">
                  {vehicle.model?.brand?.brandName} {vehicle.model?.modelName}
                </h3>
                <p className="text-sm text-gray-600">{vehicle.vehicleRegistration}</p>
                <div className="flex items-center space-x-4 mt-2 text-sm text-gray-600">
                  <span className="flex items-center space-x-1">
                    <MapPin className="w-4 h-4" />
                    <span className="truncate">{vehicle.currentLocation?.locationName}</span>
                  </span>
                  <span>{vehicle.mileage?.toLocaleString()} km</span>
                </div>
              </div>
              <div className="text-right flex-shrink-0">
                <p className="text-lg font-bold text-green-600">
                  ${vehicle.dailyRentalRate}/gün
                </p>
                <p className="text-sm text-gray-600">Temel ücret</p>
              </div>
            </div>
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Left Column */}
            <div className="space-y-4">
              {/* Date Selection */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    <Calendar className="w-4 h-4 inline mr-1" />
                    Alış Tarihi
                  </label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    min={new Date().toISOString().split('T')[0]}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                  />
                  <input
                    type="time"
                    value={pickupTime}
                    onChange={(e) => setPickupTime(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent mt-1 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    <Calendar className="w-4 h-4 inline mr-1" />
                    İade Tarihi
                  </label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    min={startDate || new Date().toISOString().split('T')[0]}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                  />
                  <input
                    type="time"
                    value={returnTime}
                    onChange={(e) => setReturnTime(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent mt-1 text-sm"
                  />
                </div>
              </div>

              {/* Location Selection */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    <MapPin className="w-4 h-4 inline mr-1" />
                    Alış Lokasyonu
                  </label>
                  <input
                    type="text"
                    value={pickupLocation}
                    onChange={(e) => setPickupLocation(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                    placeholder="Alış lokasyonu"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    <MapPin className="w-4 h-4 inline mr-1" />
                    İade Lokasyonu
                  </label>
                  <input
                    type="text"
                    value={returnLocation}
                    onChange={(e) => setReturnLocation(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                    placeholder="İade lokasyonu"
                  />
                </div>
              </div>

              {/* Notes */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Notlar (Opsiyonel)
                </label>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  rows={2}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                  placeholder="Özel isteklerinizi buraya yazabilirsiniz..."
                />
              </div>
            </div>

            {/* Right Column */}
            <div className="space-y-4">
              {/* Pricing Breakdown */}
              {pricingBreakdown && (
                <div className="bg-green-50 rounded-lg p-4">
                  <h3 className="font-semibold text-gray-900 mb-3 flex items-center">
                    <DollarSign className="w-5 h-5 mr-2 text-green-600" />
                    Fiyat Detayları
                  </h3>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span>Temel ücret ({pricingBreakdown.totalDays || 0} gün)</span>
                      <span>${(pricingBreakdown.basePrice || 0).toFixed(2)}</span>
                    </div>
                    {(pricingBreakdown.weekendSurcharge || 0) > 0 && (
                      <div className="flex justify-between text-orange-600">
                        <span>Hafta sonu ek ücreti</span>
                        <span>+${(pricingBreakdown.weekendSurcharge || 0).toFixed(2)}</span>
                      </div>
                    )}
                    {(pricingBreakdown.seasonalAdjustment || 0) !== 0 && (
                      <div className={`flex justify-between ${(pricingBreakdown.seasonalAdjustment || 0) > 0 ? 'text-orange-600' : 'text-green-600'}`}>
                        <span>Sezonsal ayarlama</span>
                        <span>{(pricingBreakdown.seasonalAdjustment || 0) > 0 ? '+' : ''}${(pricingBreakdown.seasonalAdjustment || 0).toFixed(2)}</span>
                      </div>
                    )}
                    {(pricingBreakdown.discountAmount || 0) > 0 && (
                      <div className="flex justify-between text-green-600">
                        <span>İndirim</span>
                        <span>-${(pricingBreakdown.discountAmount || 0).toFixed(2)}</span>
                      </div>
                    )}
                    <div className="flex justify-between">
                      <span>Vergi (%15)</span>
                      <span>${(pricingBreakdown.taxAmount || 0).toFixed(2)}</span>
                    </div>
                    <div className="border-t border-green-200 pt-2 flex justify-between font-semibold text-lg">
                      <span>Toplam</span>
                      <span className="text-green-600">${(pricingBreakdown.totalPrice || 0).toFixed(2)}</span>
                    </div>
                    <div className="text-center text-gray-600 text-xs">
                      Ortalama: ${(pricingBreakdown.averageRate || 0).toFixed(2)}/gün
                    </div>
                  </div>
                </div>
              )}

              {/* Customer Info */}
              <div className="bg-blue-50 rounded-lg p-4">
                <h3 className="font-semibold text-gray-900 mb-3 flex items-center">
                  <User className="w-5 h-5 mr-2 text-blue-600" />
                  Müşteri Bilgileri
                </h3>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Ad Soyad:</span>
                    <span className="font-medium">{user?.firstName} {user?.lastName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">E-posta:</span>
                    <span className="font-medium text-xs">{user?.email}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Telefon:</span>
                    <span className="font-medium">{user?.phoneNumber || 'Belirtilmemiş'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Şehir:</span>
                    <span className="font-medium">{user?.cityName || 'Belirtilmemiş'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex flex-col sm:flex-row items-center justify-between p-4 sm:p-6 border-t border-gray-200 bg-gray-50 gap-3">
          <div className="text-sm text-gray-600 flex items-center">
            <Clock className="w-4 h-4 inline mr-1" />
            Kiralama süresi: {pricingBreakdown?.totalDays || 0} gün
          </div>
          <div className="flex space-x-3 w-full sm:w-auto">
            <button
              onClick={onClose}
              className="flex-1 sm:flex-none px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm"
            >
              İptal
            </button>
            <button
              onClick={handleConfirm}
              disabled={!isValidDates() || isCalculating || !pricingBreakdown}
              className="flex-1 sm:flex-none px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors flex items-center justify-center space-x-2 text-sm"
            >
              <CreditCard className="w-4 h-4" />
              <span>
                {isCalculating ? 'Hesaplanıyor...' : `Kirala - $${(pricingBreakdown?.totalPrice || 0).toFixed(2)}`}
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RentalModal; 