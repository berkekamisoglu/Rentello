package com.example.rentello.controller;

import com.example.rentello.service.PricingService;
import com.example.rentello.service.VehicleService;
import com.example.rentello.entity.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin(origins = "http://localhost:3000")
public class PricingController {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private VehicleService vehicleService;

    /**
     * Calculate dynamic price for a vehicle and date range
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculatePrice(@RequestBody PriceCalculationRequest request) {
        try {
            // Get vehicle to get base rate
            Vehicle vehicle = vehicleService.findById(request.getVehicleId()).orElse(null);
            if (vehicle == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vehicle not found"));
            }

            BigDecimal baseRate = vehicle.getDailyRentalRate();
            LocalDate startDate = request.getStartDate();
            LocalDate endDate = request.getEndDate();

            // Calculate dynamic price
            BigDecimal dynamicPrice = pricingService.calculateDynamicPrice(baseRate, startDate, endDate);
            
            // Get pricing breakdown
            PricingService.PricingBreakdown breakdown = pricingService.getPricingBreakdown(baseRate, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("vehicleId", request.getVehicleId());
            response.put("baseRate", baseRate);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalPrice", dynamicPrice);
            response.put("breakdown", breakdown);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pricing breakdown for display
     */
    @GetMapping("/breakdown")
    public ResponseEntity<PricingService.PricingBreakdown> getPricingBreakdown(
            @RequestParam Integer vehicleId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Vehicle vehicle = vehicleService.findById(vehicleId).orElse(null);
            if (vehicle == null) {
                return ResponseEntity.badRequest().build();
            }

            BigDecimal baseRate = vehicle.getDailyRentalRate();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            PricingService.PricingBreakdown breakdown = pricingService.getPricingBreakdown(baseRate, start, end);
            return ResponseEntity.ok(breakdown);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get pricing multipliers info
     */
    @GetMapping("/multipliers")
    public ResponseEntity<Map<String, Object>> getPricingMultipliers() {
        Map<String, Object> multipliers = new HashMap<>();
        
        Map<String, String> seasonal = new HashMap<>();
        seasonal.put("summer", "30% increase (June-August)");
        seasonal.put("winter", "10% decrease (December-February)");
        seasonal.put("springFall", "10% increase (March-May, September-November)");
        
        Map<String, String> special = new HashMap<>();
        special.put("weekend", "20% increase (Saturday-Sunday)");
        special.put("holiday", "50% increase (National holidays)");
        special.put("highDemand", "40% increase (Summer vacation, New Year, Spring break)");
        
        multipliers.put("seasonal", seasonal);
        multipliers.put("special", special);
        
        return ResponseEntity.ok(multipliers);
    }

    /**
     * Request DTO for price calculation
     */
    public static class PriceCalculationRequest {
        private Integer vehicleId;
        private LocalDate startDate;
        private LocalDate endDate;

        // Getters and Setters
        public Integer getVehicleId() { return vehicleId; }
        public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
} 