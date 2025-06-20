package com.example.rentello.controller;

import com.example.rentello.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> summary = reportService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Map<String, Object> report = reportService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/vehicle-utilization")
    public ResponseEntity<Map<String, Object>> getVehicleUtilizationReport() {
        Map<String, Object> report = reportService.getVehicleUtilizationReport();
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getCustomerReport() {
        Map<String, Object> report = reportService.getCustomerReport();
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/maintenance")
    public ResponseEntity<Map<String, Object>> getMaintenanceReport() {
        Map<String, Object> report = reportService.getMaintenanceReport();
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/fleet-status")
    public ResponseEntity<Map<String, Object>> getFleetStatusReport() {
        Map<String, Object> report = reportService.getFleetStatusReport();
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/location-performance")
    public ResponseEntity<Map<String, Object>> getLocationPerformanceReport() {
        Map<String, Object> report = reportService.getLocationPerformanceReport();
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdvancedAnalytics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Map<String, Object> analytics = reportService.getAdvancedAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
} 