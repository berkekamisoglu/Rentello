package com.example.rentello.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "CustomerFeedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "feedbackId")
@EntityListeners(AuditingEntityListener.class)
public class CustomerFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FeedbackID")
    private Integer feedbackId;
    
    @Column(name = "Rating")
    private Integer rating;
    
    @Column(name = "FeedbackText", columnDefinition = "NVARCHAR(MAX)")
    private String feedbackText;
    
    @Column(name = "ResponseText", columnDefinition = "NVARCHAR(MAX)")
    private String responseText;
    
    @Column(name = "FeedbackDate")
    private LocalDateTime feedbackDate = LocalDateTime.now();
    
    @Column(name = "ResponseDate")
    private LocalDateTime responseDate;
    
    @Column(name = "IsPublic")
    private Boolean isPublic = false;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RentalID", nullable = false)
    private Rental rental;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RespondedBy")
    private User respondedBy;
}
