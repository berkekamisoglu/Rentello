package com.example.rentello.repository;

import com.example.rentello.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleStatusRepository extends JpaRepository<VehicleStatus, Integer> {
    
    List<VehicleStatus> findByIsAvailableForRent(Boolean isAvailableForRent);
    
    VehicleStatus findByStatusName(String statusName);
} 