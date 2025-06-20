package com.example.rentello.repository;

import com.example.rentello.entity.ViewAvailableVehicles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ViewAvailableVehiclesRepository extends JpaRepository<ViewAvailableVehicles, Integer> {
    
    @Query("SELECT v FROM ViewAvailableVehicles v WHERE " +
           "LOWER(v.vehicleRegistration) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.brandName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ViewAvailableVehicles> searchVehicles(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT v FROM ViewAvailableVehicles v WHERE v.categoryName = :categoryName")
    List<ViewAvailableVehicles> findByCategory(@Param("categoryName") String categoryName);
    
    @Query("SELECT v FROM ViewAvailableVehicles v WHERE v.cityName = :cityName")
    List<ViewAvailableVehicles> findByCity(@Param("cityName") String cityName);
    
    @Query("SELECT v FROM ViewAvailableVehicles v WHERE v.dailyRentalRate BETWEEN :minPrice AND :maxPrice")
    List<ViewAvailableVehicles> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT COUNT(v) FROM ViewAvailableVehicles v")
    Long countAvailableVehicles();
} 