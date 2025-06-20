package com.example.rentello.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DatabaseFunctionRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Yaş hesaplama fonksiyonu
     */
    public Integer calculateAge(LocalDate dateOfBirth) {
        String sql = "SELECT dbo.fn_CalculateAge(?)";
        return jdbcTemplate.queryForObject(sql, Integer.class, dateOfBirth);
    }

    /**
     * Kiralama süresi hesaplama
     */
    public Integer calculateRentalDuration(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT dbo.fn_CalculateRentalDuration(?, ?)";
        return jdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);
    }

    /**
     * Geç ücret hesaplama
     */
    public BigDecimal calculateLateFee(LocalDateTime plannedReturnDate,
                                      LocalDateTime actualReturnDate,
                                      BigDecimal dailyRate) {
        String sql = "SELECT dbo.fn_CalculateLateFee(?, ?, ?)";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class,
                plannedReturnDate, actualReturnDate, dailyRate);
    }

    /**
     * Müşteri sadakat seviyesi
     */
    public String getCustomerLoyaltyTier(Integer customerId) {
        String sql = "SELECT dbo.fn_GetCustomerLoyaltyTier(?)";
        return jdbcTemplate.queryForObject(sql, String.class, customerId);
    }

    /**
     * İndirim yüzdesi hesaplama
     */
    public BigDecimal calculateDiscountPercentage(Integer customerId, Integer rentalDays) {
        String sql = "SELECT dbo.fn_CalculateDiscountPercentage(?, ?)";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, customerId, rentalDays);
    }

    /**
     * Araç müsaitlik kontrolü
     */
    public Boolean isVehicleAvailable(Integer vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT dbo.fn_IsVehicleAvailable(?, ?, ?)";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, vehicleId, startDate, endDate);
        return result != null && result == 1;
    }

    /**
     * Araç kullanım oranı hesaplama
     */
    public BigDecimal calculateVehicleUtilizationRate(Integer vehicleId, Integer days) {
        String sql = "SELECT dbo.fn_CalculateVehicleUtilizationRate(?, ?)";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, vehicleId, days);
    }

    /**
     * Para formatı
     */
    public String formatCurrency(BigDecimal amount) {
        String sql = "SELECT dbo.fn_FormatCurrency(?)";
        return jdbcTemplate.queryForObject(sql, String.class, amount);
    }

    /**
     * Belirli tarih aralığında mevcut araçları getir
     */
    public List<Map<String, Object>> getAvailableVehiclesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM dbo.fn_GetAvailableVehiclesByDateRange(?, ?)";
        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    /**
     * Müşteri kiralama geçmişi
     */
    public List<Map<String, Object>> getCustomerRentalHistory(Integer customerId, Integer topN) {
        String sql = "SELECT * FROM dbo.fn_GetCustomerRentalHistory(?, ?)";
        return jdbcTemplate.queryForList(sql, customerId, topN);
    }

    /**
     * Araç bakım geçmişi
     */
    public List<Map<String, Object>> getVehicleMaintenanceHistory(Integer vehicleId) {
        String sql = "SELECT * FROM dbo.fn_GetVehicleMaintenanceHistory(?)";
        return jdbcTemplate.queryForList(sql, vehicleId);
    }

    /**
     * Gelir raporu - tarih aralığına göre
     */
    public List<Map<String, Object>> getRevenueReportByPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM dbo.fn_GetRevenueReportByPeriod(?, ?)";
        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    /**
     * Gecikmiş kiralamalar
     */
    public List<Map<String, Object>> getOverdueRentals() {
        String sql = "SELECT * FROM dbo.fn_GetOverdueRentals()";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Popüler araçlar
     */
    public List<Map<String, Object>> getPopularVehicles(Integer topN) {
        String sql = "SELECT * FROM dbo.fn_GetPopularVehicles(?)";
        return jdbcTemplate.queryForList(sql, topN);
    }

    /**
     * Debug: Eski kiralama kayıtlarını temizle
     */
    public Map<String, Object> cleanupOldRentals() {
        try {
            // Geçmiş tarihli ve tamamlanmamış kiralamaları tamamlandı olarak işaretle
            String updateSql = """
                UPDATE Rentals 
                SET RentalStatusID = 4, -- Tamamlandi
                    UpdatedDate = GETDATE()
                WHERE PlannedReturnDate < GETDATE() 
                AND RentalStatusID IN (1, 2) -- Rezerve Edildi, Aktif
                """;
            
            int updatedCount = jdbcTemplate.update(updateSql);
            
            return Map.of(
                "success", true,
                "message", "Eski kiralama kayıtları temizlendi",
                "updatedRecords", updatedCount
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Temizleme işlemi başarısız: " + e.getMessage()
            );
        }
    }

    /**
     * Debug: Araç kiralama durumunu kontrol et
     */
    public Map<String, Object> getVehicleRentalDebugInfo(Integer vehicleId) {
        String sql = """
            SELECT 
                v.VehicleID,
                v.VehicleRegistration,
                vs.StatusName as CurrentStatus,
                vs.IsAvailableForRent,
                COUNT(r.RentalID) as ActiveRentals,
                MAX(r.PlannedReturnDate) as LatestReturnDate,
                STRING_AGG(CAST(r.RentalID as VARCHAR) + ':' + rs.StatusName, ', ') as RentalDetails
            FROM Vehicle v
            LEFT JOIN VehicleStatus vs ON v.StatusID = vs.StatusID
            LEFT JOIN Rentals r ON v.VehicleID = r.VehicleID 
                AND r.RentalStatusID IN (1, 2) -- Rezerve Edildi, Aktif
            LEFT JOIN RentalStatus rs ON r.RentalStatusID = rs.StatusID
            WHERE v.VehicleID = ?
            GROUP BY v.VehicleID, v.VehicleRegistration, vs.StatusName, vs.IsAvailableForRent
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, vehicleId);
        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            return Map.of("error", "Vehicle not found", "vehicleId", vehicleId);
        }
    }

    /**
     * Arama için available vehicles - stored procedure alternative
     */
    public List<Map<String, Object>> searchAvailableVehicles(LocalDateTime pickupDate,
                                                            LocalDateTime returnDate,
                                                            Integer locationId,
                                                            Integer categoryId,
                                                            BigDecimal maxDailyRate) {
        String sql = "EXEC sp_SearchAvailableVehicles ?, ?, ?, ?, ?";
        return jdbcTemplate.queryForList(sql, pickupDate, returnDate, locationId, categoryId, maxDailyRate);
    }
} 