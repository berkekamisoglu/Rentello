package com.example.rentello.repository;

import com.example.rentello.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    
    Optional<Vehicle> findByVehicleRegistration(String vehicleRegistration);
    
    @Query("SELECT v FROM Vehicle v WHERE v.currentStatus.isAvailableForRent = true")
    List<Vehicle> findAvailableVehicles();
    
    @Query("SELECT v FROM Vehicle v WHERE v.model.category.categoryId = :categoryId")
    List<Vehicle> findByCategoryId(@Param("categoryId") Integer categoryId);
    
    @Query("SELECT v FROM Vehicle v WHERE v.model.brand.brandId = :brandId")
    List<Vehicle> findByBrandId(@Param("brandId") Integer brandId);
    
    @Query("SELECT v FROM Vehicle v WHERE v.currentLocation.locationId = :locationId")
    List<Vehicle> findByLocationId(@Param("locationId") Integer locationId);
    
    @Query("SELECT v FROM Vehicle v WHERE v.dailyRentalRate BETWEEN :minPrice AND :maxPrice")
    List<Vehicle> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT v FROM Vehicle v WHERE " +
           "v.currentStatus.isAvailableForRent = true AND " +
           "v.model.category.categoryId = :categoryId AND " +
           "v.currentLocation.locationId = :locationId")
    List<Vehicle> findAvailableVehiclesByCategoryAndLocation(
        @Param("categoryId") Integer categoryId, 
        @Param("locationId") Integer locationId
    );
    
    @Query("SELECT v FROM Vehicle v WHERE " +
           "LOWER(v.vehicleDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.model.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.model.brand.brandName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Vehicle> searchVehicles(@Param("searchTerm") String searchTerm);
    
    boolean existsByVehicleRegistration(String vehicleRegistration);
    
    // Admin Service için eklenen metodlar
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.currentStatus.statusName = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT c.categoryName, COUNT(v) FROM Vehicle v JOIN v.model.category c GROUP BY c.categoryName")
    java.util.Map<String, Long> getPopularCategories();
    
    @Query("SELECT v FROM Vehicle v WHERE v.vehicleRegistration LIKE %:searchTerm% OR v.vehicleDescription LIKE %:searchTerm%")
    org.springframework.data.domain.Page<Vehicle> findByVehicleRegistrationContainingOrVehicleDescriptionContaining(
        @Param("searchTerm") String searchTerm, org.springframework.data.domain.Pageable pageable);
}
