package com.example.rentello.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "paymentId")
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Integer paymentId;
    
    @Column(name = "PaymentAmount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentAmount;
    
    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate = LocalDateTime.now();
    
    @Column(name = "TransactionReference", length = 100)
    private String transactionReference;
    
    @Column(name = "PaymentStatus", length = 30)
    private String paymentStatus = "Pending";
    
    @Column(name = "ProcessingFee", precision = 10, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;
    
    @Column(name = "Notes", length = 500)
    private String notes;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RentalID", nullable = false)
    private Rental rental;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PaymentMethodID", nullable = false)
    private PaymentMethod paymentMethod;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy")
    private User createdBy;
}
