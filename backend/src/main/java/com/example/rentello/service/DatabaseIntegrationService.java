package com.example.rentello.service;

import com.example.rentello.entity.view.AvailableVehicle;
import com.example.rentello.repository.AvailableVehicleRepository;
import com.example.rentello.repository.DatabaseFunctionRepository;
import com.example.rentello.repository.StoredProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DatabaseIntegrationService {

    private final StoredProcedureRepository storedProcedureRepository;
    private final DatabaseFunctionRepository functionRepository;
    private final AvailableVehicleRepository availableVehicleRepository;

    // ===== STORED PROCEDURE İŞLEMLERİ =====

    /**
     * Kullanıcı kimlik doğrulama
     */
    public Map<String, Object> authenticateUser(String username, String password) {
        log.info("Authenticating user: {}", username);
        return storedProcedureRepository.authenticateUser(username, password);
    }

    /**
     * Yeni kiralama oluştur
     */
    public Map<String, Object> createRental(Integer customerId, Integer vehicleId,
                                          Integer pickupLocationId, Integer returnLocationId,
                                          LocalDateTime plannedPickupDate, LocalDateTime plannedReturnDate,
                                          Integer createdBy) {
        log.info("Creating rental for customer: {}, vehicle: {}", customerId, vehicleId);
        
        // Önce araç müsaitliğini kontrol et
        boolean isAvailable = isVehicleAvailable(vehicleId, plannedPickupDate, plannedReturnDate);
        if (!isAvailable) {
            log.warn("Vehicle {} is not available for the requested dates", vehicleId);
            return Map.of("IsSuccess", false, "ErrorMessage", "Araç seçilen tarihlerde müsait değil");
        }

        return storedProcedureRepository.createRental(
                customerId, vehicleId, pickupLocationId, returnLocationId,
                plannedPickupDate, plannedReturnDate, createdBy);
    }

    /**
     * Araç iade işlemi
     */
    public Map<String, Object> processVehicleReturn(Integer rentalId, LocalDateTime actualReturnDate,
                                                   Integer mileage, String damageNotes, Integer processedBy) {
        log.info("Processing vehicle return for rental: {}", rentalId);
        
        return storedProcedureRepository.processVehicleReturn(
                rentalId, actualReturnDate, mileage, damageNotes, processedBy);
    }

    /**
     * Müşteri kaydı
     */
    public Map<String, Object> registerCustomer(String username, String email, String passwordHash,
                                               String firstName, String lastName, String phoneNumber,
                                               String dateOfBirth, String nationalId, Integer cityId, String address) {
        log.info("Registering new customer: {}", username);
        
        return storedProcedureRepository.registerCustomer(
                username, email, passwordHash, firstName, lastName, phoneNumber,
                dateOfBirth, nationalId, cityId, address);
    }

    /**
     * Ödeme işlemi
     */
    public Map<String, Object> processPayment(Integer rentalId, Integer paymentMethodId,
                                            BigDecimal paymentAmount, String transactionReference,
                                            Integer processedBy) {
        log.info("Processing payment for rental: {}, amount: {}", rentalId, paymentAmount);
        
        return storedProcedureRepository.processPayment(
                rentalId, paymentMethodId, paymentAmount, transactionReference, processedBy);
    }

    /**
     * Araç durumu güncelleme
     */
    public Map<String, Object> updateVehicleStatus(Integer vehicleId, Integer newStatusId,
                                                  String notes, Integer updatedBy) {
        log.info("Updating vehicle status - vehicle: {}, new status: {}", vehicleId, newStatusId);
        
        return storedProcedureRepository.updateVehicleStatus(vehicleId, newStatusId, notes, updatedBy);
    }

    // ===== DATABASE FUNCTION İŞLEMLERİ =====

    /**
     * Yaş hesaplama
     */
    @Transactional(readOnly = true)
    public Integer calculateAge(LocalDate dateOfBirth) {
        return functionRepository.calculateAge(dateOfBirth);
    }

    /**
     * Kiralama süresi hesaplama
     */
    @Transactional(readOnly = true)
    public Integer calculateRentalDuration(LocalDateTime startDate, LocalDateTime endDate) {
        return functionRepository.calculateRentalDuration(startDate, endDate);
    }

    /**
     * Geç ücret hesaplama
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateLateFee(LocalDateTime plannedReturnDate,
                                      LocalDateTime actualReturnDate,
                                      BigDecimal dailyRate) {
        return functionRepository.calculateLateFee(plannedReturnDate, actualReturnDate, dailyRate);
    }

    /**
     * Müşteri sadakat seviyesi
     */
    @Transactional(readOnly = true)
    public String getCustomerLoyaltyTier(Integer customerId) {
        return functionRepository.getCustomerLoyaltyTier(customerId);
    }

    /**
     * İndirim yüzdesi hesaplama
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscountPercentage(Integer customerId, Integer rentalDays) {
        log.info("Calculating discount for customer: {} with {} rental days", customerId, rentalDays);
        return functionRepository.calculateDiscountPercentage(customerId, rentalDays);
    }

    /**
     * Araç müsaitlik kontrolü
     */
    @Transactional(readOnly = true)
    public boolean isVehicleAvailable(Integer vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Checking availability for vehicle ID: {} from {} to {}", vehicleId, startDate, endDate);
        return functionRepository.isVehicleAvailable(vehicleId, startDate, endDate);
    }

    /**
     * Araç kullanım oranı hesaplama
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateVehicleUtilizationRate(Integer vehicleId, Integer days) {
        return functionRepository.calculateVehicleUtilizationRate(vehicleId, days);
    }

    /**
     * Para formatı
     */
    @Transactional(readOnly = true)
    public String formatCurrency(BigDecimal amount) {
        return functionRepository.formatCurrency(amount);
    }

    // ===== TABLE-VALUED FUNCTION İŞLEMLERİ =====

    /**
     * Belirli tarih aralığında mevcut araçlar
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableVehiclesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting available vehicles from {} to {}", startDate, endDate);
        return functionRepository.getAvailableVehiclesByDateRange(startDate, endDate);
    }

    /**
     * Müşteri kiralama geçmişi
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCustomerRentalHistory(Integer customerId, Integer limit) {
        log.info("Getting rental history for customer: {}", customerId);
        return functionRepository.getCustomerRentalHistory(customerId, limit);
    }

    /**
     * Araç bakım geçmişi
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVehicleMaintenanceHistory(Integer vehicleId) {
        log.info("Getting maintenance history for vehicle: {}", vehicleId);
        return functionRepository.getVehicleMaintenanceHistory(vehicleId);
    }

    /**
     * Gelir raporu - tarih aralığına göre
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRevenueReportByPeriod(LocalDate startDate, LocalDate endDate) {
        log.info("Getting revenue report from {} to {}", startDate, endDate);
        return functionRepository.getRevenueReportByPeriod(startDate, endDate);
    }

    /**
     * Gecikmiş kiralamalar
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOverdueRentals() {
        log.info("Getting overdue rentals");
        return functionRepository.getOverdueRentals();
    }

    /**
     * Popüler araçlar
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopularVehicles(Integer limit) {
        log.info("Getting popular vehicles, limit: {}", limit);
        return functionRepository.getPopularVehicles(limit);
    }

    // ===== VIEW İŞLEMLERİ =====

    /**
     * Mevcut araçları filtreleyerek arama
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> searchAvailableVehicles(String cityName, String categoryName,
                                                         String brandName, BigDecimal maxRate,
                                                         String fuelType, String transmissionType) {
        log.info("Searching available vehicles with filters - city: {}, category: {}, brand: {}", 
                cityName, categoryName, brandName);
        
        return availableVehicleRepository.findWithFilters(
                cityName, categoryName, brandName, maxRate, fuelType, transmissionType);
    }

    /**
     * Şehir adına göre mevcut araçlar
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> getAvailableVehiclesByCity(String cityName) {
        return availableVehicleRepository.findByCityName(cityName);
    }

    /**
     * Kategori adına göre mevcut araçlar
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> getAvailableVehiclesByCategory(String categoryName) {
        return availableVehicleRepository.findByCategoryName(categoryName);
    }

    /**
     * En ucuz araçlar
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> getCheapestVehicles() {
        return availableVehicleRepository.findCheapestVehicles();
    }

    /**
     * En yeni araçlar
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> getNewestVehicles() {
        return availableVehicleRepository.findNewestVehicles();
    }

    // ===== COMPOSITE İŞLEMLER =====

    /**
     * Kiralama için kapsamlı araç arama
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> searchVehiclesForRental(LocalDateTime pickupDate,
                                                         LocalDateTime returnDate,
                                                         String cityName,
                                                         String categoryName,
                                                         BigDecimal maxRate) {
        // Önce view'dan filtreleme yap
        List<AvailableVehicle> filteredVehicles = searchAvailableVehicles(
                cityName, categoryName, null, maxRate, null, null);
        
        // Sonra her araç için tarih müsaitliğini kontrol et
        return filteredVehicles.stream()
                .filter(vehicle -> isVehicleAvailable(vehicle.getVehicleId(), pickupDate, returnDate))
                .toList();
    }

    /**
     * Müşteri için kişiselleştirilmiş araç önerileri
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> getPersonalizedVehicleRecommendations(Integer customerId,
                                                                       String cityName,
                                                                       Integer rentalDays) {
        // Müşteri sadakat seviyesini al
        String loyaltyTier = getCustomerLoyaltyTier(customerId);
        
        // İndirim oranını hesapla
        BigDecimal discountRate = calculateDiscountPercentage(customerId, rentalDays);
        
        log.info("Customer {} loyalty tier: {}, discount: {}%", customerId, loyaltyTier, discountRate);
        
        // Sadakat seviyesine göre araç önerileri
        if ("Platinum".equals(loyaltyTier)) {
            return availableVehicleRepository.findByCityNameAndCategoryName(cityName, "Luks");
        } else if ("Gold".equals(loyaltyTier)) {
            return availableVehicleRepository.findByCityNameAndCategoryName(cityName, "Orta Sinif");
        } else {
            return availableVehicleRepository.findByCityNameAndCategoryName(cityName, "Ekonomik");
        }
    }

    /**
     * Debug: Eski kiralama kayıtlarını temizle
     */
    @Transactional
    public Map<String, Object> cleanupOldRentals() {
        log.info("Cleaning up old rental records");
        return functionRepository.cleanupOldRentals();
    }

    /**
     * Debug: Araç kiralama durumunu kontrol et
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getVehicleRentalDebugInfo(Integer vehicleId) {
        log.info("Getting debug info for vehicle: {}", vehicleId);
        return functionRepository.getVehicleRentalDebugInfo(vehicleId);
    }
} 