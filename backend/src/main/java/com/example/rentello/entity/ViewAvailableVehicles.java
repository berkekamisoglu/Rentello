package com.example.rentello.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "vw_AvailableVehicles")
public class ViewAvailableVehicles {
    
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

    // Getters and Setters
    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public BigDecimal getDailyRentalRate() {
        return dailyRentalRate;
    }

    public void setDailyRentalRate(BigDecimal dailyRentalRate) {
        this.dailyRentalRate = dailyRentalRate;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Integer manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }
} 