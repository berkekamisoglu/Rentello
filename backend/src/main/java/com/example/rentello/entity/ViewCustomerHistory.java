package com.example.rentello.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "vw_CustomerHistory")
public class ViewCustomerHistory {
    
    @Id
    @Column(name = "CustomerID")
    private Integer customerId;
    
    @Column(name = "CustomerName")
    private String customerName;
    
    @Column(name = "Email")
    private String email;
    
    @Column(name = "PhoneNumber")
    private String phoneNumber;
    
    @Column(name = "TotalRentals")
    private Long totalRentals;
    
    @Column(name = "TotalSpent")
    private BigDecimal totalSpent;
    
    @Column(name = "LastRentalDate")
    private LocalDateTime lastRentalDate;
    
    @Column(name = "AverageRentalAmount")
    private BigDecimal averageRentalAmount;
    
    @Column(name = "LoyaltyTier")
    private String loyaltyTier;
    
    @Column(name = "PreferredVehicleCategory")
    private String preferredVehicleCategory;

    // Default constructor
    public ViewCustomerHistory() {}

    // Getters
    public Integer getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Long getTotalRentals() { return totalRentals; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public LocalDateTime getLastRentalDate() { return lastRentalDate; }
    public BigDecimal getAverageRentalAmount() { return averageRentalAmount; }
    public String getLoyaltyTier() { return loyaltyTier; }
    public String getPreferredVehicleCategory() { return preferredVehicleCategory; }
}
