package com.example.rentello.repository;

import com.example.rentello.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    Optional<Country> findByCountryName(String countryName);
    Optional<Country> findByCountryCode(String countryCode);
    boolean existsByCountryName(String countryName);
    boolean existsByCountryCode(String countryCode);
}
