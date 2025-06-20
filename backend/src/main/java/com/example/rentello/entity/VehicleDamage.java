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
@Table(name = "VehicleDamages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "damageId")
@EntityListeners(AuditingEntityListener.class)
public class VehicleDamage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DamageID")
    private Integer damageId;
    
    @Column(name = "DamageDate", nullable = false)
    private LocalDate damageDate;
    
    @Column(name = "DamageDescription", columnDefinition = "NVARCHAR(MAX)")
    private String damageDescription;
    
    @Column(name = "RepairCost", precision = 10, scale = 2)
    private BigDecimal repairCost;
    
    @Column(name = "RepairDate")
    private LocalDate repairDate;
    
    @Column(name = "IsRepaired")
    private Boolean isRepaired = false;
    
    @Column(name = "ResponsibleParty", length = 100)
    private String responsibleParty;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VehicleID", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RentalID")
    private Rental rental; // NULL if damage not related to rental
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DamageTypeID", nullable = false)
    private DamageType damageType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;
}
