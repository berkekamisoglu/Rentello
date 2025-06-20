package com.example.rentello.repository;

import com.example.rentello.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    
    Optional<PaymentMethod> findByMethodName(String methodName);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.isActive = true")
    List<PaymentMethod> findActivePaymentMethods();
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.isActive = :isActive")
    List<PaymentMethod> findByIsActive(@Param("isActive") Boolean isActive);
} 