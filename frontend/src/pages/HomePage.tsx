import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Calendar, Shield, Award, Users, Star } from 'lucide-react';
import { vehicleService, locationService } from '../services/api';
import { Vehicle, Location } from '../types';

const HomePage: React.FC = () => {
  const [searchLocation, setSearchLocation] = useState('');
  const [searchStartDate, setSearchStartDate] = useState('');
  const [searchEndDate, setSearchEndDate] = useState('');
  const [locations, setLocations] = useState<Location[]>([]);
  const [featuredVehicles, setFeaturedVehicles] = useState<Vehicle[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [locationsData, vehiclesData] = await Promise.all([
          locationService.getAllLocations(),
          vehicleService.getAvailableVehicles()
        ]);
        setLocations(Array.isArray(locationsData) ? locationsData : []);
        setFeaturedVehicles(Array.isArray(vehiclesData) ? vehiclesData.slice(0, 6) : []);
      } catch (error) {
        console.error('Veri yükleme hatası:', error);
        setLocations([
          {
            locationId: 1,
            locationName: 'İstanbul Merkez',
            address: 'Taksim, İstanbul',
            city: { cityId: 1, cityName: 'İstanbul', country: { countryId: 1, countryName: 'Türkiye', countryCode: 'TR' } }
          },
          {
            locationId: 2,
            locationName: 'Ankara Merkez',
            address: 'Kızılay, Ankara',
            city: { cityId: 2, cityName: 'Ankara', country: { countryId: 1, countryName: 'Türkiye', countryCode: 'TR' } }
          },
          {
            locationId: 3,
            locationName: 'İzmir Merkez',
            address: 'Konak, İzmir',
            city: { cityId: 3, cityName: 'İzmir', country: { countryId: 1, countryName: 'Türkiye', countryCode: 'TR' } }
          }
        ]);
        setFeaturedVehicles([]);
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  const handleSearch = () => {
    const params = new URLSearchParams();
    if (searchLocation) params.append('locationId', searchLocation);
    if (searchStartDate) params.append('startDate', searchStartDate);
    if (searchEndDate) params.append('endDate', searchEndDate);
    
    window.location.href = `/vehicles?${params.toString()}`;
  };

  const parseImageUrls = (imageUrls: string): string[] => {
    try {
      return JSON.parse(imageUrls || '[]');
    } catch {
      return [];
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700 flex justify-center items-center">
        <div className="text-center">
          <div className="loading-spinner mx-auto mb-4"></div>
          <p className="text-green-200">Yükleniyor...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-900 via-green-800 to-green-700">
      {/* Hero Section */}
      <section className="relative py-20 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-green-900 via-green-800 to-green-900 opacity-90"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12 animate-fade-in-up">
            <h1 className="text-6xl md:text-7xl font-bold mb-6">
              <span className="text-green-300">Hayalinizdeki Araç</span>
              <br />
              <span className="text-white">Sizi Bekliyor</span>
            </h1>
            <p className="text-xl text-green-100 max-w-2xl mx-auto">
              Güvenilir, konforlu ve uygun fiyatlı araç kiralama hizmetleri
            </p>
          </div>

          {/* Search Form */}
          <div className="card max-w-5xl mx-auto p-8 animate-slide-in-right">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="form-group">
                <label className="form-label">
                  Konum
                </label>
                <select
                  value={searchLocation}
                  onChange={(e) => setSearchLocation(e.target.value)}
                  className="select-field"
                >
                  <option value="">Konum Seçin</option>
                  {locations.map((location) => (
                    <option key={location.locationId} value={location.locationId.toString()}>
                      {location.locationName}
                    </option>
                  ))}
                </select>
              </div>
              
              <div className="form-group">
                <label className="form-label">
                  Alış Tarihi
                </label>
                <input
                  type="date"
                  value={searchStartDate}
                  onChange={(e) => setSearchStartDate(e.target.value)}
                  className="input-field"
                  min={new Date().toISOString().split('T')[0]}
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">
                  Teslim Tarihi
                </label>
                <input
                  type="date"
                  value={searchEndDate}
                  onChange={(e) => setSearchEndDate(e.target.value)}
                  className="input-field"
                  min={searchStartDate || new Date().toISOString().split('T')[0]}
                />
              </div>
              
              <div className="flex items-end">
                <button
                  onClick={handleSearch}
                  className="btn-primary w-full flex items-center justify-center space-x-2"
                >
                  <Search size={20} />
                  <span>Ara</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Vehicles - Moved up */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16 animate-fade-in-up">
            <h2 className="text-green-300 text-4xl font-bold mb-4">
              Öne Çıkan Araçlar
            </h2>
            <p className="text-green-100 text-lg">
              En popüler araçlarımızı keşfedin
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {featuredVehicles.map((vehicle) => {
              const imageUrls = parseImageUrls(vehicle.imageUrls || '[]');
              const firstImage = imageUrls.length > 0 ? imageUrls[0] : null;
              
              return (
                <div key={vehicle.vehicleId} className="vehicle-card group bg-gradient-to-br from-white to-green-50 border-green-400">
                  <div className="h-48 mb-6 rounded-lg overflow-hidden bg-gradient-to-br from-green-100 to-green-200 flex items-center justify-center">
                    {firstImage ? (
                      <img 
                        src={firstImage} 
                        alt={`${vehicle.model.brand.brandName} ${vehicle.model.modelName}`}
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          const target = e.target as HTMLImageElement;
                          target.style.display = 'none';
                          target.parentElement!.innerHTML = '<span class="text-green-600">Araç Fotoğrafı</span>';
                        }}
                      />
                    ) : (
                      <span className="text-green-600">Araç Fotoğrafı</span>
                    )}
                  </div>
                  <h3 className="text-xl font-semibold text-green-800 mb-2">
                    {vehicle.model.brand.brandName} {vehicle.model.modelName}
                  </h3>
                  <p className="text-green-600 mb-4">
                    {vehicle.model.year} • {vehicle.model.fuelType} • {vehicle.model.transmission}
                  </p>
                  <div className="flex items-center justify-between mb-6">
                    <div className="vehicle-price text-green-700">
                      ${vehicle.dailyRentalRate}/gün
                    </div>
                    <div className="flex items-center space-x-1">
                      <Star className="text-green-500 fill-current" size={16} />
                      <span className="text-sm text-green-600">4.8</span>
                    </div>
                  </div>
                  <Link
                    to={`/vehicles/${vehicle.vehicleId}`}
                    className="btn-primary w-full block text-center"
                  >
                    Detayları Gör
                  </Link>
                </div>
              );
            })}
          </div>

          <div className="text-center mt-12">
            <Link to="/vehicles" className="btn-secondary">
              Tüm Araçları Gör
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section - Moved down */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16 animate-fade-in-up">
            <h2 className="text-green-300 text-4xl font-bold mb-4">
              Neden Rentello?
            </h2>
            <p className="text-green-100 text-lg">
              Size en iyi hizmeti sunmak için buradayız
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="stats-card text-center group bg-gradient-to-br from-green-800 to-green-700 border-green-600">
              <div className="stats-icon mx-auto mb-6 group-hover:animate-pulse-glow">
                <Shield size={32} />
              </div>
              <h3 className="text-xl font-semibold text-white mb-4">Güvenli</h3>
              <p className="text-green-100">
                Tüm araçlarımız düzenli bakımdan geçer ve sigortalıdır
              </p>
            </div>

            <div className="stats-card text-center group bg-gradient-to-br from-green-800 to-green-700 border-green-600">
              <div className="stats-icon mx-auto mb-6 group-hover:animate-pulse-glow">
                <Award size={32} />
              </div>
              <h3 className="text-xl font-semibold text-white mb-4">Kaliteli</h3>
              <p className="text-green-100">
                En yeni model araçlar ve üstün kalite garantisi
              </p>
            </div>

            <div className="stats-card text-center group bg-gradient-to-br from-green-800 to-green-700 border-green-600">
              <div className="stats-icon mx-auto mb-6 group-hover:animate-pulse-glow">
                <Users size={32} />
              </div>
              <h3 className="text-xl font-semibold text-white mb-4">Müşteri Odaklı</h3>
              <p className="text-green-100">
                7/24 müşteri desteği ve kolay rezervasyon
              </p>
            </div>

            <div className="stats-card text-center group bg-gradient-to-br from-green-800 to-green-700 border-green-600">
              <div className="stats-icon mx-auto mb-6 group-hover:animate-pulse-glow">
                <Calendar size={32} />
              </div>
              <h3 className="text-xl font-semibold text-white mb-4">Esnek</h3>
              <p className="text-green-100">
                Günlük, haftalık veya aylık kiralama seçenekleri
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-green-600/20 to-green-500/20"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-green-300 text-4xl font-bold mb-4">
            Hemen Rezervasyon Yap
          </h2>
          <p className="text-xl text-green-100 mb-8 max-w-2xl mx-auto">
            Hayalindeki araçla yolculuğa çıkmaya hazır mısın?
          </p>
          <Link to="/vehicles" className="btn-primary text-lg px-8 py-4">
            Araç Kirala
          </Link>
        </div>
      </section>
    </div>
  );
};

export default HomePage; 