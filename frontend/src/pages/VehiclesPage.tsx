import React, { useState, useEffect } from 'react';
import { vehicleService, referenceService } from '../services/api';
import { Vehicle, VehicleBrand, VehicleCategory } from '../types';
import { Search, Filter, Car, Fuel, Users, Cog, MapPin, Calendar, DollarSign, Info } from 'lucide-react';
import rentalService from '../services/rentalService';
import pricingService, { PricingBreakdown } from '../services/pricingService';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import RentalModal from '../components/RentalModal';
import RentalSuccessModal from '../components/RentalSuccessModal';

const VehiclesPage: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [allVehicles, setAllVehicles] = useState<Vehicle[]>([]); // Store all vehicles for filtering
  const [brands, setBrands] = useState<VehicleBrand[]>([]);
  const [categories, setCategories] = useState<VehicleCategory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBrand, setSelectedBrand] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 200]);
  const [showFilters, setShowFilters] = useState(false);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [showPricingInfo, setShowPricingInfo] = useState(false);
  const [pricingBreakdowns, setPricingBreakdowns] = useState<{[key: number]: PricingBreakdown}>({});
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);
  const [showRentalModal, setShowRentalModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [rentalSuccessData, setRentalSuccessData] = useState<any>(null);

  useEffect(() => {
    loadData();
    // Set default dates (today and 7 days later)
    const today = new Date();
    const nextWeek = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000);
    setStartDate(today.toISOString().split('T')[0]);
    setEndDate(nextWeek.toISOString().split('T')[0]);
  }, []);

  useEffect(() => {
    // Calculate pricing for all vehicles when dates change
    if (startDate && endDate && vehicles.length > 0) {
      calculatePricingForAllVehicles();
    }
  }, [startDate, endDate, vehicles]);

  // Perform search when searchTerm changes
  useEffect(() => {
    if (searchTerm.trim()) {
      performSearch();
    } else {
      // Reset to all vehicles when search is cleared
      setVehicles(allVehicles);
    }
  }, [searchTerm, allVehicles]);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [allVehiclesData, availableVehiclesData, brandsData, categoriesData] = await Promise.all([
        vehicleService.getAllVehicles(0, 1000), // Tüm araçları al (sayfalama ile)
        vehicleService.getAvailableVehicles(), // Sadece müsait araçları al
        referenceService.getBrands(),
        referenceService.getCategories()
      ]);

      // PaginatedResponse'dan content'i çıkar
      const allVehiclesList: Vehicle[] = Array.isArray(allVehiclesData) 
        ? allVehiclesData 
        : allVehiclesData.content || [];
      
      // İstatistikler için tüm araçları kullan
      setAllVehicles(allVehiclesList);
      // Listeleme için müsait araçları kullan
      setVehicles(availableVehiclesData);
      setBrands(brandsData);
      setCategories(categoriesData);
      
      // Debug: Status isimlerini konsola yazdır (tüm araçlar için)
      console.log('🚗 All vehicles loaded:', allVehiclesList.length);
      console.log('✅ Available vehicles loaded:', availableVehiclesData.length);
      
      allVehiclesList.forEach((vehicle, index) => {
        if (index < 10) { // İlk 10 aracın status bilgilerini göster
          console.log(`Vehicle ${vehicle.vehicleId}: status="${vehicle.currentStatus?.statusName}", isAvailable=${vehicle.currentStatus?.isAvailableForRent}`);
        }
      });
      
      // Status dağılımını göster (tüm araçlar için)
      const statusCounts = allVehiclesList.reduce((acc: any, vehicle) => {
        const status = vehicle.currentStatus?.statusName || 'Unknown';
        acc[status] = (acc[status] || 0) + 1;
        return acc;
      }, {});
      console.log('📊 Status distribution (all vehicles):', statusCounts);
      
      // isAvailableForRent dağılımını göster
      const availabilityCount = allVehiclesList.reduce((acc: any, vehicle) => {
        const isAvailable = vehicle.currentStatus?.isAvailableForRent;
        const key = isAvailable === true ? 'Available (true)' : isAvailable === false ? 'Not Available (false)' : 'Unknown';
        acc[key] = (acc[key] || 0) + 1;
        return acc;
      }, {});
      console.log('🔍 Availability distribution:', availabilityCount);
      
      // Müsait olmayan araçların status'larını göster
      const notAvailableVehicles = allVehiclesList.filter(v => v.currentStatus?.isAvailableForRent === false);
      console.log('❌ Not available vehicles:', notAvailableVehicles.map(v => ({
        id: v.vehicleId,
        status: v.currentStatus?.statusName,
        isAvailable: v.currentStatus?.isAvailableForRent
      })));
      
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const performSearch = async () => {
    if (!searchTerm.trim()) {
      setVehicles(allVehicles);
      return;
    }

    try {
      // Use comprehensive search endpoint
      const response = await axios.get(
        'http://localhost:8080/api/database-integration/composite/search-vehicles-comprehensive',
        {
          params: {
            searchTerm: searchTerm,
            includeDescription: true,
            includeBrand: true,
            includeModel: true,
            includeCategory: true
          }
        }
      );

      if (response.data && Array.isArray(response.data)) {
        // Convert AvailableVehicle to Vehicle format
        const searchResults = response.data.map((av: any) => ({
          vehicleId: av.vehicleId,
          vehicleRegistration: av.vehicleRegistration,
          color: av.color,
          mileage: av.mileage,
          dailyRentalRate: av.dailyRentalRate,
          vehicleDescription: '', // Not available in AvailableVehicle
          imageUrls: '[]', // Empty array as default - will show placeholder
          createdDate: '',
          model: {
            modelId: 0,
            modelName: av.modelName,
            year: av.manufactureYear,
            seatingCapacity: av.seatingCapacity,
            brand: {
              brandId: 0,
              brandName: av.brandName
            },
            category: {
              categoryId: 0,
              categoryName: av.categoryName
            }
          },
          currentStatus: {
            statusId: 0,
            statusName: av.statusName,
            isAvailableForRent: av.statusName === 'Müsait' || av.statusName === 'Available' || av.statusName === 'Musait'
          },
          currentLocation: {
            locationId: 0,
            locationName: av.locationName,
            address: '',
            city: {
              cityId: 0,
              cityName: av.cityName,
              country: {
                countryId: 0,
                countryName: '',
                countryCode: ''
              }
            }
          }
        }));

        // Fetch image URLs for search results from Vehicle endpoint
        try {
          const vehicleIds = searchResults.map(v => v.vehicleId);
          
          const imagePromises = vehicleIds.map(async (vehicleId) => {
            try {
              // Get token for authentication
              const token = localStorage.getItem('token');
              const headers = token ? { Authorization: `Bearer ${token}` } : {};
              
              const vehicleResponse = await axios.get(`http://localhost:8080/api/vehicles/${vehicleId}`, {
                headers
              });
              
              return {
                vehicleId,
                imageUrls: vehicleResponse.data.imageUrls || '[]'
              };
            } catch (error) {
              console.error(`Error fetching images for vehicle ${vehicleId}:`, error);
              return { vehicleId, imageUrls: '[]' };
            }
          });

          const imageResults = await Promise.all(imagePromises);
          
          // Update search results with actual image URLs
          const searchResultsWithImages = searchResults.map(vehicle => {
            const imageData = imageResults.find(img => img.vehicleId === vehicle.vehicleId);
            return {
              ...vehicle,
              imageUrls: imageData?.imageUrls || '[]'
            };
          });

          setVehicles(searchResultsWithImages);
        } catch (error) {
          console.error('Error fetching vehicle images:', error);
          // Fallback to search results without images
          setVehicles(searchResults);
        }
      }
    } catch (error) {
      console.error('Search error:', error);
      // Fallback to client-side search
      const searchResults = allVehicles.filter(vehicle => {
        const searchLower = searchTerm.toLowerCase();
        return (
          vehicle.model?.modelName.toLowerCase().includes(searchLower) ||
          vehicle.model?.brand?.brandName.toLowerCase().includes(searchLower) ||
          vehicle.model?.category?.categoryName.toLowerCase().includes(searchLower) ||
          vehicle.vehicleRegistration.toLowerCase().includes(searchLower) ||
          (vehicle.vehicleDescription && vehicle.vehicleDescription.toLowerCase().includes(searchLower))
        );
      });
      setVehicles(searchResults);
    }
  };

  const calculatePricingForAllVehicles = async () => {
    if (!startDate || !endDate) return;
    
    const breakdowns: {[key: number]: PricingBreakdown} = {};
    
    for (const vehicle of vehicles) {
      try {
        const breakdown = await pricingService.getPricingBreakdown(
          vehicle.vehicleId, 
          startDate, 
          endDate
        );
        breakdowns[vehicle.vehicleId] = breakdown;
      } catch (error) {
        console.error(`Error calculating pricing for vehicle ${vehicle.vehicleId}:`, error);
      }
    }
    
    setPricingBreakdowns(breakdowns);
  };

  const handleRentVehicle = async (vehicle: Vehicle) => {
    if (!isAuthenticated || !user) {
      navigate('/login');
      return;
    }

    setSelectedVehicle(vehicle);
    setShowRentalModal(true);
  };

  const handleRentalConfirm = async (rentalData: {
    vehicleId: number;
    startDate: string;
    endDate: string;
    totalAmount: number;
    pickupLocation: string;
    returnLocation: string;
  }) => {
    try {
      // Check availability using database integration
      const availabilityResponse = await axios.get(
        `http://localhost:8080/api/database-integration/functions/is-vehicle-available`, {
          params: {
            vehicleId: rentalData.vehicleId,
            startDate: rentalData.startDate,
            endDate: rentalData.endDate
          }
        }
      );
      
      if (!availabilityResponse.data) {
        alert('Bu araç şu anda kiralama için müsait değil.');
        return;
      }

      // Create rental using stored procedure
      const requestData = {
        customerId: user.userId,
        vehicleId: rentalData.vehicleId,
        pickupLocationId: selectedVehicle?.currentLocation?.locationId || 1,
        returnLocationId: selectedVehicle?.currentLocation?.locationId || 1,
        plannedPickupDate: rentalData.startDate,
        plannedReturnDate: rentalData.endDate,
        createdBy: user.userId
      };

      const response = await axios.post(
        'http://localhost:8080/api/database-integration/stored-procedures/create-rental',
        requestData
      );

      if (response.data && (response.data.IsSuccess || response.data.RentalID)) {
        // Calculate days for success modal
        const startDate = new Date(rentalData.startDate);
        const endDate = new Date(rentalData.endDate);
        const totalDays = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

        // Prepare success modal data
        const successData = {
          rentalId: response.data.RentalID || response.data,
          vehicleName: `${selectedVehicle?.model?.brand?.brandName} ${selectedVehicle?.model?.modelName}`,
          vehicleRegistration: selectedVehicle?.vehicleRegistration || '',
          customerName: `${user.firstName} ${user.lastName}`,
          startDate: rentalData.startDate,
          endDate: rentalData.endDate,
          totalAmount: response.data.TotalAmount || rentalData.totalAmount,
          pickupLocation: rentalData.pickupLocation,
          returnLocation: rentalData.returnLocation,
          totalDays: totalDays
        };

        setRentalSuccessData(successData);
        setShowRentalModal(false);
        setShowSuccessModal(true);
        
        // Refresh vehicles list
        loadData();
      } else {
        alert(`❌ Kiralama işlemi başarısız: ${response.data?.ErrorMessage || 'Bilinmeyen hata'}`);
      }
    } catch (error) {
      console.error('Error renting vehicle:', error);
      if (axios.isAxiosError(error) && error.response?.status === 400) {
        alert('Bu araç zaten kiralanmış veya müsait değil.');
      } else {
        alert('Kiralama işlemi sırasında bir hata oluştu. Lütfen tekrar deneyin.');
      }
    }
  };

  const parseImageUrls = (imageUrls: string): string[] => {
    if (!imageUrls) return [];
    try {
      return JSON.parse(imageUrls);
    } catch {
      return imageUrls.split(',').map(url => url.trim()).filter(url => url);
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status.toLowerCase()) {
      case 'available':
      case 'müsait':
        return 'badge-success';
      case 'rented':
      case 'kiralanmış':
        return 'badge-warning';
      case 'maintenance':
      case 'bakımda':
        return 'badge-info';
      default:
        return 'badge-danger';
    }
  };

  // Apply client-side filters to vehicles (for brand, category, price)
  const filteredVehicles = vehicles.filter(vehicle => {
    const matchesBrand = !selectedBrand || vehicle.model?.brand?.brandId?.toString() === selectedBrand;
    const matchesCategory = !selectedCategory || vehicle.model?.category?.categoryId?.toString() === selectedCategory;
    const matchesPrice = vehicle.dailyRentalRate >= priceRange[0] && vehicle.dailyRentalRate <= priceRange[1];
    
    return matchesBrand && matchesCategory && matchesPrice;
  });

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700 flex items-center justify-center">
        <div className="text-center">
          <div className="loading-spinner mx-auto mb-4"></div>
          <p className="text-white">Araçlar yükleniyor...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8 animate-fade-in-up">
          <h1 className="text-green-300 text-5xl font-bold mb-2">Araç Filosu</h1>
          <p className="text-green-100 text-lg">Premium araçlarımızı keşfedin ve hemen kiralayın</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="stats-card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-100 text-sm">Toplam Araç</p>
                <p className="text-2xl font-bold text-white">{allVehicles.length}</p>
              </div>
              <div className="stats-icon">
                <Car className="h-5 w-5" />
              </div>
            </div>
          </div>
          
          <div className="stats-card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-100 text-sm">Müsait</p>
                <p className="text-2xl font-bold text-white">
                  {allVehicles.filter(v => 
                    v.currentStatus?.statusName === 'Müsait' || 
                    v.currentStatus?.statusName === 'Musait' ||
                    v.currentStatus?.isAvailableForRent === true
                  ).length}
                </p>
              </div>
              <div className="stats-icon">
                <Calendar className="h-5 w-5" />
              </div>
            </div>
          </div>
          
          <div className="stats-card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-100 text-sm">Kiralanmış</p>
                <p className="text-2xl font-bold text-white">
                  {allVehicles.filter(v => {
                    const status = v.currentStatus?.statusName?.toLowerCase() || '';
                    return (
                      status.includes('kira') ||
                      status.includes('rent') ||
                      status === 'aktif' ||
                      status === 'active' ||
                      status === 'rezerve' ||
                      status === 'reserved' ||
                      (v.currentStatus?.isAvailableForRent === false && 
                       !status.includes('bakım') && 
                       !status.includes('arıza') &&
                       !status.includes('maintenance') &&
                       !status.includes('repair'))
                    );
                  }).length}
                </p>
              </div>
              <div className="stats-icon">
                <Users className="h-5 w-5" />
              </div>
            </div>
          </div>
          
          <div className="stats-card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-100 text-sm">Ortalama Fiyat</p>
                <p className="text-2xl font-bold text-white">
                  ${allVehicles.length > 0 ? (allVehicles.reduce((sum, v) => sum + v.dailyRentalRate, 0) / allVehicles.length).toFixed(0) : '0'}/gün
                </p>
              </div>
              <div className="stats-icon">
                <DollarSign className="h-5 w-5" />
              </div>
            </div>
          </div>
        </div>

        {/* Search and Filters */}
        <div className="mb-8">
          {/* Date Selection */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-white mb-2">Başlangıç Tarihi</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="form-input w-full"
                min={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-white mb-2">Bitiş Tarihi</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="form-input w-full"
                min={startDate || new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="flex items-end">
              <button
                onClick={() => setShowPricingInfo(!showPricingInfo)}
                className="btn-secondary w-full flex items-center justify-center space-x-2"
              >
                <Info className="h-4 w-4" />
                <span>Fiyatlandırma Bilgisi</span>
              </button>
            </div>
          </div>

          {/* Pricing Info */}
          {showPricingInfo && (
            <div className="bg-gray-800 border border-gray-700 rounded-lg p-4 mb-4">
              <h3 className="text-lg font-semibold text-white mb-3">Dinamik Fiyatlandırma Sistemi</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <h4 className="font-medium text-green-300 mb-2">Mevsimsel Fiyatlandırma:</h4>
                  <ul className="space-y-1 text-gray-300">
                    <li>• Yaz (Haziran-Ağustos): +%30</li>
                    <li>• Kış (Aralık-Şubat): -%10</li>
                    <li>• İlkbahar/Sonbahar: +%10</li>
                  </ul>
                </div>
                <div>
                  <h4 className="font-medium text-green-300 mb-2">Özel Günler:</h4>
                  <ul className="space-y-1 text-gray-300">
                    <li>• Hafta sonu: +%20</li>
                    <li>• Resmi tatiller: +%50</li>
                    <li>• Yoğun dönemler: +%40</li>
                  </ul>
                </div>
              </div>
            </div>
          )}

          {/* Search Bar */}
          <div className="relative mb-4">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Araç ara (marka, model, plaka...)"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="form-input pl-10 w-full"
            />
          </div>

          {/* Filter Toggle */}
          <div className="flex justify-between items-center mb-4">
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="btn-secondary flex items-center space-x-2"
            >
              <Filter className="h-4 w-4" />
              <span>Filtreler</span>
            </button>
          </div>

          {/* Filters */}
          {showFilters && (
            <div className="filter-container p-6 rounded-lg grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium filter-label mb-2">Marka</label>
                <select
                  value={selectedBrand}
                  onChange={(e) => setSelectedBrand(e.target.value)}
                  className="filter-input w-full"
                >
                  <option value="">Tüm Markalar</option>
                  {brands.map(brand => (
                    <option key={brand.brandId} value={brand.brandId}>
                      {brand.brandName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium filter-label mb-2">Kategori</label>
                <select
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                  className="filter-input w-full"
                >
                  <option value="">Tüm Kategoriler</option>
                  {categories.map(category => (
                    <option key={category.categoryId} value={category.categoryId}>
                      {category.categoryName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium price-range-label mb-2">
                  Fiyat Aralığı (${priceRange[0]} - ${priceRange[1]})
                </label>
                <div className="flex space-x-2">
                  <input
                    type="range"
                    min="0"
                    max="200"
                    value={priceRange[0]}
                    onChange={(e) => setPriceRange([Number(e.target.value), priceRange[1]])}
                    className="flex-1 price-range-input"
                  />
                  <input
                    type="range"
                    min="0"
                    max="200"
                    value={priceRange[1]}
                    onChange={(e) => setPriceRange([priceRange[0], Number(e.target.value)])}
                    className="flex-1 price-range-input"
                  />
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Results Count */}
        <div className="mb-6">
          <p className="text-white">
            {filteredVehicles.length} araç bulundu
            {startDate && endDate && (
              <span className="ml-2 text-green-300">
                • {startDate} - {endDate} tarihleri için dinamik fiyatlandırma aktif
              </span>
            )}
          </p>
        </div>

        {/* Vehicle Grid */}
        {filteredVehicles.length === 0 ? (
                  <div className="text-center py-12">
          <Car className="mx-auto h-12 w-12 text-white mb-4" />
          <h3 className="text-lg font-medium text-white mb-2">Araç bulunamadı</h3>
          <p className="text-white">Arama kriterlerinizi değiştirmeyi deneyin.</p>
        </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredVehicles.map(vehicle => {
              const images = parseImageUrls(vehicle.imageUrls);
              
              return (
                <div key={vehicle.vehicleId} className="vehicle-card group">
                  {/* Image */}
                  <div className="relative h-48 mb-4 rounded-lg overflow-hidden">
                    {images.length > 0 ? (
                      <img
                        src={images[0]}
                        alt={`${vehicle.model?.brand?.brandName} ${vehicle.model?.modelName}`}
                        className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                        onError={(e) => {
                          const target = e.target as HTMLImageElement;
                          target.src = '/api/placeholder/400/300';
                        }}
                      />
                    ) : (
                      <div className="w-full h-full bg-gradient-to-br from-gray-700 to-gray-800 flex items-center justify-center">
                        <Car className="h-16 w-16 text-gray-500" />
                      </div>
                    )}
                    
                    {/* Status Badge */}
                    <div className="absolute top-3 right-3">
                      <span className={`badge ${getStatusBadgeClass(vehicle.currentStatus?.statusName)}`}>
                        {vehicle.currentStatus?.statusName}
                      </span>
                    </div>
                  </div>

                  {/* Content */}
                  <div className="space-y-4">
                    {/* Title */}
                    <div>
                      <h3 className="text-xl font-bold text-gray-800 mb-1">
                        {vehicle.model?.brand?.brandName} {vehicle.model?.modelName}
                      </h3>
                      <p className="text-gray-600 text-sm">{vehicle.model?.year} • {vehicle.vehicleRegistration}</p>
                    </div>

                    {/* Description */}
                    {vehicle.vehicleDescription && (
                      <p className="text-gray-700 text-sm line-clamp-2">
                        {vehicle.vehicleDescription}
                      </p>
                    )}

                    {/* Details */}
                    <div className="grid grid-cols-2 gap-4 text-sm">
                      <div className="flex items-center space-x-2 text-gray-600">
                        <MapPin className="h-4 w-4" />
                        <span>{vehicle.currentLocation?.locationName}</span>
                      </div>
                      <div className="flex items-center space-x-2 text-gray-600">
                        <Fuel className="h-4 w-4" />
                        <span>{vehicle.mileage.toLocaleString()} km</span>
                      </div>
                    </div>

                    {/* Price and Action */}
                    <div className="flex items-center justify-between pt-4 border-t border-gray-700">
                      <div>
                        {pricingBreakdowns[vehicle.vehicleId] ? (
                          <>
                            <p className="vehicle-price">
                              {pricingService.formatPrice(pricingBreakdowns[vehicle.vehicleId].totalPrice)}
                            </p>
                            <p className="text-gray-600 text-xs">
                              {pricingBreakdowns[vehicle.vehicleId].totalDays} gün toplam
                            </p>
                            <p className="text-green-600 text-xs">
                              Ort: {pricingService.formatPrice(pricingBreakdowns[vehicle.vehicleId].averageRate)}/gün
                            </p>
                          </>
                        ) : (
                          <>
                            <p className="vehicle-price">${vehicle.dailyRentalRate}/gün</p>
                            <p className="text-gray-600 text-xs">Temel ücret</p>
                          </>
                        )}
                      </div>
                      {isAuthenticated ? (
                        <button
                          onClick={() => handleRentVehicle(vehicle)}
                          className="btn-primary"
                          disabled={!vehicle.currentStatus?.isAvailableForRent}
                        >
                          {vehicle.currentStatus?.isAvailableForRent 
                            ? 'Kirala' 
                            : 'Müsait Değil'
                          }
                        </button>
                      ) : (
                        <button
                          onClick={() => navigate('/login')}
                          className="btn-primary"
                        >
                          Giriş Yap
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Rental Modal */}
      <RentalModal
        vehicle={selectedVehicle}
        isOpen={showRentalModal}
        onClose={() => {
          setShowRentalModal(false);
          setSelectedVehicle(null);
        }}
        onConfirm={handleRentalConfirm}
        user={user}
      />

      {/* Success Modal */}
      <RentalSuccessModal
        isOpen={showSuccessModal}
        onClose={() => {
          setShowSuccessModal(false);
          setRentalSuccessData(null);
        }}
        rentalData={rentalSuccessData}
      />
    </div>
  );
};

export default VehiclesPage; 