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
@Table(name = "PaymentMethods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "paymentMethodId")
@EntityListeners(AuditingEntityListener.class)
public class PaymentMethod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentMethodID")
    private Integer paymentMethodId;
    
    @Column(name = "MethodName", nullable = false, unique = true, length = 50)
    private String methodName;
    
    @Column(name = "MethodDescription", length = 255)
    private String methodDescription;
    
    @Column(name = "IsActive")
    private Boolean isActive = true;
    
    @Column(name = "ProcessingFeePercentage", precision = 5, scale = 4)
    private BigDecimal processingFeePercentage = BigDecimal.ZERO;
    
    @CreatedDate
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    // Relationships
    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
}
