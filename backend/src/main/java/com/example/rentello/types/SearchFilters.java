package com.example.rentello.types;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilters {
    private Integer categoryId;
    private Integer brandId;
    private Integer locationId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private String searchTerm;
} 