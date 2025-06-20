package com.example.rentello.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final RentalService rentalService;
    private final VehicleService vehicleService;
    private final PaymentService paymentService;
    private final UserService userService;
    
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Basic counts
        summary.put("totalVehicles", vehicleService.getAllVehicles().size());
        summary.put("totalUsers", userService.getAllUsers().size());
        summary.put("totalRentals", rentalService.getAllRentals().size());
        summary.put("activeRentals", rentalService.getActiveRentals().size());
        summary.put("overdueRentals", rentalService.getOverdueRentals().size());
        
        // Revenue summary
        BigDecimal totalRevenue = paymentService.getTotalRevenueForPeriod(
                LocalDateTime.now().minusDays(30), LocalDateTime.now());
        summary.put("monthlyRevenue", totalRevenue);
        
        return summary;
    }
    
    public Map<String, Object> getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Set default dates if not provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        BigDecimal totalRevenue = paymentService.getTotalRevenueForPeriod(startDate, endDate);
        
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalRevenue", totalRevenue);
        report.put("totalTransactions", paymentService.getPaymentsByDateRange(startDate, endDate).size());
        
        return report;
    }
    
    public Map<String, Object> getVehicleUtilizationReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Mock data - in real implementation would calculate from database
        report.put("totalVehicles", vehicleService.getAllVehicles().size());
        report.put("availableVehicles", vehicleService.getAvailableVehicles().size());
        report.put("averageUtilization", 75.5);
        
        return report;
    }
    
    public Map<String, Object> getCustomerReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Mock data - in real implementation would calculate from database
        report.put("totalCustomers", userService.getUsersByRole("Customer").size());
        report.put("activeCustomers", userService.getActiveUsers().size());
        report.put("newCustomersThisMonth", 15);
        
        return report;
    }
    
    public Map<String, Object> getMaintenanceReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Mock data - in real implementation would calculate from database
        report.put("scheduledMaintenance", 8);
        report.put("overdueMaintenance", 3);
        report.put("completedMaintenanceThisMonth", 25);
        
        return report;
    }
    
    public Map<String, Object> getFleetStatusReport() {
        Map<String, Object> report = new HashMap<>();
        
        int totalVehicles = vehicleService.getAllVehicles().size();
        int availableVehicles = vehicleService.getAvailableVehicles().size();
        
        report.put("totalVehicles", totalVehicles);
        report.put("availableVehicles", availableVehicles);
        report.put("rentedVehicles", totalVehicles - availableVehicles);
        report.put("maintenanceVehicles", 5); // Mock data
        report.put("damagedVehicles", 2); // Mock data
        
        return report;
    }
    
    public Map<String, Object> getLocationPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Mock data - in real implementation would calculate from database
        report.put("totalLocations", 8);
        report.put("activeLocations", 7);
        report.put("topPerformingLocation", "Istanbul Airport");
        report.put("locationRevenue", Map.of(
                "Istanbul Airport", 45000,
                "Ankara Center", 32000,
                "Izmir Port", 28000
        ));
        
        return report;
    }
    
    public Map<String, Object> getAdvancedAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Combine multiple reports for advanced analytics
        analytics.put("revenue", getRevenueReport(startDate, endDate));
        analytics.put("fleet", getFleetStatusReport());
        analytics.put("customers", getCustomerReport());
        analytics.put("maintenance", getMaintenanceReport());
        analytics.put("locations", getLocationPerformanceReport());
        
        return analytics;
    }
} 