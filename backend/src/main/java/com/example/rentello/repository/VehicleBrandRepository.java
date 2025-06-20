package com.example.rentello.repository;

import com.example.rentello.entity.VehicleBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Integer> {
    Optional<VehicleBrand> findByBrandName(String brandName);
    boolean existsByBrandName(String brandName);
}
