package com.example.rentello.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "rentalId")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Rental {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RentalID")
    private Integer rentalId;
    
    @Column(name = "PlannedPickupDate", nullable = false)
    private LocalDateTime plannedPickupDate;
    
    @Column(name = "PlannedReturnDate", nullable = false)
    private LocalDateTime plannedReturnDate;
    
    @Column(name = "ActualPickupDate")
    private LocalDateTime actualPickupDate;
    
    @Column(name = "ActualReturnDate")
    private LocalDateTime actualReturnDate;
    
    @Column(name = "BaseAmount", precision = 12, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;
    
    @Column(name = "TaxAmount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "DiscountAmount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "TotalAmount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "SecurityDeposit", precision = 12, scale = 2)
    private BigDecimal securityDeposit = BigDecimal.ZERO;
    
    @Column(name = "Notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CustomerID", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VehicleID", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PickupLocationID", nullable = false)
    private Location pickupLocation;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ReturnLocationID", nullable = false)
    private Location returnLocation;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RentalStatusID", nullable = false)
    private RentalStatus rentalStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedBy")
    private User updatedBy;
}
