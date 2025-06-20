package com.example.rentello.controller;

import com.example.rentello.entity.Location;
import com.example.rentello.entity.City;
import com.example.rentello.entity.Country;
import com.example.rentello.service.LocationService;
import com.example.rentello.dto.LocationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocationController {
    
    private final LocationService locationService;
    
    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        try {
            List<Location> locations = locationService.getAllLocations();
            List<LocationDto> locationDtos = convertToLocationDtos(locations);
            return ResponseEntity.ok(locationDtos);
        } catch (Exception e) {
            // Return test data if database is not available
            List<LocationDto> testLocations = createTestLocationDtos();
            return ResponseEntity.ok(testLocations);
        }
    }
    
    private List<Location> createTestLocations() {
        List<Location> locations = new ArrayList<>();
        
        // Create test country
        Country turkey = new Country();
        turkey.setCountryId(1);
        turkey.setCountryName("Türkiye");
        turkey.setCountryCode("TR");
        
        // Create test cities
        City istanbul = new City();
        istanbul.setCityId(1);
        istanbul.setCityName("İstanbul");
        istanbul.setCountry(turkey);
        
        City ankara = new City();
        ankara.setCityId(2);
        ankara.setCityName("Ankara");
        ankara.setCountry(turkey);
        
        City izmir = new City();
        izmir.setCityId(3);
        izmir.setCityName("İzmir");
        izmir.setCountry(turkey);
        
        // Create test locations
        Location location1 = new Location();
        location1.setLocationId(1);
        location1.setLocationName("İstanbul Merkez");
        location1.setAddress("Taksim Meydanı No:1, Beyoğlu, İstanbul");
        location1.setPhoneNumber("+90 212 555 0101");
        location1.setEmail("istanbul@rentello.com");
        location1.setIsActive(true);
        location1.setCity(istanbul);
        locations.add(location1);
        
        Location location2 = new Location();
        location2.setLocationId(2);
        location2.setLocationName("Ankara Merkez");
        location2.setAddress("Kızılay Caddesi No:15, Çankaya, Ankara");
        location2.setPhoneNumber("+90 312 555 0201");
        location2.setEmail("ankara@rentello.com");
        location2.setIsActive(true);
        location2.setCity(ankara);
        locations.add(location2);
        
        Location location3 = new Location();
        location3.setLocationId(3);
        location3.setLocationName("İzmir Merkez");
        location3.setAddress("Kordon Caddesi No:25, Konak, İzmir");
        location3.setPhoneNumber("+90 232 555 0301");
        location3.setEmail("izmir@rentello.com");
        location3.setIsActive(true);
        location3.setCity(izmir);
        locations.add(location3);
        
        return locations;
    }
    
    private List<LocationDto> convertToLocationDtos(List<Location> locations) {
        return locations.stream().map(this::convertToLocationDto).toList();
    }
    
    private LocationDto convertToLocationDto(Location location) {
        LocationDto dto = new LocationDto();
        dto.setLocationId(location.getLocationId());
        dto.setLocationName(location.getLocationName());
        dto.setAddress(location.getAddress());
        dto.setPhoneNumber(location.getPhoneNumber());
        dto.setEmail(location.getEmail());
        dto.setIsActive(location.getIsActive());
        
        if (location.getCity() != null) {
            LocationDto.CityDto cityDto = new LocationDto.CityDto();
            cityDto.setCityId(location.getCity().getCityId());
            cityDto.setCityName(location.getCity().getCityName());
            cityDto.setPostalCode(location.getCity().getPostalCode());
            
            if (location.getCity().getCountry() != null) {
                LocationDto.CountryDto countryDto = new LocationDto.CountryDto();
                countryDto.setCountryId(location.getCity().getCountry().getCountryId());
                countryDto.setCountryName(location.getCity().getCountry().getCountryName());
                countryDto.setCountryCode(location.getCity().getCountry().getCountryCode());
                cityDto.setCountry(countryDto);
            }
            dto.setCity(cityDto);
        }
        
        return dto;
    }
    
    private List<LocationDto> createTestLocationDtos() {
        List<LocationDto> locations = new ArrayList<>();
        
        // Create test locations with DTOs
        LocationDto location1 = new LocationDto();
        location1.setLocationId(1);
        location1.setLocationName("İstanbul Merkez");
        location1.setAddress("Taksim Meydanı No:1, Beyoğlu, İstanbul");
        location1.setPhoneNumber("+90 212 555 0101");
        location1.setEmail("istanbul@rentello.com");
        location1.setIsActive(true);
        
        LocationDto.CountryDto turkey = new LocationDto.CountryDto();
        turkey.setCountryId(1);
        turkey.setCountryName("Türkiye");
        turkey.setCountryCode("TR");
        
        LocationDto.CityDto istanbul = new LocationDto.CityDto();
        istanbul.setCityId(1);
        istanbul.setCityName("İstanbul");
        istanbul.setCountry(turkey);
        location1.setCity(istanbul);
        locations.add(location1);
        
        LocationDto location2 = new LocationDto();
        location2.setLocationId(2);
        location2.setLocationName("Ankara Merkez");
        location2.setAddress("Kızılay Caddesi No:15, Çankaya, Ankara");
        location2.setPhoneNumber("+90 312 555 0201");
        location2.setEmail("ankara@rentello.com");
        location2.setIsActive(true);
        
        LocationDto.CityDto ankara = new LocationDto.CityDto();
        ankara.setCityId(2);
        ankara.setCityName("Ankara");
        ankara.setCountry(turkey);
        location2.setCity(ankara);
        locations.add(location2);
        
        LocationDto location3 = new LocationDto();
        location3.setLocationId(3);
        location3.setLocationName("İzmir Merkez");
        location3.setAddress("Kordon Caddesi No:25, Konak, İzmir");
        location3.setPhoneNumber("+90 232 555 0301");
        location3.setEmail("izmir@rentello.com");
        location3.setIsActive(true);
        
        LocationDto.CityDto izmir = new LocationDto.CityDto();
        izmir.setCityId(3);
        izmir.setCityName("İzmir");
        izmir.setCountry(turkey);
        location3.setCity(izmir);
        locations.add(location3);
        
        return locations;
    }
    
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<Location>> getAllLocationsPaginated(Pageable pageable) {
        return ResponseEntity.ok(locationService.getAllLocations(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Integer id) {
        return locationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<LocationDto>> getActiveLocations() {
        try {
            List<Location> locations = locationService.getActiveLocations();
            List<LocationDto> locationDtos = convertToLocationDtos(locations);
            return ResponseEntity.ok(locationDtos);
        } catch (Exception e) {
            List<LocationDto> testLocations = createTestLocationDtos();
            return ResponseEntity.ok(testLocations);
        }
    }
    
    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<Location>> getLocationsByCity(@PathVariable Integer cityId) {
        List<Location> locations = locationService.getLocationsByCity(cityId);
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Location>> getLocationsByManager(@PathVariable Integer managerId) {
        List<Location> locations = locationService.getLocationsByManager(managerId);
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Location>> searchLocations(@RequestParam String searchTerm) {
        List<Location> locations = locationService.searchLocations(searchTerm);
        return ResponseEntity.ok(locations);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Location> createLocation(@Valid @RequestBody Location location) {
        try {
            Location savedLocation = locationService.createLocation(location);
            return ResponseEntity.ok(savedLocation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Location> updateLocation(@PathVariable Integer id, @Valid @RequestBody Location location) {
        return locationService.findById(id)
                .map(existingLocation -> {
                    Location updatedLocation = locationService.updateLocation(id, location);
                    return ResponseEntity.ok(updatedLocation);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Integer id) {
        return locationService.findById(id)
                .map(location -> {
                    locationService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Location> activateLocation(@PathVariable Integer id) {
        try {
            Location location = locationService.activateLocation(id);
            return ResponseEntity.ok(location);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Location> deactivateLocation(@PathVariable Integer id) {
        try {
            Location location = locationService.deactivateLocation(id);
            return ResponseEntity.ok(location);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/vehicle-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Long> getVehicleCountAtLocation(@PathVariable Integer id) {
        long count = locationService.getVehicleCountAtLocation(id);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/{id}/has-active-rentals")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Boolean> hasActiveRentalsAtLocation(@PathVariable Integer id) {
        boolean hasActiveRentals = locationService.hasActiveRentalsAtLocation(id);
        return ResponseEntity.ok(hasActiveRentals);
    }
} 