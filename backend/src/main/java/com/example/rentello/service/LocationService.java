package com.example.rentello.service;

import com.example.rentello.entity.Location;
import com.example.rentello.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    
    private final LocationRepository locationRepository;
    
    // Basic CRUD operations
    public Optional<Location> findById(Integer locationId) {
        return locationRepository.findById(locationId);
    }
    
    public Location save(Location location) {
        return locationRepository.save(location);
    }
    
    public void deleteById(Integer locationId) {
        Location location = getLocationById(locationId);
        locationRepository.delete(location);
    }
    
    public Page<Location> getAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }
    
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
    
    // Business logic methods
    public Location getLocationById(Integer locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
    }
    
    public List<Location> getLocationsByCity(Integer cityId) {
        return locationRepository.findByCityId(cityId);
    }
    
    public List<Location> getActiveLocations() {
        return locationRepository.findByIsActiveTrue();
    }
    
    public List<Location> searchLocations(String searchTerm) {
        return locationRepository.searchLocations(searchTerm);
    }
    
    public Location createLocation(Location location) {
        // Validate location data
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty()) {
            throw new RuntimeException("Location name is required");
        }
        
        if (location.getAddress() == null || location.getAddress().trim().isEmpty()) {
            throw new RuntimeException("Location address is required");
        }
        
        // Set default active status
        if (location.getIsActive() == null) {
            location.setIsActive(true);
        }
        
        return locationRepository.save(location);
    }
    
    public Location updateLocation(Integer locationId, Location locationDetails) {
        Location location = getLocationById(locationId);
        
        location.setLocationName(locationDetails.getLocationName());
        location.setAddress(locationDetails.getAddress());
        location.setPhoneNumber(locationDetails.getPhoneNumber());
        location.setEmail(locationDetails.getEmail());
        location.setIsActive(locationDetails.getIsActive());
        location.setCity(locationDetails.getCity());
        location.setManager(locationDetails.getManager());
        
        return locationRepository.save(location);
    }
    
    public Location activateLocation(Integer locationId) {
        Location location = getLocationById(locationId);
        location.setIsActive(true);
        return locationRepository.save(location);
    }
    
    public Location deactivateLocation(Integer locationId) {
        Location location = getLocationById(locationId);
        location.setIsActive(false);
        return locationRepository.save(location);
    }
    
    public List<Location> getLocationsByManager(Integer managerId) {
        return locationRepository.findByManagerId(managerId);
    }
    
    public long getVehicleCountAtLocation(Integer locationId) {
        // This would be implemented with a repository method
        // For now, return a mock value
        return 0L;
    }
    
    public boolean hasActiveRentalsAtLocation(Integer locationId) {
        // This would check if there are any active rentals at this location
        // For now, return false
        return false;
    }
} 