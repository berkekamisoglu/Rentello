package com.example.rentello.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    
    private Integer locationId;
    private String locationName;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
    private CityDto city;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityDto {
        private Integer cityId;
        private String cityName;
        private String postalCode;
        private CountryDto country;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryDto {
        private Integer countryId;
        private String countryName;
        private String countryCode;
    }
} 