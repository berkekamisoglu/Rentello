package com.example.rentello.repository;

import com.example.rentello.entity.view.AvailableVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AvailableVehicleRepository extends JpaRepository<AvailableVehicle, Integer> {

    /**
     * Şehir adına göre mevcut araçları getir
     */
    List<AvailableVehicle> findByCityName(String cityName);

    /**
     * Kategori adına göre mevcut araçları getir
     */
    List<AvailableVehicle> findByCategoryName(String categoryName);

    /**
     * Marka adına göre mevcut araçları getir
     */
    List<AvailableVehicle> findByBrandName(String brandName);

    /**
     * Günlük kiralama ücreti aralığında araçları getir
     */
    List<AvailableVehicle> findByDailyRentalRateBetween(BigDecimal minRate, BigDecimal maxRate);

    /**
     * Belirli kapasitede araçları getir
     */
    List<AvailableVehicle> findBySeatingCapacity(Integer seatingCapacity);

    /**
     * Belirli yakıt tipinde araçları getir
     */
    List<AvailableVehicle> findByFuelType(String fuelType);

    /**
     * Belirli şanzıman tipinde araçları getir
     */
    List<AvailableVehicle> findByTransmissionType(String transmissionType);

    /**
     * Maksimum kilometre sınırı ile araçları getir
     */
    List<AvailableVehicle> findByMileageLessThanEqual(Integer maxMileage);

    /**
     * Lokasyon adına göre araçları getir
     */
    List<AvailableVehicle> findByLocationName(String locationName);

    /**
     * Şehir ve kategori kombinasyonu ile araçları getir
     */
    List<AvailableVehicle> findByCityNameAndCategoryName(String cityName, String categoryName);

    /**
     * Gelişmiş arama - birden fazla kriter
     */
    @Query("SELECT av FROM AvailableVehicle av WHERE " +
           "(:cityName IS NULL OR av.cityName = :cityName) AND " +
           "(:categoryName IS NULL OR av.categoryName = :categoryName) AND " +
           "(:brandName IS NULL OR av.brandName = :brandName) AND " +
           "(:maxRate IS NULL OR av.dailyRentalRate <= :maxRate) AND " +
           "(:fuelType IS NULL OR av.fuelType = :fuelType) AND " +
           "(:transmissionType IS NULL OR av.transmissionType = :transmissionType) " +
           "ORDER BY av.dailyRentalRate ASC")
    List<AvailableVehicle> findWithFilters(@Param("cityName") String cityName,
                                          @Param("categoryName") String categoryName,
                                          @Param("brandName") String brandName,
                                          @Param("maxRate") BigDecimal maxRate,
                                          @Param("fuelType") String fuelType,
                                          @Param("transmissionType") String transmissionType);

    /**
     * En ucuz araçları getir
     */
    @Query("SELECT av FROM AvailableVehicle av ORDER BY av.dailyRentalRate ASC")
    List<AvailableVehicle> findCheapestVehicles();

    /**
     * En yeni araçları getir
     */
    @Query("SELECT av FROM AvailableVehicle av ORDER BY av.manufactureYear DESC")
    List<AvailableVehicle> findNewestVehicles();

    /**
     * En az kilometreli araçları getir
     */
    @Query("SELECT av FROM AvailableVehicle av ORDER BY av.mileage ASC")
    List<AvailableVehicle> findLowMileageVehicles();
} 