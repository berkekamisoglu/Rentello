package com.example.rentello.entity.view;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vw_ActiveRentals")
@Immutable
public class ActiveRental {
    
    @Id
    @Column(name = "RentalID")
    private Integer rentalId;
    
    @Column(name = "CustomerName")
    private String customerName;
    
    @Column(name = "CustomerEmail")
    private String customerEmail;
    
    @Column(name = "CustomerPhone")
    private String customerPhone;
    
    @Column(name = "VehicleRegistration")
    private String vehicleRegistration;
    
    @Column(name = "VehicleName")
    private String vehicleName;
    
    @Column(name = "PickupLocation")
    private String pickupLocation;
    
    @Column(name = "ReturnLocation")
    private String returnLocation;
    
    @Column(name = "PlannedPickupDate")
    private LocalDateTime plannedPickupDate;
    
    @Column(name = "PlannedReturnDate")
    private LocalDateTime plannedReturnDate;
    
    @Column(name = "ActualPickupDate")
    private LocalDateTime actualPickupDate;
    
    @Column(name = "RentalStatus")
    private String rentalStatus;
    
    @Column(name = "TotalAmount")
    private BigDecimal totalAmount;
    
    @Column(name = "RentalDays")
    private Integer rentalDays;
    
    @Column(name = "OverdueDays")
    private Integer overdueDays;
} 