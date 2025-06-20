package com.example.rentello.repository;

import com.example.rentello.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Integer> {
    
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.brand.brandId = :brandId")
    List<VehicleModel> findByBrandId(@Param("brandId") Integer brandId);
    
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.category.categoryId = :categoryId")
    List<VehicleModel> findByCategoryId(@Param("categoryId") Integer categoryId);
}
