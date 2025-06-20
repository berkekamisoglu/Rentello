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

import java.time.LocalDateTime;

@Entity
@Table(name = "Locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "locationId")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LocationID")
    private Integer locationId;
    
    @Column(name = "LocationName", nullable = false, length = 100)
    private String locationName;
    
    @Column(name = "Address", nullable = false, length = 255)
    private String address;
    
    @Column(name = "PhoneNumber", length = 20)
    private String phoneNumber;
    
    @Column(name = "Email", length = 100)
    private String email;
    
    @Column(name = "IsActive")
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CityID", nullable = false)
    private City city;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ManagerID")
    private User manager;
}
