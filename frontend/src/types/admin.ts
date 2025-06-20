export interface DashboardStats {
  // Genel İstatistikler
  totalUsers: number;
  totalVehicles: number;
  totalRentals: number;
  activeRentals: number;
  
  // Gelir İstatistikleri
  totalRevenue: number;
  monthlyRevenue: number;
  dailyRevenue: number;
  
  // Araç İstatistikleri
  availableVehicles: number;
  rentedVehicles: number;
  maintenanceVehicles: number;
  outOfServiceVehicles: number;
  
  // Son 7 Günlük İstatistikler
  dailyRentals: { [key: string]: number };
  dailyRevenues: { [key: string]: number };
  
  // Popüler Araç Kategorileri
  popularCategories: { [key: string]: number };
  
  // Müşteri İstatistikleri
  newCustomersThisMonth: number;
  activeCustomers: number;
  
  // Lokasyon İstatistikleri
  totalLocations: number;
  activeLocations: number;
}

export interface AdminUser {
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  address?: string;
  isActive: boolean;
  lastLoginDate?: string;
  createdDate: string;
  updatedDate?: string;
  
  // Role bilgileri
  roleId: number;
  roleName: string;
  
  // City bilgileri
  cityId?: number;
  cityName?: string;
  
  // İstatistikler
  totalRentals: number;
  activeRentals: number;
}

export interface AdminVehicle {
  vehicleId: number;
  licensePlate: string;
  vin: string;
  color: string;
  year: number;
  dailyRate: number;
  mileage: number;
  fuelType: string;
  transmissionType: string;
  capacity: number;
  isActive: boolean;
  features?: string;
  description?: string;
  imageUrls?: string | string[]; // URL dizisi veya JSON string
  createdDate: string;
  updatedDate?: string;
  
  // Brand ve Model bilgileri
  brandId: number;
  brandName: string;
  modelId: number;
  modelName: string;
  
  // Category bilgileri
  categoryId: number;
  categoryName: string;
  
  // Location bilgileri
  locationId: number;
  locationName: string;
  
  // Status bilgileri
  statusId: number;
  statusName: string;
  
  // İstatistikler
  totalRentals: number;
  activeRentals: number;
  totalRevenue: number;
  maintenanceCount: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ApiResponse {
  success: boolean;
  message: string;
}

export interface Location {
  locationId: number;
  locationName: string;
  address: string;
  city: string;
  phoneNumber: string;
  isActive: boolean;
}

export interface VehicleModel {
  modelId: number;
  modelName: string;
  manufactureYear: number;
  brand: {
    brandId: number;
    brandName: string;
  };
}

export interface VehicleStatus {
  statusId: number;
  statusName: string;
}

export interface VehicleReferences {
  models: VehicleModel[];
  locations: Location[];
  statuses: VehicleStatus[];
}

export interface AdminRental {
  rentalId: number;
  startDate: string;
  endDate: string;
  actualReturnDate?: string;
  totalCost: number;
  totalDays: number;
  notes?: string;
  createdDate: string;
  updatedDate?: string;
  
  // Customer information
  customerId: number;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  
  // Vehicle information
  vehicleId: number;
  vehiclePlate: string;
  vehicleBrand: string;
  vehicleModel: string;
  vehicleYear: number;
  
  // Location information
  pickupLocationId: number;
  pickupLocationName: string;
  returnLocationId: number;
  returnLocationName: string;
  
  // Status information
  statusId: number;
  statusName: string;
  
  // Payment information
  totalPaid: number;
  remainingAmount: number;
  paymentStatus: string;
}

export interface Role {
  roleId: number;
  roleName: string;
} 