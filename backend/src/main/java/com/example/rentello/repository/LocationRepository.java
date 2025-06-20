package com.example.rentello.repository;

import com.example.rentello.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    
    @Query("SELECT l FROM Location l WHERE l.isActive = true")
    List<Location> findActiveLocations();
    
    @Query("SELECT l FROM Location l WHERE l.isActive = true")
    List<Location> findByIsActiveTrue();
    
    @Query("SELECT l FROM Location l WHERE l.city.cityId = :cityId")
    List<Location> findByCityId(@Param("cityId") Integer cityId);
    
    @Query("SELECT l FROM Location l WHERE l.manager.userId = :managerId")
    List<Location> findByManagerId(@Param("managerId") Integer managerId);
    
    @Query("SELECT l FROM Location l WHERE " +
           "LOWER(l.locationName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.city.cityName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Location> searchLocations(@Param("searchTerm") String searchTerm);
    
    // Admin Service için eklenen metodlar
    Long countByIsActive(Boolean isActive);
    
    List<Location> findByIsActive(Boolean isActive);
}
