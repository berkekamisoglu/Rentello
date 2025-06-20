package com.example.rentello.repository;

import com.example.rentello.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    
    @Query("SELECT c FROM City c WHERE c.country.countryId = :countryId")
    List<City> findByCountryId(@Param("countryId") Integer countryId);
    
    @Query("SELECT c FROM City c WHERE LOWER(c.cityName) LIKE LOWER(CONCAT('%', :cityName, '%'))")
    List<City> findByCityNameContaining(@Param("cityName") String cityName);
}
