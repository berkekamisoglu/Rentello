package com.example.rentello.controller;

import com.example.rentello.entity.*;
import com.example.rentello.service.VehicleService;
import com.example.rentello.types.SearchFilters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    @GetMapping
    public ResponseEntity<Page<Vehicle>> getAllVehicles(Pageable pageable) {
        return ResponseEntity.ok(vehicleService.getAllVehicles(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Integer id) {
        return vehicleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Use real database data
            List<Vehicle> vehicles = vehicleService.getAvailableVehicles(startDate, endDate);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error fetching vehicles: " + e.getMessage());
            e.printStackTrace();
            // Return empty list on error
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<Vehicle>> searchVehicles(@RequestBody SearchFilters filters) {
        try {
            List<Vehicle> vehicles = vehicleService.searchVehicles(filters);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByCategory(@PathVariable Integer categoryId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByCategory(categoryId);
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByBrand(@PathVariable Integer brandId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByBrand(brandId);
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByLocation(@PathVariable Integer locationId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByLocation(locationId);
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<Vehicle>> getVehiclesByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(vehicles);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        try {
            Vehicle savedVehicle = vehicleService.createVehicle(vehicle);
            return ResponseEntity.ok(savedVehicle);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Integer id, @Valid @RequestBody Vehicle vehicle) {
        return vehicleService.findById(id)
                .map(existingVehicle -> {
                    Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicle);
                    return ResponseEntity.ok(updatedVehicle);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Integer id) {
        return vehicleService.findById(id)
                .map(vehicle -> {
                    vehicleService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Vehicle> updateVehicleStatus(@PathVariable Integer id, @RequestParam Integer statusId) {
        try {
            Vehicle vehicle = vehicleService.updateVehicleStatus(id, statusId);
            return ResponseEntity.ok(vehicle);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Vehicle> updateVehicleLocation(@PathVariable Integer id, @RequestParam Integer locationId) {
        try {
            Vehicle vehicle = vehicleService.updateVehicleLocation(id, locationId);
            return ResponseEntity.ok(vehicle);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/maintenance-due")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Vehicle>> getVehiclesDueForMaintenance() {
        List<Vehicle> vehicles = vehicleService.getVehiclesDueForMaintenance();
        return ResponseEntity.ok(vehicles);
    }
} 