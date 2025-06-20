import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { Role } from '../types/admin';

interface UserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (userData: any) => void;
  roles: Role[];
}

const UserModal: React.FC<UserModalProps> = ({ isOpen, onClose, onSave, roles }) => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    address: '',
    dateOfBirth: '',
    roleId: ''
  });

  const [errors, setErrors] = useState<any>({});

  useEffect(() => {
    if (isOpen) {
      setFormData({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        phoneNumber: '',
        address: '',
        dateOfBirth: '',
        roleId: ''
      });
      setErrors({});
    }
  }, [isOpen]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors((prev: any) => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors: any = {};

    if (!formData.username.trim()) {
      newErrors.username = 'Kullanıcı adı gerekli';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'E-posta gerekli';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Geçerli bir e-posta adresi girin';
    }

    if (!formData.password.trim()) {
      newErrors.password = 'Şifre gerekli';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Şifre en az 6 karakter olmalı';
    }

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Ad gerekli';
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Soyad gerekli';
    }

    if (!formData.roleId) {
      newErrors.roleId = 'Rol seçimi gerekli';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    const userData = {
      ...formData,
      roleId: parseInt(formData.roleId),
      dateOfBirth: formData.dateOfBirth || null
    };

    onSave(userData);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="flex justify-between items-center mb-6">
          <h2 className="gradient-text text-2xl font-bold">Yeni Kullanıcı Ekle</h2>
          <button
            onClick={onClose}
            className="action-delete"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="form-group">
            <label className="form-label">
              Kullanıcı Adı *
            </label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              className={`input-field ${
                errors.username ? 'border-red-500' : ''
              }`}
            />
            {errors.username && <p className="form-error">{errors.username}</p>}
          </div>

          <div className="form-group">
            <label className="form-label">
              E-posta *
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              className={`input-field ${
                errors.email ? 'border-red-500' : ''
              }`}
            />
            {errors.email && <p className="form-error">{errors.email}</p>}
          </div>

          <div className="form-group">
            <label className="form-label">
              Şifre *
            </label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              className={`input-field ${
                errors.password ? 'border-red-500' : ''
              }`}
            />
            {errors.password && <p className="form-error">{errors.password}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">
                Ad *
              </label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                className={`input-field ${
                  errors.firstName ? 'border-red-500' : ''
                }`}
              />
              {errors.firstName && <p className="form-error">{errors.firstName}</p>}
            </div>

            <div className="form-group">
              <label className="form-label">
                Soyad *
              </label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                className={`input-field ${
                  errors.lastName ? 'border-red-500' : ''
                }`}
              />
              {errors.lastName && <p className="form-error">{errors.lastName}</p>}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">
              Telefon
            </label>
            <input
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleInputChange}
              className="input-field"
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              Doğum Tarihi
            </label>
            <input
              type="date"
              name="dateOfBirth"
              value={formData.dateOfBirth}
              onChange={handleInputChange}
              className="input-field"
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              Adres
            </label>
            <input
              type="text"
              name="address"
              value={formData.address}
              onChange={handleInputChange}
              className="input-field"
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              Rol *
            </label>
            <select
              name="roleId"
              value={formData.roleId}
              onChange={handleInputChange}
              className={`select-field ${
                errors.roleId ? 'border-red-500' : ''
              }`}
            >
              <option value="">Rol seçin</option>
              {roles.map((role) => (
                <option key={role.roleId} value={role.roleId}>
                  {role.roleName}
                </option>
              ))}
            </select>
            {errors.roleId && <p className="form-error">{errors.roleId}</p>}
          </div>

          <div className="flex justify-end space-x-4 pt-6">
            <button
              type="button"
              onClick={onClose}
              className="btn-secondary"
            >
              İptal
            </button>
            <button
              type="submit"
              className="btn-primary"
            >
              Kullanıcı Oluştur
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserModal; 