package com.example.rentello.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "vehicleId")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VehicleID")
    private Integer vehicleId;
    
    @Column(name = "VehicleRegistration", nullable = false, unique = true, length = 20)
    private String vehicleRegistration;
    
    @Column(name = "Color", length = 30)
    private String color;
    
    @Column(name = "Mileage")
    private Integer mileage = 0;
    
    @Column(name = "PurchaseDate")
    private LocalDate purchaseDate;
    
    @Column(name = "PurchasePrice", precision = 12, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "DailyRentalRate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRentalRate;
    
    @Column(name = "InsurancePolicyNumber", length = 50)
    private String insurancePolicyNumber;
    
    @Column(name = "NextMaintenanceDate")
    private LocalDate nextMaintenanceDate;
    
    @Column(name = "VehicleDescription", columnDefinition = "NVARCHAR(MAX)")
    private String vehicleDescription; // For full-text search
    
    @Column(name = "ImageUrls", columnDefinition = "NVARCHAR(MAX)")
    private String imageUrls; // JSON format: ["url1", "url2", "url3"]
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ModelID", nullable = false)
    private VehicleModel model;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CurrentStatusID", nullable = false)
    private VehicleStatus currentStatus;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CurrentLocationID", nullable = false)
    private Location currentLocation;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedBy")
    private User updatedBy;
}
