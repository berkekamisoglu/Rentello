package com.example.rentello.repository;

import com.example.rentello.entity.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Integer> {
    Optional<VehicleCategory> findByCategoryName(String categoryName);
    boolean existsByCategoryName(String categoryName);
}
