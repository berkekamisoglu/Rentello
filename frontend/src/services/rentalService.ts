import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
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

export interface CreateRentalRequest {
  userId?: number;
  vehicleId: number;
  plannedPickupDate: string;
  plannedReturnDate: string;
  totalAmount: number;
}

export interface Rental {
  rentalId: number;
  userId: number;
  vehicleId: number;
  plannedPickupDate: string;
  plannedReturnDate: string;
  actualPickupDate?: string;
  actualReturnDate?: string;
  totalAmount: number;
  baseAmount?: number;
  taxAmount?: number;
  discountAmount?: number;
  securityDeposit?: number;
  notes?: string;
  rentalStatus: {
    statusId: number;
    statusName: string;
    statusDescription?: string;
  };
  vehicle: {
    vehicleId: number;
    vehicleRegistration: string;
    model: {
      modelName: string;
      brand: {
        brandName: string;
      };
    };
  };
  pickupLocation: {
    locationId: number;
    locationName: string;
    address: string;
  };
  returnLocation: {
    locationId: number;
    locationName: string;
    address: string;
  };
  customer: {
    userId: number;
    firstName: string;
    lastName: string;
  };
  createdDate: string;
  updatedDate?: string;
}

class RentalService {
  async createRental(rentalData: CreateRentalRequest): Promise<Rental> {
    const response = await api.post('/rentals', rentalData);
    return response.data;
  }

  async getUserRentals(userId?: number): Promise<Rental[]> {
    const url = userId ? `/rentals/user/${userId}` : '/rentals/my-rentals';
    const response = await api.get(url);
    return response.data;
  }

  async getRentalById(rentalId: number): Promise<Rental> {
    const response = await api.get(`/rentals/${rentalId}`);
    return response.data;
  }

  async updateRentalStatus(rentalId: number, statusId: number): Promise<Rental> {
    const response = await api.put(`/rentals/${rentalId}/status`, { statusId });
    return response.data;
  }

  async processPayment(rentalId: number): Promise<Rental> {
    const response = await api.post(`/rentals/${rentalId}/payment`, {});
    return response.data;
  }

  async cancelRental(rentalId: number): Promise<void> {
    await api.post(`/rentals/${rentalId}/cancel`);
  }
}

const rentalServiceInstance = new RentalService();
export default rentalServiceInstance; 