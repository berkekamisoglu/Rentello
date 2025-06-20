// User types
export interface User {
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
  createdDate?: string;
  updatedDate?: string;
  userRole?: UserRole; // Optional for backward compatibility
  role?: string; // Role as string from backend
  city?: City;
  cityName?: string; // City name as string from backend
}

export interface UserRole {
  roleId: number;
  roleName: string;
  description?: string;
}

export interface City {
  cityId: number;
  cityName: string;
  country: Country;
}

export interface Country {
  countryId: number;
  countryName: string;
  countryCode: string;
}

// Vehicle types
export interface Vehicle {
  vehicleId: number;
  vehicleRegistration: string;
  color?: string;
  mileage: number;
  purchaseDate?: string;
  purchasePrice?: number;
  dailyRentalRate: number;
  insurancePolicyNumber?: string;
  nextMaintenanceDate?: string;
  vehicleDescription?: string;
  imageUrls?: string; // JSON string containing array of image URLs
  createdDate: string;
  updatedDate?: string;
  model: VehicleModel;
  currentStatus: VehicleStatus;
  currentLocation: Location;
}

export interface VehicleModel {
  modelId: number;
  modelName: string;
  year: number;
  engineSize?: string;
  fuelType?: string;
  transmission?: string;
  seatingCapacity: number;
  brand: VehicleBrand;
  category: VehicleCategory;
}

export interface VehicleBrand {
  brandId: number;
  brandName: string;
  country?: Country;
}

export interface VehicleCategory {
  categoryId: number;
  categoryName: string;
  description?: string;
}

export interface VehicleStatus {
  statusId: number;
  statusName: string;
  isAvailableForRent: boolean;
  description?: string;
}

export interface Location {
  locationId: number;
  locationName: string;
  address: string;
  phoneNumber?: string;
  email?: string;
  isActive?: boolean;
  city: City;
}

// Rental types
export interface Rental {
  rentalId: number;
  startDate: string;
  endDate: string;
  actualReturnDate?: string;
  totalCost: number;
  totalDays: number;
  notes?: string;
  createdDate: string;
  updatedDate?: string;
  customer: User;
  vehicle: Vehicle;
  pickupLocation: Location;
  returnLocation: Location;
  rentalStatus: RentalStatus;
  payments?: Payment[];
}

export interface RentalStatus {
  statusId: number;
  statusName: string;
  description?: string;
}

// Payment types
export interface Payment {
  paymentId: number;
  amount: number;
  paymentDate: string;
  notes?: string;
  rental: Rental;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
}

export interface PaymentMethod {
  methodId: number;
  methodName: string;
  description?: string;
}

export interface PaymentStatus {
  statusId: number;
  statusName: string;
  description?: string;
}

// Auth types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  address?: string;
  cityId?: number;
}

// API Response types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface SearchFilters {
  categoryId?: number;
  brandId?: number;
  locationId?: number;
  minPrice?: number;
  maxPrice?: number;
  startDate?: string;
  endDate?: string;
  searchTerm?: string;
} 