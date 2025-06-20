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
@Table(name = "DamageTypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "damageTypeId")
@EntityListeners(AuditingEntityListener.class)
public class DamageType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DamageTypeID")
    private Integer damageTypeId;
    
    @Column(name = "DamageTypeName", nullable = false, unique = true, length = 50)
    private String damageTypeName;
    
    @Column(name = "DamageDescription", length = 255)
    private String damageDescription;
    
    @Column(name = "AverageRepairCost", precision = 10, scale = 2)
    private BigDecimal averageRepairCost;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    // Relationships
    @OneToMany(mappedBy = "damageType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleDamage> vehicleDamages;
}
