package com.example.rentello.controller;

import com.example.rentello.entity.view.AvailableVehicle;
import com.example.rentello.service.DatabaseIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/database-integration")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DatabaseIntegrationController {

    private final DatabaseIntegrationService databaseService;

    // ===== STORED PROCEDURE ENDPOINTS =====

    /**
     * Kullanıcı kimlik doğrulama
     */
    @PostMapping("/auth/authenticate")
    public ResponseEntity<Map<String, Object>> authenticateUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        Map<String, Object> result = databaseService.authenticateUser(username, password);
        return ResponseEntity.ok(result);
    }

    /**
     * Yeni kiralama oluştur
     */
    @PostMapping("/rentals/create")
    public ResponseEntity<Map<String, Object>> createRental(@RequestBody Map<String, Object> rentalData) {
        Integer customerId = (Integer) rentalData.get("customerId");
        Integer vehicleId = (Integer) rentalData.get("vehicleId");
        Integer pickupLocationId = (Integer) rentalData.get("pickupLocationId");
        Integer returnLocationId = (Integer) rentalData.get("returnLocationId");
        LocalDateTime plannedPickupDate = LocalDateTime.parse((String) rentalData.get("plannedPickupDate"));
        LocalDateTime plannedReturnDate = LocalDateTime.parse((String) rentalData.get("plannedReturnDate"));
        Integer createdBy = (Integer) rentalData.get("createdBy");

        Map<String, Object> result = databaseService.createRental(
                customerId, vehicleId, pickupLocationId, returnLocationId,
                plannedPickupDate, plannedReturnDate, createdBy);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Araç iade işlemi
     */
    @PostMapping("/rentals/{rentalId}/return")
    public ResponseEntity<Map<String, Object>> processVehicleReturn(
            @PathVariable Integer rentalId,
            @RequestBody Map<String, Object> returnData) {
        
        LocalDateTime actualReturnDate = LocalDateTime.parse((String) returnData.get("actualReturnDate"));
        Integer mileage = (Integer) returnData.get("mileage");
        String damageNotes = (String) returnData.get("damageNotes");
        Integer processedBy = (Integer) returnData.get("processedBy");

        Map<String, Object> result = databaseService.processVehicleReturn(
                rentalId, actualReturnDate, mileage, damageNotes, processedBy);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Müşteri kaydı
     */
    @PostMapping("/customers/register")
    public ResponseEntity<Map<String, Object>> registerCustomer(@RequestBody Map<String, String> customerData) {
        Map<String, Object> result = databaseService.registerCustomer(
                customerData.get("username"),
                customerData.get("email"),
                customerData.get("passwordHash"),
                customerData.get("firstName"),
                customerData.get("lastName"),
                customerData.get("phoneNumber"),
                customerData.get("dateOfBirth"),
                customerData.get("nationalId"),
                Integer.parseInt(customerData.get("cityId")),
                customerData.get("address")
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * Ödeme işlemi
     */
    @PostMapping("/payments/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> paymentData) {
        Integer rentalId = (Integer) paymentData.get("rentalId");
        Integer paymentMethodId = (Integer) paymentData.get("paymentMethodId");
        BigDecimal paymentAmount = new BigDecimal(paymentData.get("paymentAmount").toString());
        String transactionReference = (String) paymentData.get("transactionReference");
        Integer processedBy = (Integer) paymentData.get("processedBy");

        Map<String, Object> result = databaseService.processPayment(
                rentalId, paymentMethodId, paymentAmount, transactionReference, processedBy);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Araç durumu güncelleme
     */
    @PutMapping("/vehicles/{vehicleId}/status")
    public ResponseEntity<Map<String, Object>> updateVehicleStatus(
            @PathVariable Integer vehicleId,
            @RequestBody Map<String, Object> statusData) {
        
        Integer newStatusId = (Integer) statusData.get("newStatusId");
        String notes = (String) statusData.get("notes");
        Integer updatedBy = (Integer) statusData.get("updatedBy");

        Map<String, Object> result = databaseService.updateVehicleStatus(
                vehicleId, newStatusId, notes, updatedBy);
        
        return ResponseEntity.ok(result);
    }

    // ===== STORED PROCEDURE ENDPOINTS =====

    /**
     * Stored Procedure: Kiralama oluşturma
     */
    @PostMapping("/stored-procedures/create-rental")
    public ResponseEntity<Map<String, Object>> createRentalStoredProcedure(@RequestBody Map<String, Object> rentalData) {
        Integer customerId = (Integer) rentalData.get("customerId");
        Integer vehicleId = (Integer) rentalData.get("vehicleId");
        Integer pickupLocationId = (Integer) rentalData.get("pickupLocationId");
        Integer returnLocationId = (Integer) rentalData.get("returnLocationId");
        LocalDateTime plannedPickupDate = LocalDateTime.parse((String) rentalData.get("plannedPickupDate"));
        LocalDateTime plannedReturnDate = LocalDateTime.parse((String) rentalData.get("plannedReturnDate"));
        Integer createdBy = (Integer) rentalData.get("createdBy");

        Map<String, Object> result = databaseService.createRental(
                customerId, vehicleId, pickupLocationId, returnLocationId,
                plannedPickupDate, plannedReturnDate, createdBy);
        
        return ResponseEntity.ok(result);
    }

    // ===== FUNCTION ENDPOINTS =====

    /**
     * Yaş hesaplama
     */
    @GetMapping("/functions/calculate-age")
    public ResponseEntity<Integer> calculateAge(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth) {
        Integer age = databaseService.calculateAge(dateOfBirth);
        return ResponseEntity.ok(age);
    }

    /**
     * Kiralama süresi hesaplama
     */
    @GetMapping("/functions/calculate-rental-duration")
    public ResponseEntity<Integer> calculateRentalDuration(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Integer duration = databaseService.calculateRentalDuration(startDate, endDate);
        return ResponseEntity.ok(duration);
    }

    /**
     * Geç ücret hesaplama
     */
    @GetMapping("/functions/calculate-late-fee")
    public ResponseEntity<BigDecimal> calculateLateFee(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime plannedReturnDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime actualReturnDate,
            @RequestParam BigDecimal dailyRate) {
        BigDecimal lateFee = databaseService.calculateLateFee(plannedReturnDate, actualReturnDate, dailyRate);
        return ResponseEntity.ok(lateFee);
    }

    /**
     * Müşteri sadakat seviyesi
     */
    @GetMapping("/functions/customer-loyalty-tier/{customerId}")
    public ResponseEntity<String> getCustomerLoyaltyTier(@PathVariable Integer customerId) {
        String loyaltyTier = databaseService.getCustomerLoyaltyTier(customerId);
        return ResponseEntity.ok(loyaltyTier);
    }

    /**
     * İndirim yüzdesi hesaplama
     */
    @GetMapping("/functions/calculate-discount")
    public ResponseEntity<BigDecimal> calculateDiscountPercentage(
            @RequestParam Integer customerId,
            @RequestParam Integer rentalDays) {
        BigDecimal discount = databaseService.calculateDiscountPercentage(customerId, rentalDays);
        return ResponseEntity.ok(discount);
    }

    /**
     * Araç müsaitlik kontrolü
     */
    @GetMapping("/functions/is-vehicle-available")
    public ResponseEntity<Boolean> isVehicleAvailable(
            @RequestParam Integer vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Boolean isAvailable = databaseService.isVehicleAvailable(vehicleId, startDate, endDate);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Araç kullanım oranı
     */
    @GetMapping("/functions/vehicle-utilization/{vehicleId}")
    public ResponseEntity<BigDecimal> getVehicleUtilizationRate(
            @PathVariable Integer vehicleId,
            @RequestParam(defaultValue = "30") Integer days) {
        BigDecimal utilizationRate = databaseService.calculateVehicleUtilizationRate(vehicleId, days);
        return ResponseEntity.ok(utilizationRate);
    }

    /**
     * Para formatı
     */
    @GetMapping("/functions/format-currency")
    public ResponseEntity<String> formatCurrency(@RequestParam BigDecimal amount) {
        String formattedAmount = databaseService.formatCurrency(amount);
        return ResponseEntity.ok(formattedAmount);
    }

    // ===== TABLE-VALUED FUNCTION ENDPOINTS =====

    /**
     * Müşteri kiralama geçmişi
     */
    @GetMapping("/functions/customer-rental-history/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getCustomerRentalHistory(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> history = databaseService.getCustomerRentalHistory(customerId, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Araç bakım geçmişi
     */
    @GetMapping("/functions/vehicle-maintenance-history/{vehicleId}")
    public ResponseEntity<List<Map<String, Object>>> getVehicleMaintenanceHistory(@PathVariable Integer vehicleId) {
        List<Map<String, Object>> history = databaseService.getVehicleMaintenanceHistory(vehicleId);
        return ResponseEntity.ok(history);
    }

    /**
     * Gelir raporu
     */
    @GetMapping("/functions/revenue-report")
    public ResponseEntity<List<Map<String, Object>>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> report = databaseService.getRevenueReportByPeriod(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Gecikmiş kiralamalar
     */
    @GetMapping("/functions/overdue-rentals")
    public ResponseEntity<List<Map<String, Object>>> getOverdueRentals() {
        List<Map<String, Object>> overdueRentals = databaseService.getOverdueRentals();
        return ResponseEntity.ok(overdueRentals);
    }

    /**
     * Popüler araçlar
     */
    @GetMapping("/functions/popular-vehicles")
    public ResponseEntity<List<Map<String, Object>>> getPopularVehicles(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> popularVehicles = databaseService.getPopularVehicles(limit);
        return ResponseEntity.ok(popularVehicles);
    }

    // ===== VIEW ENDPOINTS =====

    /**
     * Mevcut araçları arama
     */
    @GetMapping("/views/available-vehicles/search")
    public ResponseEntity<List<AvailableVehicle>> searchAvailableVehicles(
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmissionType) {
        
        List<AvailableVehicle> vehicles = databaseService.searchAvailableVehicles(
                cityName, categoryName, brandName, maxRate, fuelType, transmissionType);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Şehir adına göre mevcut araçlar
     */
    @GetMapping("/views/available-vehicles/by-city/{cityName}")
    public ResponseEntity<List<AvailableVehicle>> getAvailableVehiclesByCity(@PathVariable String cityName) {
        List<AvailableVehicle> vehicles = databaseService.getAvailableVehiclesByCity(cityName);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Kategori adına göre mevcut araçlar
     */
    @GetMapping("/views/available-vehicles/by-category/{categoryName}")
    public ResponseEntity<List<AvailableVehicle>> getAvailableVehiclesByCategory(@PathVariable String categoryName) {
        List<AvailableVehicle> vehicles = databaseService.getAvailableVehiclesByCategory(categoryName);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * En ucuz araçlar
     */
    @GetMapping("/views/available-vehicles/cheapest")
    public ResponseEntity<List<AvailableVehicle>> getCheapestVehicles() {
        List<AvailableVehicle> vehicles = databaseService.getCheapestVehicles();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * En yeni araçlar
     */
    @GetMapping("/views/available-vehicles/newest")
    public ResponseEntity<List<AvailableVehicle>> getNewestVehicles() {
        List<AvailableVehicle> vehicles = databaseService.getNewestVehicles();
        return ResponseEntity.ok(vehicles);
    }

    // ===== COMPOSITE ENDPOINTS =====

    /**
     * Kiralama için kapsamlı araç arama
     */
    @GetMapping("/composite/search-vehicles-for-rental")
    public ResponseEntity<List<AvailableVehicle>> searchVehiclesForRental(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime returnDate,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) BigDecimal maxRate) {
        
        List<AvailableVehicle> vehicles = databaseService.searchVehiclesForRental(
                pickupDate, returnDate, cityName, categoryName, maxRate);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Müşteri için kişiselleştirilmiş araç önerileri
     */
    @GetMapping("/composite/personalized-recommendations/{customerId}")
    public ResponseEntity<List<AvailableVehicle>> getPersonalizedVehicleRecommendations(
            @PathVariable Integer customerId,
            @RequestParam String cityName,
            @RequestParam Integer rentalDays) {
        
        List<AvailableVehicle> recommendations = databaseService.getPersonalizedVehicleRecommendations(
                customerId, cityName, rentalDays);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Kapsamlı araç arama - Full-text search desteği ile
     */
    @GetMapping("/composite/search-vehicles-comprehensive")
    public ResponseEntity<List<AvailableVehicle>> searchVehiclesComprehensive(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "true") Boolean includeDescription,
            @RequestParam(defaultValue = "true") Boolean includeBrand,
            @RequestParam(defaultValue = "true") Boolean includeModel,
            @RequestParam(defaultValue = "true") Boolean includeCategory) {
        
        try {
            // Use database service's search functionality
            List<AvailableVehicle> results = databaseService.searchAvailableVehicles(
                    null, null, null, null, null, null);
            
            // Filter by search term
            List<AvailableVehicle> filteredResults = results.stream()
                    .filter(vehicle -> matchesSearchTerm(vehicle, searchTerm))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(filteredResults);
        } catch (Exception e) {
            log.error("Error in comprehensive vehicle search", e);
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    private boolean matchesSearchTerm(AvailableVehicle vehicle, String searchTerm) {
        String searchLower = searchTerm.toLowerCase();
        return (vehicle.getBrandName() != null && vehicle.getBrandName().toLowerCase().contains(searchLower)) ||
               (vehicle.getModelName() != null && vehicle.getModelName().toLowerCase().contains(searchLower)) ||
               (vehicle.getCategoryName() != null && vehicle.getCategoryName().toLowerCase().contains(searchLower)) ||
               (vehicle.getVehicleRegistration() != null && vehicle.getVehicleRegistration().toLowerCase().contains(searchLower)) ||
               (vehicle.getColor() != null && vehicle.getColor().toLowerCase().contains(searchLower)) ||
               (vehicle.getFuelType() != null && vehicle.getFuelType().toLowerCase().contains(searchLower)) ||
               (vehicle.getTransmissionType() != null && vehicle.getTransmissionType().toLowerCase().contains(searchLower));
    }

    /**
     * Debug: Eski kiralama kayıtlarını temizle
     */
    @PostMapping("/debug/cleanup-old-rentals")
    public ResponseEntity<Map<String, Object>> cleanupOldRentals() {
        Map<String, Object> result = databaseService.cleanupOldRentals();
        return ResponseEntity.ok(result);
    }

    /**
     * Debug: Araç kiralama durumunu kontrol et
     */
    @GetMapping("/debug/vehicle-rental-status/{vehicleId}")
    public ResponseEntity<Map<String, Object>> getVehicleRentalStatus(@PathVariable Integer vehicleId) {
        Map<String, Object> result = databaseService.getVehicleRentalDebugInfo(vehicleId);
        return ResponseEntity.ok(result);
    }
} 