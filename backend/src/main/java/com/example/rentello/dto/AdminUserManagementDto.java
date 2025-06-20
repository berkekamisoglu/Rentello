package com.example.rentello.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserManagementDto {
    
    private Integer userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    // Role bilgileri
    private Integer roleId;
    private String roleName;
    
    // City bilgileri
    private Integer cityId;
    private String cityName;
    
    // İstatistikler
    private Long totalRentals;
    private Long activeRentals;
} 