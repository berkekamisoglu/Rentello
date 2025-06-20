import axios from 'axios';

// Use axios instance with token interceptor
const api = axios.create({
  baseURL: 'http://localhost:8080/api/staff',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Araç Yönetimi API'ları
export const getAllVehiclesForStaff = async (page = 0, size = 10) => {
  const response = await api.get('/vehicles', {
    params: { page, size }
  });
  return response.data;
};

// Araç Resim Yönetimi API'ları
export const addVehicleImage = async (vehicleId: number, imageUrl: string) => {
  const response = await api.post(`/vehicles/${vehicleId}/images`, {
    imageUrl
  });
  return response.data;
};

export const getVehicleImages = async (vehicleId: number) => {
  const response = await api.get(`/vehicles/${vehicleId}/images`);
  return response.data;
};

export const removeVehicleImage = async (vehicleId: number, imageUrl: string) => {
  const response = await api.delete(`/vehicles/${vehicleId}/images`, {
    params: { imageUrl }
  });
  return response.data;
}; 