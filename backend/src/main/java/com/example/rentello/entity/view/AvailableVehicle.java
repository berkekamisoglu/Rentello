package com.example.rentello.entity.view;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vw_AvailableVehicles")
@Immutable
public class AvailableVehicle {
    
    @Id
    @Column(name = "VehicleID")
    private Integer vehicleId;
    
    @Column(name = "VehicleRegistration")
    private String vehicleRegistration;
    
    @Column(name = "BrandName")
    private String brandName;
    
    @Column(name = "ModelName")
    private String modelName;
    
    @Column(name = "CategoryName")
    private String categoryName;
    
    @Column(name = "Color")
    private String color;
    
    @Column(name = "Mileage")
    private Integer mileage;
    
    @Column(name = "DailyRentalRate")
    private BigDecimal dailyRentalRate;
    
    @Column(name = "StatusName")
    private String statusName;
    
    @Column(name = "LocationName")
    private String locationName;
    
    @Column(name = "CityName")
    private String cityName;
    
    @Column(name = "ManufactureYear")
    private Integer manufactureYear;
    
    @Column(name = "FuelType")
    private String fuelType;
    
    @Column(name = "TransmissionType")
    private String transmissionType;
    
    @Column(name = "SeatingCapacity")
    private Integer seatingCapacity;
    
    // Removed columns that don't exist in vw_AvailableVehicles view:
    // VehicleDescription, ImageUrls, Year, IsAvailableForRent
} 