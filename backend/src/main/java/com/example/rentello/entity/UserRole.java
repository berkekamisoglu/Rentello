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

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "UserRoles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "roleId")
@EntityListeners(AuditingEntityListener.class)
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer roleId;
    
    @Column(name = "RoleName", nullable = false, unique = true, length = 50)
    private String roleName;
    
    @Column(name = "RoleDescription", length = 255)
    private String roleDescription;
    
    @Column(name = "Permissions", columnDefinition = "NVARCHAR(MAX)")
    private String permissions; // JSON format permissions
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
    
    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "userRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}
