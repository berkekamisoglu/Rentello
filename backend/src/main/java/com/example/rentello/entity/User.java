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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "userId")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;
    
    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "FirstName", nullable = false, length = 50)
    private String firstName;
    
    @Column(name = "LastName", nullable = false, length = 50)
    private String lastName;
    
    @Column(name = "PhoneNumber", length = 20)
    private String phoneNumber;
    
    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;
    
    @Column(name = "NationalID")
    private byte[] nationalId; // Encrypted
    
    @Column(name = "Address", length = 255)
    private String address;
    
    @Column(name = "IsActive")
    private Boolean isActive = true;
    
    @Column(name = "LastLoginDate")
    private LocalDateTime lastLoginDate;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
    
    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleID", nullable = false)
    private UserRole userRole;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CityID")
    private City city;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedBy")
    private User updatedBy;
    
    // One-to-many relationships
    @JsonIgnore
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> createdUsers;
    
    @JsonIgnore
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> updatedUsers;
    
    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Rental> rentals;
    
    @JsonIgnore
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Location> managedLocations;
}
