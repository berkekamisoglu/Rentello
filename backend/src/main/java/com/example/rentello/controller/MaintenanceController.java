package com.example.rentello.controller;

import com.example.rentello.entity.VehicleMaintenance;
import com.example.rentello.entity.MaintenanceType;
import com.example.rentello.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaintenanceController {
    
    private final MaintenanceService maintenanceService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<VehicleMaintenance>> getAllMaintenance() {
        List<VehicleMaintenance> maintenance = maintenanceService.getAllMaintenance();
        return ResponseEntity.ok(maintenance);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<VehicleMaintenance> getMaintenanceById(@PathVariable Integer id) {
        return maintenanceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<VehicleMaintenance>> getMaintenanceByVehicle(@PathVariable Integer vehicleId) {
        List<VehicleMaintenance> maintenance = maintenanceService.getMaintenanceByVehicle(vehicleId);
        return ResponseEntity.ok(maintenance);
    }
    
    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<VehicleMaintenance>> getScheduledMaintenance() {
        List<VehicleMaintenance> maintenance = maintenanceService.getScheduledMaintenance();
        return ResponseEntity.ok(maintenance);
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<VehicleMaintenance>> getOverdueMaintenance() {
        List<VehicleMaintenance> maintenance = maintenanceService.getOverdueMaintenance();
        return ResponseEntity.ok(maintenance);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<VehicleMaintenance> scheduleMaintenance(@Valid @RequestBody VehicleMaintenance maintenance) {
        try {
            VehicleMaintenance savedMaintenance = maintenanceService.scheduleMaintenance(maintenance);
            return ResponseEntity.ok(savedMaintenance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<VehicleMaintenance> updateMaintenance(@PathVariable Integer id, 
                                                                @Valid @RequestBody VehicleMaintenance maintenance) {
        return maintenanceService.findById(id)
                .map(existingMaintenance -> {
                    VehicleMaintenance updatedMaintenance = maintenanceService.save(maintenance);
                    return ResponseEntity.ok(updatedMaintenance);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<VehicleMaintenance> completeMaintenance(
            @PathVariable Integer id,
            @RequestParam LocalDate completedDate,
            @RequestParam BigDecimal cost,
            @RequestParam(required = false) String notes) {
        
        try {
            VehicleMaintenance completedMaintenance = maintenanceService.completeMaintenance(
                    id, completedDate, cost, notes);
            return ResponseEntity.ok(completedMaintenance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable Integer id) {
        return maintenanceService.findById(id)
                .map(maintenance -> {
                    maintenanceService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Maintenance Types endpoints
    @GetMapping("/types")
    public ResponseEntity<List<MaintenanceType>> getAllMaintenanceTypes() {
        List<MaintenanceType> types = maintenanceService.getAllMaintenanceTypes();
        return ResponseEntity.ok(types);
    }
} 