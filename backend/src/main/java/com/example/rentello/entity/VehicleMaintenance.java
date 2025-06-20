package com.example.rentello.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "VehicleMaintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "maintenanceId")
@EntityListeners(AuditingEntityListener.class)
public class VehicleMaintenance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaintenanceID")
    private Integer maintenanceId;
    
    @Column(name = "ScheduledDate", nullable = false)
    private LocalDate scheduledDate;
    
    @Column(name = "CompletedDate")
    private LocalDate completedDate;
    
    @Column(name = "Cost", precision = 10, scale = 2)
    private BigDecimal cost;
    
    @Column(name = "ServiceProvider", length = 100)
    private String serviceProvider;
    
    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    @Column(name = "NextServiceMileage")
    private Integer nextServiceMileage;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VehicleID", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaintenanceTypeID", nullable = false)
    private MaintenanceType maintenanceType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;
}
