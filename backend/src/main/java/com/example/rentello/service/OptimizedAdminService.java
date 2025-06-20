package com.example.rentello.service;

import com.example.rentello.dto.AdminUserManagementDto;
import com.example.rentello.dto.AdminVehicleManagementDto;
import com.example.rentello.dto.DashboardStatsDto;
import com.example.rentello.entity.ViewUserInfo;
import com.example.rentello.entity.ViewAvailableVehicles;
import com.example.rentello.entity.ViewRevenueSummary;
import com.example.rentello.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OptimizedAdminService {

    @Autowired
    private ViewUserInfoRepository viewUserInfoRepository;
    
    @Autowired
    private ViewAvailableVehiclesRepository viewAvailableVehiclesRepository;
    
    @Autowired
    private RentalRepository rentalRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private EntityManager entityManager;

    /**
     * View ve Function'ları kullanarak optimize edilmiş dashboard istatistikleri
     */
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        // View'lardan temel istatistikler
        stats.setTotalUsers(viewUserInfoRepository.count());
        stats.setTotalVehicles(viewAvailableVehiclesRepository.count());
        stats.setActiveCustomers(viewUserInfoRepository.countActiveCustomers());

        // Rental ve Payment repository'lerden gelir istatistikleri
        stats.setTotalRentals(rentalRepository.count());
        stats.setActiveRentals(rentalRepository.countByStatus("Aktif"));
        stats.setTotalRevenue(paymentRepository.getTotalRevenue());
        stats.setMonthlyRevenue(paymentRepository.getMonthlyRevenue(LocalDate.now().withDayOfMonth(1)));
        stats.setDailyRevenue(paymentRepository.getDailyRevenue(LocalDate.now()));

        // Araç durumu istatistikleri - Database Function kullanarak
        stats.setAvailableVehicles(getVehicleCountByStatus("Müsait"));
        stats.setRentedVehicles(getVehicleCountByStatus("Kiralandı"));
        stats.setMaintenanceVehicles(getVehicleCountByStatus("Bakımda"));
        stats.setOutOfServiceVehicles(getVehicleCountByStatus("Hizmet Dışı"));

        // Son 7 günlük istatistikler - Optimize edilmiş sorgu
        Map<LocalDate, Integer> dailyRentals = getDailyRentalsMap();
        Map<LocalDate, BigDecimal> dailyRevenues = getDailyRevenuesMap();
        stats.setDailyRentals(dailyRentals);
        stats.setDailyRevenues(dailyRevenues);

        // Popüler kategoriler - View'dan
        stats.setPopularCategories(getPopularCategoriesFromView());

        // Lokasyon istatistikleri
        stats.setTotalLocations(locationRepository.count());
        stats.setActiveLocations(locationRepository.countByIsActive(true));

        // Bu ayın yeni müşterileri
        stats.setNewCustomersThisMonth(viewUserInfoRepository.countNewCustomersThisMonth(
            LocalDate.now().withDayOfMonth(1).atStartOfDay()));

        return stats;
    }

    /**
     * View kullanarak optimize edilmiş kullanıcı listesi
     */
    public Page<AdminUserManagementDto> getAllUsersOptimized(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ViewUserInfo> users = viewUserInfoRepository.findAll(pageable);
        
        return users.map(this::convertViewUserInfoToDto);
    }

    /**
     * View kullanarak optimize edilmiş kullanıcı arama
     */
    public Page<AdminUserManagementDto> searchUsersOptimized(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ViewUserInfo> users = viewUserInfoRepository.searchUsers(searchTerm, pageable);
        
        return users.map(this::convertViewUserInfoToDto);
    }

    /**
     * View kullanarak optimize edilmiş araç listesi
     */
    public Page<AdminVehicleManagementDto> getAllVehiclesOptimized(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ViewAvailableVehicles> vehicles = viewAvailableVehiclesRepository.findAll(pageable);
        
        return vehicles.map(this::convertViewVehicleToDto);
    }

    /**
     * View kullanarak optimize edilmiş araç arama
     */
    public Page<AdminVehicleManagementDto> searchVehiclesOptimized(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ViewAvailableVehicles> vehicles = viewAvailableVehiclesRepository.searchVehicles(searchTerm, pageable);
        
        return vehicles.map(this::convertViewVehicleToDto);
    }

    // Helper metodlar
    private Long getVehicleCountByStatus(String status) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM Vehicles v " +
            "INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID " +
            "WHERE vs.StatusName = ?1");
        query.setParameter(1, status);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Map<LocalDate, Integer> getDailyRentalsMap() {
        Map<LocalDate, Integer> dailyRentals = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Integer count = rentalRepository.countByDate(date);
            dailyRentals.put(date, count);
        }
        
        return dailyRentals;
    }

    private Map<LocalDate, BigDecimal> getDailyRevenuesMap() {
        Map<LocalDate, BigDecimal> dailyRevenues = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal revenue = paymentRepository.getDailyRevenue(date);
            dailyRevenues.put(date, revenue);
        }
        
        return dailyRevenues;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> getPopularCategoriesFromView() {
        Query query = entityManager.createNativeQuery(
            "SELECT CategoryName, COUNT(*) as VehicleCount " +
            "FROM vw_AvailableVehicles " +
            "GROUP BY CategoryName " +
            "ORDER BY VehicleCount DESC");
        
        List<Object[]> results = query.getResultList();
        Map<String, Long> popularCategories = new HashMap<>();
        
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            popularCategories.put(categoryName, count);
        }
        
        return popularCategories;
    }

    private AdminUserManagementDto convertViewUserInfoToDto(ViewUserInfo user) {
        AdminUserManagementDto dto = new AdminUserManagementDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setIsActive(user.getIsActive());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setRoleName(user.getRoleName());
        dto.setCityName(user.getCityName());

        // İstatistikler için ayrı sorgular (cache'lenebilir)
        dto.setTotalRentals(getTotalRentalsByUserId(user.getUserId()));
        dto.setActiveRentals(getActiveRentalsByUserId(user.getUserId()));
        
        return dto;
    }

    private AdminVehicleManagementDto convertViewVehicleToDto(ViewAvailableVehicles vehicle) {
        AdminVehicleManagementDto dto = new AdminVehicleManagementDto();
        dto.setVehicleId(vehicle.getVehicleId());
        dto.setLicensePlate(vehicle.getVehicleRegistration());
        dto.setBrandName(vehicle.getBrandName());
        dto.setModelName(vehicle.getModelName());
        dto.setCategoryName(vehicle.getCategoryName());
        dto.setColor(vehicle.getColor());
        dto.setYear(vehicle.getManufactureYear());
        dto.setDailyRate(vehicle.getDailyRentalRate());
        dto.setMileage(vehicle.getMileage());
        dto.setFuelType(vehicle.getFuelType());
        dto.setTransmissionType(vehicle.getTransmissionType());
        dto.setCapacity(vehicle.getSeatingCapacity());
        dto.setStatusName(vehicle.getStatusName());
        dto.setLocationName(vehicle.getLocationName());

        // İstatistikler
        dto.setTotalRentals(getTotalRentalsByVehicleId(vehicle.getVehicleId()));
        dto.setActiveRentals(getActiveRentalsByVehicleId(vehicle.getVehicleId()));
        dto.setTotalRevenue(getTotalRevenueByVehicleId(vehicle.getVehicleId()));
        
        return dto;
    }

    // Cache'lenebilir yardımcı metodlar
    private Long getTotalRentalsByUserId(Integer userId) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Rental r WHERE r.customer.userId = :userId");
        query.setParameter("userId", userId);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Long getActiveRentalsByUserId(Integer userId) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Rental r WHERE r.customer.userId = :userId " +
            "AND r.rentalStatus.statusName = 'Aktif'");
        query.setParameter("userId", userId);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Long getTotalRentalsByVehicleId(Integer vehicleId) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Rental r WHERE r.vehicle.vehicleId = :vehicleId");
        query.setParameter("vehicleId", vehicleId);
        return ((Number) query.getSingleResult()).longValue();
    }

    private Long getActiveRentalsByVehicleId(Integer vehicleId) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Rental r WHERE r.vehicle.vehicleId = :vehicleId " +
            "AND r.rentalStatus.statusName = 'Aktif'");
        query.setParameter("vehicleId", vehicleId);
        return ((Number) query.getSingleResult()).longValue();
    }

    private BigDecimal getTotalRevenueByVehicleId(Integer vehicleId) {
        return rentalRepository.getTotalRevenueByVehicle(vehicleId);
    }

    // Kullanıcı ve araç durum değiştirme metodları
    public boolean toggleUserStatus(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setIsActive(!user.getIsActive());
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    public boolean toggleVehicleStatus(Integer vehicleId) {
        // Vehicle entity'si için isActive alanı doğrudan erişilebilir olmayabilir
        // Bu durumda native query kullanabiliriz veya Vehicle entity'sini güncelleyebiliriz
        Query query = entityManager.createNativeQuery(
            "UPDATE Vehicles SET IsActive = CASE WHEN IsActive = 1 THEN 0 ELSE 1 END WHERE VehicleID = ?1");
        query.setParameter(1, vehicleId);
        int updated = query.executeUpdate();
        return updated > 0;
    }
} 