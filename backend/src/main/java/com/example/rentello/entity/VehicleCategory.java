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
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "VehicleCategories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "categoryId")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vehicleModels"})
public class VehicleCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Integer categoryId;
    
    @Column(name = "CategoryName", nullable = false, unique = true, length = 50)
    private String categoryName;
    
    @Column(name = "CategoryDescription", length = 255)
    private String categoryDescription;
    
    @Column(name = "BasePrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
    
    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleModel> vehicleModels;
}
