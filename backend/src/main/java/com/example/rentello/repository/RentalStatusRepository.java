package com.example.rentello.repository;

import com.example.rentello.entity.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalStatusRepository extends JpaRepository<RentalStatus, Integer> {
    
    Optional<RentalStatus> findByStatusName(String statusName);
    
    @Query("SELECT rs FROM RentalStatus rs ORDER BY rs.statusId")
    List<RentalStatus> findAllOrderByStatusId();
    
    @Query("SELECT rs FROM RentalStatus rs WHERE rs.statusName IN ('Rezerve Edildi', 'Aktif', 'Tamamlandi', 'Odendi')")
    List<RentalStatus> findActiveStatuses();
} 