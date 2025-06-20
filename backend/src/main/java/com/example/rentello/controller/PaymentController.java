package com.example.rentello.controller;

import com.example.rentello.entity.Payment;
import com.example.rentello.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Page<Payment>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer id) {
        return paymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<Payment>> getPaymentsByRental(@PathVariable Integer rentalId) {
        List<Payment> payments = paymentService.getPaymentsByRental(rentalId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Payment>> getPaymentsByCustomer(@PathVariable Integer customerId) {
        List<Payment> payments = paymentService.getPaymentsByCustomer(customerId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping
    public ResponseEntity<Payment> processPayment(@Valid @RequestBody Payment payment) {
        try {
            Payment processedPayment = paymentService.processPayment(payment);
            return ResponseEntity.ok(processedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Payment> updatePayment(@PathVariable Integer id, @Valid @RequestBody Payment payment) {
        return paymentService.findById(id)
                .map(existingPayment -> {
                    payment.setPaymentId(id);
                    return ResponseEntity.ok(paymentService.updatePayment(payment));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        return paymentService.findById(id)
                .map(payment -> {
                    paymentService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/revenue/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Object> getRevenueSummary(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        Object summary = paymentService.getRevenueSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/refund/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Payment> processRefund(
            @PathVariable Integer id,
            @RequestParam BigDecimal refundAmount,
            @RequestParam(required = false) String reason) {
        
        try {
            Payment refund = paymentService.processRefund(id, refundAmount, reason);
            return ResponseEntity.ok(refund);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 