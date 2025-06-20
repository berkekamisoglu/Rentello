package com.example.rentello.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminVehicleManagementDto {
    
    private Integer vehicleId;
    private String licensePlate;
    private String vin;
    private String color;
    private Integer year;
    private BigDecimal dailyRate;
    private Integer mileage;
    private String fuelType;
    private String transmissionType;
    private Integer capacity;
    private Boolean isActive;
    private String features;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Brand ve Model bilgileri
    private Integer brandId;
    private String brandName;
    private Integer modelId;
    private String modelName;
    
    // Category bilgileri
    private Integer categoryId;
    private String categoryName;
    
    // Location bilgileri
    private Integer locationId;
    private String locationName;
    
    // Status bilgileri
    private Integer statusId;
    private String statusName;
    
    // İstatistikler
    private Long totalRentals;
    private Long activeRentals;
    private BigDecimal totalRevenue;
    private Integer maintenanceCount;
} 