import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { AdminVehicle } from '../types/admin';

interface VehicleModalProps {
  vehicle?: AdminVehicle | null;
  references: any;
  onSave: (vehicleData: any) => void;
  onClose: () => void;
  onReferencesRefresh?: () => void;
}

const VehicleModal: React.FC<VehicleModalProps> = ({ vehicle, references, onSave, onClose, onReferencesRefresh }) => {
  const [formData, setFormData] = useState({
    licensePlate: '',
    color: '',
    mileage: 0,
    dailyRate: 0,
    description: '',
    modelId: '',
    locationId: '',
    statusId: '',
    imageUrls: ['']
  });

  const [showNewBrandForm, setShowNewBrandForm] = useState(false);
  const [showNewModelForm, setShowNewModelForm] = useState(false);
  const [newBrand, setNewBrand] = useState({ brandName: '', brandCountry: '', website: '' });
  const [newModel, setNewModel] = useState({ 
    modelName: '', 
    brandId: '', 
    categoryId: '', 
    manufactureYear: new Date().getFullYear(),
    engineType: '',
    fuelType: '',
    transmissionType: '',
    seatingCapacity: 5
  });
  useEffect(() => {
    console.log('VehicleModal references:', references);
    if (vehicle) {
      const imageUrls = vehicle.imageUrls ? 
        (typeof vehicle.imageUrls === 'string' ? 
          JSON.parse(vehicle.imageUrls) : vehicle.imageUrls) : [''];
      
      setFormData({
        licensePlate: vehicle.licensePlate || '',
        color: vehicle.color || '',
        mileage: vehicle.mileage || 0,
        dailyRate: vehicle.dailyRate || 0,
        description: vehicle.description || '',
        modelId: vehicle.modelId?.toString() || '',
        locationId: vehicle.locationId?.toString() || '',
        statusId: vehicle.statusId?.toString() || '',
        imageUrls: imageUrls.length > 0 ? imageUrls : ['']
      });
    }
  }, [vehicle, references]);
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const vehicleData = {
      ...formData,
      mileage: Number(formData.mileage),
      dailyRate: Number(formData.dailyRate),
      modelId: Number(formData.modelId),
      locationId: Number(formData.locationId),
      statusId: Number(formData.statusId),
      imageUrls: JSON.stringify(formData.imageUrls.filter(url => url.trim() !== ''))
    };

    onSave(vehicleData);
  };
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleImageUrlChange = (index: number, value: string) => {
    const newImageUrls = [...formData.imageUrls];
    newImageUrls[index] = value;
    setFormData(prev => ({
      ...prev,
      imageUrls: newImageUrls
    }));
  };

  const addImageUrl = () => {
    setFormData(prev => ({
      ...prev,
      imageUrls: [...prev.imageUrls, '']
    }));
  };

  const removeImageUrl = (index: number) => {
    if (formData.imageUrls.length > 1) {
      const newImageUrls = formData.imageUrls.filter((_, i) => i !== index);
      setFormData(prev => ({
        ...prev,
        imageUrls: newImageUrls
      }));
    }
  };
  const createNewBrand = async () => {
    try {
      const response = await fetch('/api/reference/brands', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newBrand),
      });
        if (response.ok) {
        const savedBrand = await response.json();
        alert(`Yeni marka "${savedBrand.brandName}" başarıyla oluşturuldu!`);
        setShowNewBrandForm(false);
        setNewBrand({ brandName: '', brandCountry: '', website: '' });
        
        // References'ları yenile
        if (onReferencesRefresh) {
          onReferencesRefresh();
        }
      } else {
        alert('Marka oluşturulurken hata oluştu.');
      }
    } catch (error) {
      console.error('Marka oluşturma hatası:', error);
      alert('Marka oluşturulurken hata oluştu.');
    }
  };

  const createNewModel = async () => {
    try {
      const modelData = {
        ...newModel,
        brandId: Number(newModel.brandId),
        categoryId: Number(newModel.categoryId),
        manufactureYear: Number(newModel.manufactureYear),
        seatingCapacity: Number(newModel.seatingCapacity)
      };
        const response = await fetch('/api/reference/models', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(modelData),
      });
        if (response.ok) {
        const savedModel = await response.json();
        alert(`Yeni model "${savedModel.modelName}" başarıyla oluşturuldu!`);
        setShowNewModelForm(false);
        setNewModel({ 
          modelName: '', 
          brandId: '', 
          categoryId: '', 
          manufactureYear: new Date().getFullYear(),
          engineType: '',
          fuelType: '',
          transmissionType: '',
          seatingCapacity: 5
        });
        setFormData(prev => ({ ...prev, modelId: savedModel.modelId.toString() }));
        
        // References'ları yenile
        if (onReferencesRefresh) {
          onReferencesRefresh();
        }
      } else {
        alert('Model oluşturulurken hata oluştu.');
      }
    } catch (error) {
      console.error('Model oluşturma hatası:', error);
      alert('Model oluşturulurken hata oluştu.');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="flex justify-between items-center mb-6">
          <h2 className="gradient-text text-2xl font-bold">
            {vehicle ? 'Araç Düzenle' : 'Yeni Araç Ekle'}
          </h2>
          <button
            onClick={onClose}
            className="action-delete"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="form-group">
              <label className="form-label">
                Plaka
              </label>
              <input
                type="text"
                name="licensePlate"
                value={formData.licensePlate}
                onChange={handleChange}
                required
                className="input-field"
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Renk
              </label>
              <input
                type="text"
                name="color"
                value={formData.color}
                onChange={handleChange}
                className="input-field"
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Kilometre
              </label>
              <input
                type="number"
                name="mileage"
                value={formData.mileage}
                onChange={handleChange}
                min="0"
                className="input-field"
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Günlük Ücret ($)
              </label>
              <input
                type="number"
                name="dailyRate"
                value={formData.dailyRate}
                onChange={handleChange}
                min="0"
                step="0.01"
                required
                className="input-field"
              />
            </div>            <div className="form-group md:col-span-2">
              <label className="form-label">
                Model
              </label>
              <div className="flex space-x-2">
                <select
                  name="modelId"
                  value={formData.modelId}
                  onChange={handleChange}
                  required
                  className="select-field flex-1"
                >
                  <option value="">Model Seçin</option>
                  {references?.models?.map((model: any) => (
                    <option key={model.modelId} value={model.modelId}>
                      {model.brand?.brandName} {model.modelName} ({model.manufactureYear})
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  onClick={() => setShowNewModelForm(true)}
                  className="btn-secondary px-3 py-2 text-sm whitespace-nowrap"
                >
                  + Yeni Model
                </button>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Lokasyon
              </label>
              <select
                name="locationId"
                value={formData.locationId}
                onChange={handleChange}
                required
                className="select-field"
              >
                <option value="">Lokasyon Seçin</option>
                {references?.locations?.map((location: any) => (
                  <option key={location.locationId} value={location.locationId}>
                    {location.locationName}
                  </option>
                ))}
              </select>
            </div>            <div className="form-group">
              <label className="form-label">
                Durum
              </label>
              <select
                name="statusId"
                value={formData.statusId}
                onChange={handleChange}
                required
                className="select-field"
              >
                <option value="">Durum Seçin</option>
                {references?.statuses?.map((status: any) => (
                  <option key={status.statusId} value={status.statusId}>
                    {status.statusName}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">
              Araç Resimleri (URL)
            </label>
            <div className="space-y-3">
              {formData.imageUrls.map((url, index) => (
                <div key={index} className="flex items-center space-x-2">
                  <input
                    type="url"
                    value={url}
                    onChange={(e) => handleImageUrlChange(index, e.target.value)}
                    placeholder="https://example.com/image.jpg"
                    className="input-field flex-1"
                  />
                  {formData.imageUrls.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeImageUrl(index)}
                      className="btn-danger px-3 py-2 text-sm"
                    >
                      ×
                    </button>
                  )}
                </div>
              ))}
              <button
                type="button"
                onClick={addImageUrl}
                className="btn-secondary text-sm px-4 py-2"
              >
                + Resim Ekle
              </button>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">
              Açıklama
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={4}
              className="input-field resize-none"
              placeholder="Araç hakkında detaylı bilgi..."
            />
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
              {vehicle ? 'Güncelle' : 'Oluştur'}
            </button>          </div>
        </form>

        {/* New Model Form */}
        {showNewModelForm && (
          <div className="modal-overlay">
            <div className="modal-content">
              <div className="flex justify-between items-center mb-6">
                <h3 className="gradient-text text-xl font-bold">Yeni Model Ekle</h3>
                <button
                  onClick={() => setShowNewModelForm(false)}
                  className="action-delete"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>

              <div className="space-y-4">
                <div className="form-group">
                  <label className="form-label">Model Adı</label>
                  <input
                    type="text"
                    value={newModel.modelName}
                    onChange={(e) => setNewModel({...newModel, modelName: e.target.value})}
                    className="input-field"
                    required
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="form-group">
                    <label className="form-label">Marka</label>
                    <div className="flex space-x-2">
                      <select
                        value={newModel.brandId}
                        onChange={(e) => setNewModel({...newModel, brandId: e.target.value})}
                        className="select-field flex-1"
                        required
                      >
                        <option value="">Marka Seçin</option>
                        {references?.brands?.map((brand: any) => (
                          <option key={brand.brandId} value={brand.brandId}>
                            {brand.brandName}
                          </option>
                        ))}
                      </select>
                      <button
                        type="button"
                        onClick={() => setShowNewBrandForm(true)}
                        className="btn-secondary px-3 py-2 text-sm whitespace-nowrap"
                      >
                        + Yeni Marka
                      </button>
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Kategori</label>
                    <select
                      value={newModel.categoryId}
                      onChange={(e) => setNewModel({...newModel, categoryId: e.target.value})}
                      className="select-field"
                      required
                    >
                      <option value="">Kategori Seçin</option>
                      {references?.categories?.map((category: any) => (
                        <option key={category.categoryId} value={category.categoryId}>
                          {category.categoryName}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Üretim Yılı</label>
                    <input
                      type="number"
                      value={newModel.manufactureYear}
                      onChange={(e) => setNewModel({...newModel, manufactureYear: Number(e.target.value)})}
                      className="input-field"
                      min="1990"
                      max={new Date().getFullYear() + 1}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Yakıt Türü</label>
                    <select
                      value={newModel.fuelType}
                      onChange={(e) => setNewModel({...newModel, fuelType: e.target.value})}
                      className="select-field"
                    >
                      <option value="">Yakıt Türü Seçin</option>
                      <option value="Benzin">Benzin</option>
                      <option value="Dizel">Dizel</option>
                      <option value="Elektrik">Elektrik</option>
                      <option value="Hibrit">Hibrit</option>
                      <option value="LPG">LPG</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Vites Türü</label>
                    <select
                      value={newModel.transmissionType}
                      onChange={(e) => setNewModel({...newModel, transmissionType: e.target.value})}
                      className="select-field"
                    >
                      <option value="">Vites Türü Seçin</option>
                      <option value="Manuel">Manuel</option>
                      <option value="Otomatik">Otomatik</option>
                      <option value="Yarı Otomatik">Yarı Otomatik</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Koltuk Sayısı</label>
                    <input
                      type="number"
                      value={newModel.seatingCapacity}
                      onChange={(e) => setNewModel({...newModel, seatingCapacity: Number(e.target.value)})}
                      className="input-field"
                      min="2"
                      max="50"
                    />
                  </div>
                </div>

                <div className="flex justify-end space-x-4 pt-6">
                  <button
                    type="button"
                    onClick={() => setShowNewModelForm(false)}
                    className="btn-secondary"
                  >
                    İptal
                  </button>
                  <button
                    type="button"
                    onClick={createNewModel}
                    className="btn-primary"
                  >
                    Model Oluştur
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* New Brand Form */}
        {showNewBrandForm && (
          <div className="modal-overlay">
            <div className="modal-content">
              <div className="flex justify-between items-center mb-6">
                <h3 className="gradient-text text-xl font-bold">Yeni Marka Ekle</h3>
                <button
                  onClick={() => setShowNewBrandForm(false)}
                  className="action-delete"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>

              <div className="space-y-4">
                <div className="form-group">
                  <label className="form-label">Marka Adı</label>
                  <input
                    type="text"
                    value={newBrand.brandName}
                    onChange={(e) => setNewBrand({...newBrand, brandName: e.target.value})}
                    className="input-field"
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Ülke</label>
                  <input
                    type="text"
                    value={newBrand.brandCountry}
                    onChange={(e) => setNewBrand({...newBrand, brandCountry: e.target.value})}
                    className="input-field"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Website</label>
                  <input
                    type="url"
                    value={newBrand.website}
                    onChange={(e) => setNewBrand({...newBrand, website: e.target.value})}
                    className="input-field"
                  />
                </div>

                <div className="flex justify-end space-x-4 pt-6">
                  <button
                    type="button"
                    onClick={() => setShowNewBrandForm(false)}
                    className="btn-secondary"
                  >
                    İptal
                  </button>
                  <button
                    type="button"
                    onClick={createNewBrand}
                    className="btn-primary"
                  >
                    Marka Oluştur
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VehicleModal;