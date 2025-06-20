import axios from 'axios';

// Use the main api instance with token interceptor
const api = axios.create({
  baseURL: 'http://localhost:8080/api/admin',
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

// Dashboard API'ları
export const getDashboardStats = async () => {
  const response = await api.get('/dashboard/stats');
  return response.data;
};

// Kullanıcı Yönetimi API'ları
export const getAllUsers = async (page = 0, size = 10, sortBy = 'createdDate', sortDirection = 'desc') => {
  const response = await api.get('/users', {
    params: { page, size, sortBy, sortDirection }
  });
  return response.data;
};

export const searchUsers = async (searchTerm: string, page = 0, size = 10) => {
  const response = await api.get('/users/search', {
    params: { searchTerm, page, size }
  });
  return response.data;
};

export const toggleUserStatus = async (userId: number) => {
  const response = await api.put(`/users/${userId}/toggle-status`);
  return response.data;
};

export const updateUserRole = async (userId: number, roleId: number) => {
  const response = await api.put(`/users/${userId}/role/${roleId}`);
  return response.data;
};

export const resetUserPassword = async (userId: number, newPassword: string) => {
  const response = await api.put(`/users/${userId}/password`, { newPassword });
  return response.data;
};

export const deleteUser = async (userId: number) => {
  const response = await api.delete(`/users/${userId}`);
  return response.data;
};

export const createUser = async (userData: any) => {
  const response = await api.post('/users', userData);
  return response.data;
};

export const getAllRoles = async () => {
  const response = await api.get('/roles');
  return response.data;
};

// Araç Yönetimi API'ları
export const getAllVehicles = async (page = 0, size = 10, sortBy = 'createdDate', sortDirection = 'desc') => {
  const response = await api.get('/vehicles', {
    params: { page, size, sortBy, sortDirection }
  });
  return response.data;
};

export const searchVehicles = async (searchTerm: string, page = 0, size = 10) => {
  const response = await api.get('/vehicles/search', {
    params: { searchTerm, page, size }
  });
  return response.data;
};

export const toggleVehicleStatus = async (vehicleId: number) => {
  const response = await api.put(`/vehicles/${vehicleId}/toggle-status`);
  return response.data;
};

export const createVehicle = async (vehicleData: any) => {
  const response = await api.post('/vehicles', vehicleData);
  return response.data;
};

export const updateVehicle = async (vehicleId: number, vehicleData: any) => {
  const response = await api.put(`/vehicles/${vehicleId}`, vehicleData);
  return response.data;
};

export const deleteVehicle = async (vehicleId: number) => {
  const response = await api.delete(`/vehicles/${vehicleId}`);
  return response.data;
};

export const getVehicleReferences = async () => {
  const response = await api.get('/vehicles/references');
  return response.data;
};

// Kiralama Yönetimi API'ları
export const getAllRentals = async (page = 0, size = 10, sortBy = 'createdDate', sortDirection = 'desc') => {
  const response = await api.get('/rentals', {
    params: { page, size, sortBy, sortDirection }
  });
  return response.data;
};

export const searchRentals = async (searchTerm: string, page = 0, size = 10) => {
  const response = await api.get('/rentals/search', {
    params: { searchTerm, page, size }
  });
  return response.data;
}; 