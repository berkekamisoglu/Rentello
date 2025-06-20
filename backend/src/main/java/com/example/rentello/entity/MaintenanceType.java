package com.example.rentello.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MaintenanceTypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "maintenanceTypeId")
@EntityListeners(AuditingEntityListener.class)
public class MaintenanceType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaintenanceTypeID")
    private Integer maintenanceTypeId;
    
    @Column(name = "TypeName", nullable = false, unique = true, length = 50)
    private String typeName;
    
    @Column(name = "TypeDescription", length = 255)
    private String typeDescription;
    
    @Column(name = "EstimatedDuration")
    private Integer estimatedDuration; // in hours
    
    @Column(name = "AverageCost", precision = 10, scale = 2)
    private BigDecimal averageCost;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    // Relationships
    @OneToMany(mappedBy = "maintenanceType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleMaintenance> vehicleMaintenances;
}
