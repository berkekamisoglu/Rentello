package com.example.rentello.service;

import com.example.rentello.entity.view.AvailableVehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Veritabanı entegrasyonunu test etmek için örnek servis
 * Bu sınıf uygulamanın başlatılması sırasında örnek işlemleri gerçekleştirir
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseTestService implements CommandLineRunner {

    private final DatabaseIntegrationService databaseService;

    @Override
    public void run(String... args) throws Exception {
        if (isTestModeEnabled()) {
            log.info("=== Veritabanı Entegrasyon Testleri Başlatılıyor ===");
            
            try {
                testStoredProcedures();
                testDatabaseFunctions();
                testViews();
                testCompositeOperations();
                
                log.info("=== Tüm Testler Başarıyla Tamamlandı ===");
            } catch (Exception e) {
                log.error("Test sırasında hata oluştu: ", e);
            }
        }
    }

    private boolean isTestModeEnabled() {
        // Test modunu aktif etmek için environment variable kullanabilirsiniz
        return "true".equals(System.getProperty("database.test.enabled"));
    }

    /**
     * Stored Procedure testleri
     */
    private void testStoredProcedures() {
        log.info("--- Stored Procedure Testleri ---");

        // 1. Kullanıcı kimlik doğrulama testi
        try {
            Map<String, Object> authResult = databaseService.authenticateUser("berkekamisoglu", "Berke0204");
            log.info("Kimlik doğrulama sonucu: {}", authResult);
        } catch (Exception e) {
            log.warn("Kimlik doğrulama test hatası: {}", e.getMessage());
        }

        // 2. Araç durumu güncelleme testi
        try {
            Map<String, Object> statusResult = databaseService.updateVehicleStatus(1, 1, "Test güncelleme", 1);
            log.info("Araç durumu güncelleme sonucu: {}", statusResult);
        } catch (Exception e) {
            log.warn("Araç durumu güncelleme test hatası: {}", e.getMessage());
        }
    }

    /**
     * Database Function testleri
     */
    private void testDatabaseFunctions() {
        log.info("--- Database Function Testleri ---");

        try {
            // 1. Yaş hesaplama
            Integer age = databaseService.calculateAge(LocalDate.of(1995, 1, 1));
            log.info("Hesaplanan yaş: {}", age);

            // 2. Kiralama süresi hesaplama
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(5);
            Integer duration = databaseService.calculateRentalDuration(startDate, endDate);
            log.info("Kiralama süresi: {} gün", duration);

            // 3. Müşteri sadakat seviyesi
            String loyaltyTier = databaseService.getCustomerLoyaltyTier(8); // berkekamisoglu kullanıcı ID'si
            log.info("Müşteri sadakat seviyesi: {}", loyaltyTier);

            // 4. İndirim hesaplama
            BigDecimal discount = databaseService.calculateDiscountPercentage(8, 7);
            log.info("İndirim oranı: {}%", discount);

            // 5. Araç müsaitlik kontrolü
            boolean isAvailable = databaseService.isVehicleAvailable(1, startDate, endDate);
            log.info("Araç müsaitlik durumu: {}", isAvailable);

            // 6. Para formatı
            String formattedAmount = databaseService.formatCurrency(new BigDecimal("1250.75"));
            log.info("Formatlanmış tutar: {}", formattedAmount);

        } catch (Exception e) {
            log.warn("Database function test hatası: {}", e.getMessage());
        }
    }

    /**
     * View testleri
     */
    private void testViews() {
        log.info("--- View Testleri ---");

        try {
            // 1. Mevcut araçları arama
            List<AvailableVehicle> vehicles = databaseService.searchAvailableVehicles(
                    "Istanbul", "Ekonomik", null, new BigDecimal("50"), null, null);
            log.info("Bulunan mevcut araç sayısı: {}", vehicles.size());
            
            if (!vehicles.isEmpty()) {
                AvailableVehicle firstVehicle = vehicles.get(0);
                log.info("İlk araç: {} {} - {} TL/gün", 
                    firstVehicle.getBrandName(), firstVehicle.getModelName(), firstVehicle.getDailyRentalRate());
            }

            // 2. Şehir bazında araç arama
            List<AvailableVehicle> istanbulVehicles = databaseService.getAvailableVehiclesByCity("Istanbul");
            log.info("Istanbul'da mevcut araç sayısı: {}", istanbulVehicles.size());

            // 3. En ucuz araçlar
            List<AvailableVehicle> cheapestVehicles = databaseService.getCheapestVehicles();
            log.info("En ucuz araç listesi boyutu: {}", cheapestVehicles.size());

        } catch (Exception e) {
            log.warn("View test hatası: {}", e.getMessage());
        }
    }

    /**
     * Birleşik işlem testleri
     */
    private void testCompositeOperations() {
        log.info("--- Birleşik İşlem Testleri ---");

        try {
            // 1. Müşteri için kişiselleştirilmiş öneriler
            List<AvailableVehicle> recommendations = databaseService.getPersonalizedVehicleRecommendations(
                    8, "Istanbul", 3);
            log.info("Kişiselleştirilmiş öneri sayısı: {}", recommendations.size());

            // 2. Kiralama için kapsamlı araç arama
            LocalDateTime pickupDate = LocalDateTime.now().plusDays(1);
            LocalDateTime returnDate = pickupDate.plusDays(3);
            List<AvailableVehicle> rentalVehicles = databaseService.searchVehiclesForRental(
                    pickupDate, returnDate, "Istanbul", "Ekonomik", new BigDecimal("40"));
            log.info("Kiralama için uygun araç sayısı: {}", rentalVehicles.size());

            // 3. Müşteri kiralama geçmişi
            List<Map<String, Object>> history = databaseService.getCustomerRentalHistory(8, 5);
            log.info("Müşteri kiralama geçmişi kayıt sayısı: {}", history.size());

            // 4. Popüler araçlar
            List<Map<String, Object>> popularVehicles = databaseService.getPopularVehicles(5);
            log.info("Popüler araç sayısı: {}", popularVehicles.size());

            // 5. Gecikmiş kiralamalar
            List<Map<String, Object>> overdueRentals = databaseService.getOverdueRentals();
            log.info("Gecikmiş kiralama sayısı: {}", overdueRentals.size());

        } catch (Exception e) {
            log.warn("Birleşik işlem test hatası: {}", e.getMessage());
        }
    }

    /**
     * Örnek kiralama senaryosu
     */
    public void demonstrateRentalScenario() {
        log.info("=== Örnek Kiralama Senaryosu ===");

        try {
            // 1. Müşteri giriş yapar
            Map<String, Object> authResult = databaseService.authenticateUser("berkekamisoglu", "Berke0204");
            if ((Boolean) authResult.get("IsSuccess")) {
                Integer customerId = (Integer) authResult.get("UserID");
                log.info("Müşteri başarıyla giriş yaptı. ID: {}", customerId);

                // 2. Müşteri sadakat seviyesini kontrol et
                String loyaltyTier = databaseService.getCustomerLoyaltyTier(customerId);
                log.info("Müşteri sadakat seviyesi: {}", loyaltyTier);

                // 3. Kiralama tarihleri belirle
                LocalDateTime pickupDate = LocalDateTime.now().plusDays(1);
                LocalDateTime returnDate = pickupDate.plusDays(3);
                Integer rentalDays = databaseService.calculateRentalDuration(pickupDate, returnDate);
                
                // 4. İndirim oranını hesapla
                BigDecimal discountRate = databaseService.calculateDiscountPercentage(customerId, rentalDays);
                log.info("Müşteriye {} gün için %{} indirim uygulanacak", rentalDays, discountRate);

                // 5. Uygun araçları bul
                List<AvailableVehicle> suitableVehicles = databaseService.searchVehiclesForRental(
                        pickupDate, returnDate, "Istanbul", "Ekonomik", new BigDecimal("50"));
                
                if (!suitableVehicles.isEmpty()) {
                    AvailableVehicle selectedVehicle = suitableVehicles.get(0);
                    log.info("Seçilen araç: {} {} - {} TL/gün", 
                        selectedVehicle.getBrandName(), selectedVehicle.getModelName(), 
                        selectedVehicle.getDailyRentalRate());

                    // 6. Kiralama oluştur
                    Map<String, Object> rentalResult = databaseService.createRental(
                            customerId, selectedVehicle.getVehicleId(), 1, 1,
                            pickupDate, returnDate, 1);
                    
                    if ((Boolean) rentalResult.get("IsSuccess")) {
                        Integer rentalId = (Integer) rentalResult.get("RentalID");
                        BigDecimal totalAmount = (BigDecimal) rentalResult.get("TotalAmount");
                        log.info("Kiralama başarıyla oluşturuldu. ID: {}, Tutar: {}", 
                            rentalId, databaseService.formatCurrency(totalAmount));
                    } else {
                        log.warn("Kiralama oluşturulamadı: {}", rentalResult.get("ErrorMessage"));
                    }
                }
            } else {
                log.warn("Müşteri girişi başarısız");
            }

        } catch (Exception e) {
            log.error("Kiralama senaryosu hatası: ", e);
        }
    }
} 