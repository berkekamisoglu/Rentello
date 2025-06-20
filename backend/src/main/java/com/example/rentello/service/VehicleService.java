package com.example.rentello.service;

import com.example.rentello.entity.Vehicle;
import com.example.rentello.repository.VehicleRepository;
import com.example.rentello.types.SearchFilters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    
    // Basic CRUD operations
    public Vehicle save(Vehicle vehicle) {
        if (vehicle.getVehicleId() == null && vehicleRepository.existsByVehicleRegistration(vehicle.getVehicleRegistration())) {
            throw new RuntimeException("Vehicle registration already exists");
        }
        return vehicleRepository.save(vehicle);
    }
    
    public Optional<Vehicle> findById(Integer vehicleId) {
        return vehicleRepository.findById(vehicleId);
    }
    
    public Vehicle getVehicleById(Integer vehicleId) {
        return vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
    }
    
    public void deleteById(Integer vehicleId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicleRepository.delete(vehicle);
    }
    
    // Paginated results
    public Page<Vehicle> getAllVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }
    
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    
    // Search and filter operations
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAvailableVehicles();
    }
    
    public List<Vehicle> getAvailableVehicles(LocalDate startDate, LocalDate endDate) {
        // This would filter vehicles available between dates
        // For now, return basic available vehicles
        return vehicleRepository.findAvailableVehicles();
    }
    
    public List<Vehicle> findAvailableVehicles(LocalDateTime startDate, LocalDateTime endDate, 
                                             Integer locationId, Integer categoryId, BigDecimal maxDailyRate) {
        // This would call a complex repository method or use stored procedure
        // For now, return basic available vehicles
        return vehicleRepository.findAvailableVehicles();
    }
    
    public List<Vehicle> searchVehiclesByDescription(String searchTerm, int maxResults) {
        return vehicleRepository.searchVehicles(searchTerm);
    }
    
    public List<Vehicle> searchVehicles(SearchFilters filters) {
        // Complex search with filters
        // For now, return basic search
        if (filters.getSearchTerm() != null) {
            return vehicleRepository.searchVehicles(filters.getSearchTerm());
        }
        return vehicleRepository.findAvailableVehicles();
    }
    
    public List<Vehicle> findByCategory(Integer categoryId) {
        return vehicleRepository.findByCategoryId(categoryId);
    }
    
    public List<Vehicle> findByLocation(Integer locationId) {
        return vehicleRepository.findByLocationId(locationId);
    }
    
    public Double calculateUtilizationRate(Integer vehicleId, int days) {
        // This would calculate utilization based on rental history
        // For now, return a mock value
        return 75.0;
    }
    
    // Business logic methods
    public Vehicle createVehicle(Vehicle vehicle) {
        // Validate vehicle data
        if (vehicle.getVehicleRegistration() == null || vehicle.getVehicleRegistration().trim().isEmpty()) {
            throw new RuntimeException("Vehicle registration is required");
        }
        
        if (vehicleRepository.existsByVehicleRegistration(vehicle.getVehicleRegistration())) {
            throw new RuntimeException("Vehicle registration already exists");
        }
        
        if (vehicle.getDailyRentalRate() == null || vehicle.getDailyRentalRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Daily rental rate must be greater than zero");
        }
        
        return vehicleRepository.save(vehicle);
    }
    
    public List<Vehicle> getVehiclesDueForMaintenance() {
        // This would filter vehicles due for maintenance
        // For now, return empty list
        return List.of();
    }
    
    // Legacy methods for backward compatibility
    public Vehicle getVehicleByRegistration(String registration) {
        return vehicleRepository.findByVehicleRegistration(registration)
            .orElseThrow(() -> new RuntimeException("Vehicle not found with registration: " + registration));
    }
    
    public List<Vehicle> getVehiclesByCategory(Integer categoryId) {
        return vehicleRepository.findByCategoryId(categoryId);
    }
    
    public List<Vehicle> getVehiclesByBrand(Integer brandId) {
        return vehicleRepository.findByBrandId(brandId);
    }
    
    public List<Vehicle> getVehiclesByLocation(Integer locationId) {
        return vehicleRepository.findByLocationId(locationId);
    }
    
    public List<Vehicle> getVehiclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public List<Vehicle> getAvailableVehiclesByCategoryAndLocation(Integer categoryId, Integer locationId) {
        return vehicleRepository.findAvailableVehiclesByCategoryAndLocation(categoryId, locationId);
    }
    
    public List<Vehicle> searchVehicles(String searchTerm) {
        return vehicleRepository.searchVehicles(searchTerm);
    }
    
    public Vehicle updateVehicle(Integer vehicleId, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(vehicleId);
        
        vehicle.setColor(vehicleDetails.getColor());
        vehicle.setMileage(vehicleDetails.getMileage());
        vehicle.setCurrentStatus(vehicleDetails.getCurrentStatus());
        vehicle.setCurrentLocation(vehicleDetails.getCurrentLocation());
        vehicle.setDailyRentalRate(vehicleDetails.getDailyRentalRate());
        vehicle.setInsurancePolicyNumber(vehicleDetails.getInsurancePolicyNumber());
        vehicle.setNextMaintenanceDate(vehicleDetails.getNextMaintenanceDate());
        vehicle.setVehicleDescription(vehicleDetails.getVehicleDescription());
        
        return vehicleRepository.save(vehicle);
    }
    
    public void deleteVehicle(Integer vehicleId) {
        deleteById(vehicleId);
    }
    
    public Vehicle updateVehicleLocation(Integer vehicleId, Integer newLocationId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        // Here you would fetch the new location and set it
        // vehicle.setCurrentLocation(locationService.getLocationById(newLocationId));
        return vehicleRepository.save(vehicle);
    }
    
    public Vehicle updateVehicleStatus(Integer vehicleId, Integer newStatusId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        // Here you would fetch the new status and set it
        // vehicle.setCurrentStatus(vehicleStatusService.getStatusById(newStatusId));
        return vehicleRepository.save(vehicle);
    }
    
    public Vehicle updateVehicleStatus(Integer vehicleId, Integer newStatusId, String notes) {
        Vehicle vehicle = getVehicleById(vehicleId);
        // Here you would fetch the new status and set it
        // vehicle.setCurrentStatus(vehicleStatusService.getStatusById(newStatusId));
        // Log the status change with notes
        return vehicleRepository.save(vehicle);
    }
}
