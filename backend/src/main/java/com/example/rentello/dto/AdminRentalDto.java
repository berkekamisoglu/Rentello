package com.example.rentello.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminRentalDto {
    private Integer rentalId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualReturnDate;
    private BigDecimal totalCost;
    private Integer totalDays;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Customer information
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Vehicle information
    private Integer vehicleId;
    private String vehiclePlate;
    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleYear;
    
    // Location information
    private Integer pickupLocationId;
    private String pickupLocationName;
    private Integer returnLocationId;
    private String returnLocationName;
    
    // Status information
    private Integer statusId;
    private String statusName;
    
    // Payment information
    private BigDecimal totalPaid;
    private BigDecimal remainingAmount;
    private String paymentStatus;
} 