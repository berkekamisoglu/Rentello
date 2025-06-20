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

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "VehicleModels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "modelId")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vehicles"})
public class VehicleModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ModelID")
    private Integer modelId;
    
    @Column(name = "ModelName", nullable = false, length = 100)
    private String modelName;
    
    @Column(name = "ManufactureYear")
    private Integer manufactureYear;
    
    @Column(name = "EngineType", length = 50)
    private String engineType;
    
    @Column(name = "FuelType", length = 30)
    private String fuelType;
    
    @Column(name = "TransmissionType", length = 30)
    private String transmissionType;
    
    @Column(name = "SeatingCapacity")
    private Integer seatingCapacity;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BrandID", nullable = false)
    private VehicleBrand brand;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CategoryID", nullable = false)
    private VehicleCategory category;
    
    @JsonIgnore
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles;
}
