import axios, { AxiosInstance, AxiosResponse } from 'axios';
import {
  User,
  Vehicle,
  Rental,
  Payment,
  Location,
  VehicleBrand,
  VehicleCategory,
  VehicleModel,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  PaginatedResponse,
  SearchFilters,
} from '../types';

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth Services
export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response: AxiosResponse<LoginResponse> = await api.post('/auth/login', credentials);
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<void> => {
    await api.post('/auth/register', userData);
  },

  getProfile: async (): Promise<User> => {
    const response: AxiosResponse<User> = await api.get('/auth/profile');
    return response.data;
  },

  updateProfile: async (userId: number, profileData: Partial<User>): Promise<User> => {
    const response: AxiosResponse<User> = await api.put(`/users/${userId}`, profileData);
    return response.data;
  },

  changePassword: async (oldPassword: string, newPassword: string): Promise<void> => {
    const params = new URLSearchParams();
    params.append('oldPassword', oldPassword);
    params.append('newPassword', newPassword);
    await api.post('/auth/change-password', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },
};

// Vehicle Services
export const vehicleService = {
  getAllVehicles: async (page = 0, size = 10): Promise<PaginatedResponse<Vehicle>> => {
    const response: AxiosResponse<PaginatedResponse<Vehicle>> = await api.get(
      `/vehicles?page=${page}&size=${size}`
    );
    return response.data;
  },

  getVehicleById: async (id: number): Promise<Vehicle> => {
    const response: AxiosResponse<Vehicle> = await api.get(`/vehicles/${id}`);
    return response.data;
  },

  getAvailableVehicles: async (startDate?: string, endDate?: string): Promise<Vehicle[]> => {
    try {
      const params = new URLSearchParams();
      if (startDate) params.append('startDate', startDate);
      if (endDate) params.append('endDate', endDate);
      
      const response: AxiosResponse<Vehicle[]> = await api.get(`/vehicles/available?${params}`);
      return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
      console.error('Error fetching available vehicles:', error);
      return []; // Return empty array on error
    }
  },

  searchVehicles: async (filters: SearchFilters): Promise<Vehicle[]> => {
    const response: AxiosResponse<Vehicle[]> = await api.post('/vehicles/search', filters);
    return response.data;
  },

  createVehicle: async (vehicleData: Partial<Vehicle>): Promise<Vehicle> => {
    const response: AxiosResponse<Vehicle> = await api.post('/vehicles', vehicleData);
    return response.data;
  },

  updateVehicle: async (id: number, vehicleData: Partial<Vehicle>): Promise<Vehicle> => {
    const response: AxiosResponse<Vehicle> = await api.put(`/vehicles/${id}`, vehicleData);
    return response.data;
  },

  deleteVehicle: async (id: number): Promise<void> => {
    await api.delete(`/vehicles/${id}`);
  },
};

// Rental Services
export const rentalService = {
  getRentals: async (page = 0, size = 10): Promise<PaginatedResponse<Rental>> => {
    const response: AxiosResponse<PaginatedResponse<Rental>> = await api.get(
      `/rentals?page=${page}&size=${size}`
    );
    return response.data;
  },

  getRentalById: async (id: number): Promise<Rental> => {
    const response: AxiosResponse<Rental> = await api.get(`/rentals/${id}`);
    return response.data;
  },

  getUserRentals: async (userId: number): Promise<Rental[]> => {
    const response: AxiosResponse<Rental[]> = await api.get(`/rentals/user/${userId}`);
    return response.data;
  },

  createRental: async (rentalData: Partial<Rental>): Promise<Rental> => {
    const response: AxiosResponse<Rental> = await api.post('/rentals', rentalData);
    return response.data;
  },

  updateRental: async (id: number, rentalData: Partial<Rental>): Promise<Rental> => {
    const response: AxiosResponse<Rental> = await api.put(`/rentals/${id}`, rentalData);
    return response.data;
  },

  cancelRental: async (id: number): Promise<void> => {
    await api.post(`/rentals/${id}/cancel`);
  },

  completeRental: async (id: number): Promise<Rental> => {
    const response: AxiosResponse<Rental> = await api.post(`/rentals/${id}/complete`);
    return response.data;
  },
};

// Payment Services
export const paymentService = {
  getPaymentsByRental: async (rentalId: number): Promise<Payment[]> => {
    const response: AxiosResponse<Payment[]> = await api.get(`/payments/rental/${rentalId}`);
    return response.data;
  },

  createPayment: async (paymentData: Partial<Payment>): Promise<Payment> => {
    const response: AxiosResponse<Payment> = await api.post('/payments', paymentData);
    return response.data;
  },

  processPayment: async (paymentId: number): Promise<Payment> => {
    const response: AxiosResponse<Payment> = await api.post(`/payments/${paymentId}/process`);
    return response.data;
  },
};

// Location Services
export const locationService = {
  getAllLocations: async (): Promise<Location[]> => {
    try {
      const response: AxiosResponse<Location[]> = await api.get('/locations');
      return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
      console.error('Error fetching locations:', error);
      // Return fallback data when API fails
      return [
        {
          locationId: 1,
          locationName: 'İstanbul Merkez',
          address: 'Taksim Meydanı No:1, Beyoğlu, İstanbul',
          phoneNumber: '+90 212 555 0101',
          city: { 
            cityId: 1, 
            cityName: 'İstanbul', 
            country: { countryId: 1, countryName: 'Türkiye', countryCode: 'TR' } 
          }
        },
        {
          locationId: 2,
          locationName: 'Ankara Merkez',
          address: 'Kızılay Caddesi No:15, Çankaya, Ankara',
          phoneNumber: '+90 312 555 0201',
          city: { 
            cityId: 2, 
            cityName: 'Ankara', 
            country: { countryId: 1, countryName: 'Türkiye', countryCode: 'TR' } 
          }
        },
        {
          locationId: 3,
          locationName: 'İzmir Merkez',
          address: 'Kordon Caddesi No:25, Konak, İzmir',
          phoneNumber: '+90 232 555 0301',
          city: { 
            cityId: 3, 
            cityName: 'İzmir', 
            country: { countryId: 1, countryName: 'Türkiye', countryCode: 'TR' } 
          }
        }
      ];
    }
  },

  getLocationById: async (id: number): Promise<Location> => {
    try {
      const response: AxiosResponse<Location> = await api.get(`/locations/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching location ${id}:`, error);
      throw error;
    }
  },
};

// Reference Data Services
export const referenceService = {
  getBrands: async (): Promise<VehicleBrand[]> => {
    const response: AxiosResponse<VehicleBrand[]> = await api.get('/reference/brands');
    return response.data;
  },

  getCategories: async (): Promise<VehicleCategory[]> => {
    const response: AxiosResponse<VehicleCategory[]> = await api.get('/reference/categories');
    return response.data;
  },

  getModels: async (): Promise<VehicleModel[]> => {
    const response: AxiosResponse<VehicleModel[]> = await api.get('/reference/models');
    return response.data;
  },

  getModelsByBrand: async (brandId: number): Promise<VehicleModel[]> => {
    const response: AxiosResponse<VehicleModel[]> = await api.get(`/reference/models/brand/${brandId}`);
    return response.data;
  },
};

export default api; 