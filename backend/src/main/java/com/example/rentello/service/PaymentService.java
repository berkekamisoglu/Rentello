package com.example.rentello.service;

import com.example.rentello.entity.Payment;
import com.example.rentello.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    // Basic CRUD operations
    public Optional<Payment> findById(Integer paymentId) {
        return paymentRepository.findById(paymentId);
    }
    
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    public void deleteById(Integer paymentId) {
        Payment payment = getPaymentById(paymentId);
        paymentRepository.delete(payment);
    }
    
    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }
    
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    // Business logic methods
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
    }
    
    public List<Payment> getPaymentsByRental(Integer rentalId) {
        return paymentRepository.findByRentalId(rentalId);
    }
    
    public List<Payment> getPaymentsByCustomer(Integer customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }
    
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByPaymentStatus(status);
    }
    
    public Payment processPayment(Payment payment) {
        // Validate payment details
        if (payment.getPaymentAmount() == null || payment.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid payment amount");
        }
        
        // Set payment date if not provided
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        
        // Set default status if not provided
        if (payment.getPaymentStatus() == null || payment.getPaymentStatus().isEmpty()) {
            payment.setPaymentStatus("Pending");
        }
        
        // Calculate processing fee if applicable
        calculateProcessingFee(payment);
        
        return paymentRepository.save(payment);
    }
    
    public Payment updatePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    public Payment processRefund(Integer paymentId, BigDecimal refundAmount, String reason) {
        Payment originalPayment = getPaymentById(paymentId);
        
        if (!"Completed".equals(originalPayment.getPaymentStatus())) {
            throw new RuntimeException("Can only refund completed payments");
        }
        
        if (refundAmount.compareTo(originalPayment.getPaymentAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed original payment amount");
        }
        
        // Create refund record
        Payment refund = new Payment();
        refund.setRental(originalPayment.getRental());
        refund.setPaymentMethod(originalPayment.getPaymentMethod());
        refund.setPaymentAmount(refundAmount.negate()); // Negative amount for refund
        refund.setPaymentDate(LocalDateTime.now());
        refund.setPaymentStatus("Refunded");
        refund.setNotes("Refund for payment " + paymentId + 
                       (reason != null ? ". Reason: " + reason : ""));
        
        return paymentRepository.save(refund);
    }
    
    public Object getRevenueSummary(LocalDateTime startDate, LocalDateTime endDate) {
        // Set default dates if not provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        // Implementation for revenue summary
        List<Payment> payments = paymentRepository.findByDateRange(startDate, endDate);
        
        BigDecimal totalRevenue = payments.stream()
                .filter(p -> "Completed".equals(p.getPaymentStatus()))
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create and return summary map
        final LocalDateTime finalStartDate = startDate;
        final LocalDateTime finalEndDate = endDate;
        final BigDecimal finalTotalRevenue = totalRevenue;
        
        return new Object() {
            public final LocalDateTime summaryStartDate = finalStartDate;
            public final LocalDateTime summaryEndDate = finalEndDate;
            public final BigDecimal summaryTotalRevenue = finalTotalRevenue;
            public final int totalTransactions = payments.size();
        };
    }
    
    private void calculateProcessingFee(Payment payment) {
        // Get processing fee percentage from payment method
        if (payment.getPaymentMethod() != null) {
            // This would be implemented based on payment method
            BigDecimal feePercentage = BigDecimal.valueOf(0.025); // 2.5% default
            BigDecimal processingFee = payment.getPaymentAmount().multiply(feePercentage);
            payment.setProcessingFee(processingFee);
        }
    }
    
    public List<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByDateRange(startDate, endDate);
    }
    
    public BigDecimal getTotalRevenueForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = getPaymentsByDateRange(startDate, endDate);
        return payments.stream()
                .filter(p -> "Completed".equals(p.getPaymentStatus()))
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 