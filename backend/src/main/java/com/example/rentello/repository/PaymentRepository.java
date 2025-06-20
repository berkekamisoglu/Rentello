package com.example.rentello.repository;

import com.example.rentello.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    @Query("SELECT p FROM Payment p WHERE p.rental.rentalId = :rentalId")
    List<Payment> findByRentalId(@Param("rentalId") Integer rentalId);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status")
    List<Payment> findByPaymentStatus(@Param("status") String status);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPaymentDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT p FROM Payment p WHERE p.rental.customer.userId = :customerId")
    List<Payment> findByCustomerId(@Param("customerId") Integer customerId);
    
    // Admin Service için eklenen metodlar
    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM Payment p WHERE p.paymentStatus = 'Completed'")
    java.math.BigDecimal getTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM Payment p WHERE p.paymentStatus = 'Completed' AND p.paymentDate >= :startDate")
    java.math.BigDecimal getMonthlyRevenue(@Param("startDate") java.time.LocalDate startDate);
    
    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM Payment p WHERE p.paymentStatus = 'Completed' AND DATE(p.paymentDate) = :date")
    java.math.BigDecimal getDailyRevenue(@Param("date") java.time.LocalDate date);
}
