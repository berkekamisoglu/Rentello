package com.example.rentello.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "vw_RevenueSummary")
@IdClass(ViewRevenueSummaryId.class)
public class ViewRevenueSummary {
    
    @Id
    @Column(name = "Year")
    private Integer year;
    
    @Id
    @Column(name = "Month")
    private Integer month;
    
    @Column(name = "MonthName")
    private String monthName;
    
    @Column(name = "TotalRentals")
    private Long totalRentals;
    
    @Column(name = "TotalBaseAmount")
    private BigDecimal totalBaseAmount;
    
    @Column(name = "TotalTaxAmount")
    private BigDecimal totalTaxAmount;
    
    @Column(name = "TotalRevenue")
    private BigDecimal totalRevenue;
    
    @Column(name = "AverageRentalValue")
    private BigDecimal averageRentalValue;
    
    @Column(name = "UniqueCustomers")
    private Long uniqueCustomers;
    
    @Column(name = "VehiclesRented")
    private Long vehiclesRented;

    // Getters and Setters
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public Long getTotalRentals() {
        return totalRentals;
    }

    public void setTotalRentals(Long totalRentals) {
        this.totalRentals = totalRentals;
    }

    public BigDecimal getTotalBaseAmount() {
        return totalBaseAmount;
    }

    public void setTotalBaseAmount(BigDecimal totalBaseAmount) {
        this.totalBaseAmount = totalBaseAmount;
    }

    public BigDecimal getTotalTaxAmount() {
        return totalTaxAmount;
    }

    public void setTotalTaxAmount(BigDecimal totalTaxAmount) {
        this.totalTaxAmount = totalTaxAmount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageRentalValue() {
        return averageRentalValue;
    }

    public void setAverageRentalValue(BigDecimal averageRentalValue) {
        this.averageRentalValue = averageRentalValue;
    }

    public Long getUniqueCustomers() {
        return uniqueCustomers;
    }

    public void setUniqueCustomers(Long uniqueCustomers) {
        this.uniqueCustomers = uniqueCustomers;
    }

    public Long getVehiclesRented() {
        return vehiclesRented;
    }

    public void setVehiclesRented(Long vehiclesRented) {
        this.vehiclesRented = vehiclesRented;
    }
} 