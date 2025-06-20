package com.example.rentello.repository;

import com.example.rentello.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {
    
    @Query("SELECT r FROM Rental r WHERE r.customer.userId = :customerId")
    List<Rental> findByCustomerId(@Param("customerId") Integer customerId);
    
    @Query("SELECT r FROM Rental r WHERE r.vehicle.vehicleId = :vehicleId")
    List<Rental> findByVehicleId(@Param("vehicleId") Integer vehicleId);
    
    @Query("SELECT r FROM Rental r WHERE r.rentalStatus.statusName = :statusName")
    List<Rental> findByStatusName(@Param("statusName") String statusName);
    
    @Query("SELECT r FROM Rental r WHERE " +
           "r.plannedPickupDate BETWEEN :startDate AND :endDate")
    List<Rental> findByPickupDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT r FROM Rental r WHERE " +
           "r.plannedReturnDate BETWEEN :startDate AND :endDate")
    List<Rental> findByReturnDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT r FROM Rental r WHERE " +
           "r.vehicle.vehicleId = :vehicleId AND " +
           "r.rentalStatus.statusName IN ('Active', 'Reserved') AND " +
           "((:pickupDate BETWEEN r.plannedPickupDate AND r.plannedReturnDate) OR " +
           "(:returnDate BETWEEN r.plannedPickupDate AND r.plannedReturnDate) OR " +
           "(r.plannedPickupDate BETWEEN :pickupDate AND :returnDate))")
    List<Rental> findConflictingRentals(
        @Param("vehicleId") Integer vehicleId,
        @Param("pickupDate") LocalDateTime pickupDate,
        @Param("returnDate") LocalDateTime returnDate
    );
    
    @Query("SELECT r FROM Rental r WHERE " +
           "r.actualReturnDate IS NULL AND " +
           "r.plannedReturnDate < :currentDate")
    List<Rental> findOverdueRentals(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT r FROM Rental r WHERE " +
           "r.pickupLocation.locationId = :locationId OR " +
           "r.returnLocation.locationId = :locationId")
    List<Rental> findByLocationId(@Param("locationId") Integer locationId);
    
    // Admin Service için eklenen metodlar
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.rentalStatus.statusName = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE DATE(r.plannedPickupDate) = :date")
    Integer countByDate(@Param("date") java.time.LocalDate date);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.customer = :customer")
    Long countByCustomer(@Param("customer") com.example.rentello.entity.User customer);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.customer = :customer AND r.rentalStatus.statusName = :status")
    Long countByCustomerAndStatus(@Param("customer") com.example.rentello.entity.User customer, @Param("status") String status);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.vehicle = :vehicle")
    Long countByVehicle(@Param("vehicle") com.example.rentello.entity.Vehicle vehicle);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.vehicle = :vehicle AND r.rentalStatus.statusName = :status")
    Long countByVehicleAndStatus(@Param("vehicle") com.example.rentello.entity.Vehicle vehicle, @Param("status") String status);
    
    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Rental r WHERE r.vehicle.vehicleId = :vehicleId")
    java.math.BigDecimal getTotalRevenueByVehicle(@Param("vehicleId") Integer vehicleId);

    // Admin search method
    @Query("SELECT r FROM Rental r WHERE " +
           "LOWER(CONCAT(r.customer.firstName, ' ', r.customer.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.vehicle.vehicleRegistration) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.rentalStatus.statusName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Rental> findByCustomerNameOrVehiclePlateContaining(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Direct SQL update for rental status
    @Modifying
    @Query(value = "UPDATE Rentals SET RentalStatusID = :statusId WHERE RentalID = :rentalId", nativeQuery = true)
    int updateRentalStatusDirect(@Param("rentalId") Integer rentalId, @Param("statusId") Integer statusId);
    
    // Bu ay tamamlanan kiralamaları bul
    @Query("SELECT r FROM Rental r WHERE " +
           "r.rentalStatus.statusName IN ('Tamamlandi', 'Odendi') AND " +
           "r.updatedDate BETWEEN :startDate AND :endDate")
    List<Rental> findCompletedRentalsInMonth(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}
