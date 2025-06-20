package com.example.rentello.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    
    // Genel İstatistikler
    private Long totalUsers;
    private Long totalVehicles;
    private Long totalRentals;
    private Long activeRentals;
    
    // Gelir İstatistikleri
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal dailyRevenue;
    
    // Araç İstatistikleri
    private Long availableVehicles;
    private Long rentedVehicles;
    private Long maintenanceVehicles;
    private Long outOfServiceVehicles;
    
    // Son 7 Günlük İstatistikler
    private Map<LocalDate, Integer> dailyRentals;
    private Map<LocalDate, BigDecimal> dailyRevenues;
    
    // Popüler Araç Kategorileri
    private Map<String, Long> popularCategories;
    
    // Müşteri İstatistikleri
    private Long newCustomersThisMonth;
    private Long activeCustomers;
    
    // Lokasyon İstatistikleri
    private Long totalLocations;
    private Long activeLocations;
} 