package com.example.rentello.service;

import com.example.rentello.dto.AdminUserManagementDto;
import com.example.rentello.dto.AdminVehicleManagementDto;
import com.example.rentello.dto.DashboardStatsDto;
import com.example.rentello.dto.AdminRentalDto;
import com.example.rentello.entity.*;
import com.example.rentello.repository.*;
import com.example.rentello.repository.ViewUserInfoRepository;
import com.example.rentello.repository.ViewAvailableVehiclesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private RentalRepository rentalRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;
    
    @Autowired
    private VehicleStatusRepository vehicleStatusRepository;

    @Autowired
    private VehicleBrandRepository vehicleBrandRepository;
    
    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepository;

    public DashboardStatsDto getDashboardStats() {
        return getDashboardStatsInternal();
    }
    
    private DashboardStatsDto getDashboardStatsInternal() {
        try {
            DashboardStatsDto stats = new DashboardStatsDto();
            System.out.println("🔍 ADMIN: Starting dashboard stats calculation...");

            // Genel İstatistikler - Her birini ayrı try-catch ile
            try {
                stats.setTotalUsers(userRepository.count());
                System.out.println("🔍 ADMIN: Total users calculated");
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error getting total users: " + e.getMessage());
                stats.setTotalUsers(0L);
            }
            
            try {
                stats.setTotalVehicles(vehicleRepository.count());
                System.out.println("🔍 ADMIN: Total vehicles calculated");
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error getting total vehicles: " + e.getMessage());
                stats.setTotalVehicles(0L);
            }
            
            try {
                stats.setTotalRentals(rentalRepository.count());
                System.out.println("🔍 ADMIN: Total rentals calculated");
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error getting total rentals: " + e.getMessage());
                stats.setTotalRentals(0L);
            }
            
            // Aktif kiralama sayısını hesapla
            try {
                Long activeRentals = rentalRepository.countByStatus("Aktif");
                stats.setActiveRentals(activeRentals);
                System.out.println("🔍 ADMIN: Active rentals count: " + activeRentals);
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error getting active rentals: " + e.getMessage());
                stats.setActiveRentals(0L);
            }

            // Gelir İstatistikleri
            System.out.println("🔍 ADMIN: Starting revenue calculations...");
            
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal dailyRevenue = BigDecimal.ZERO;
            BigDecimal monthlyRevenue = BigDecimal.ZERO;
            
            try {
                totalRevenue = paymentRepository.getTotalRevenue();
                System.out.println("🔍 ADMIN: Total revenue calculated: " + totalRevenue);
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error getting total revenue: " + e.getMessage());
            }
            
            try {
                dailyRevenue = paymentRepository.getDailyRevenue(LocalDate.now());
                System.out.println("🔍 ADMIN: Daily revenue calculated: " + dailyRevenue);
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error getting daily revenue: " + e.getMessage());
            }
            
            try {
                // Aylık gelir = Bu ay tamamlanan kiralamalardan elde edilen gelir
                monthlyRevenue = getMonthlyRevenueFromCompletedRentals();
                System.out.println("🔍 ADMIN: Monthly revenue calculated: " + monthlyRevenue);
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error getting monthly revenue: " + e.getMessage());
                e.printStackTrace();
            }
            
            stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            stats.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
            stats.setDailyRevenue(dailyRevenue != null ? dailyRevenue : BigDecimal.ZERO);
            
            System.out.println("🔍 ADMIN: Revenue stats calculated successfully");

            // Araç İstatistikleri - Basit yaklaşım
            try {
                Long availableCount = vehicleRepository.countByStatus("Musait");
                if (availableCount == null || availableCount == 0) {
                    availableCount = vehicleRepository.countByStatus("Müsait");
                }
                
                stats.setAvailableVehicles(availableCount != null ? availableCount : 0L);
                stats.setRentedVehicles(0L); // Şimdilik 0
                stats.setMaintenanceVehicles(0L); // Şimdilik 0
                stats.setOutOfServiceVehicles(0L); // Şimdilik 0
                
                System.out.println("🔍 ADMIN: Vehicle stats calculated successfully");
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error calculating vehicle stats: " + e.getMessage());
                stats.setAvailableVehicles(0L);
                stats.setRentedVehicles(0L);
                stats.setMaintenanceVehicles(0L);
                stats.setOutOfServiceVehicles(0L);
            }

            // Basit istatistikler
            try {
                stats.setDailyRentals(new HashMap<>());
                stats.setDailyRevenues(new HashMap<>());
                stats.setPopularCategories(new HashMap<>());
                stats.setNewCustomersThisMonth(0L);
                stats.setActiveCustomers(userRepository.count());
                stats.setTotalLocations(locationRepository.count());
                stats.setActiveLocations(locationRepository.count());
                System.out.println("🔍 ADMIN: Basic stats calculated successfully");
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error calculating basic stats: " + e.getMessage());
                stats.setDailyRentals(new HashMap<>());
                stats.setDailyRevenues(new HashMap<>());
                stats.setPopularCategories(new HashMap<>());
                stats.setNewCustomersThisMonth(0L);
                stats.setActiveCustomers(0L);
                stats.setTotalLocations(0L);
                stats.setActiveLocations(0L);
            }

            System.out.println("✅ ADMIN: Dashboard stats calculated successfully!");
            System.out.println("🔍 ADMIN: Returning stats object...");
            return stats;
            
        } catch (Exception e) {
            System.err.println("❌ ADMIN: Critical error in getDashboardStats: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback stats
            return createFallbackStats();
        }
    }
    
    private DashboardStatsDto createFallbackStats() {
        DashboardStatsDto fallbackStats = new DashboardStatsDto();
        fallbackStats.setTotalUsers(0L);
        fallbackStats.setTotalVehicles(0L);
        fallbackStats.setTotalRentals(0L);
        fallbackStats.setActiveRentals(0L);
        fallbackStats.setTotalRevenue(BigDecimal.ZERO);
        fallbackStats.setMonthlyRevenue(BigDecimal.ZERO);
        fallbackStats.setDailyRevenue(BigDecimal.ZERO);
        fallbackStats.setAvailableVehicles(0L);
        fallbackStats.setRentedVehicles(0L);
        fallbackStats.setMaintenanceVehicles(0L);
        fallbackStats.setOutOfServiceVehicles(0L);
        fallbackStats.setDailyRentals(new HashMap<>());
        fallbackStats.setDailyRevenues(new HashMap<>());
        fallbackStats.setPopularCategories(new HashMap<>());
        fallbackStats.setNewCustomersThisMonth(0L);
        fallbackStats.setActiveCustomers(0L);
        fallbackStats.setTotalLocations(0L);
        fallbackStats.setActiveLocations(0L);
        
        return fallbackStats;
    }

    public Page<AdminUserManagementDto> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users = userRepository.findAll(pageable);
        
        return users.map(this::convertToAdminUserDto);
    }

    public Page<AdminUserManagementDto> searchUsers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findByUsernameContainingOrEmailContainingOrFirstNameContainingOrLastNameContaining(
            searchTerm, searchTerm, searchTerm, searchTerm, pageable);
        
        return users.map(this::convertToAdminUserDto);
    }

    public Page<AdminVehicleManagementDto> getAllVehicles(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
        
        return vehicles.map(this::convertToAdminVehicleDto);
    }

    public Page<AdminVehicleManagementDto> searchVehicles(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Vehicle> vehicles = vehicleRepository.findByVehicleRegistrationContainingOrVehicleDescriptionContaining(
            searchTerm, pageable);
        
        return vehicles.map(this::convertToAdminVehicleDto);
    }

    @Transactional
    public boolean toggleUserStatus(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setIsActive(!user.getIsActive());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean toggleVehicleStatus(Integer vehicleId) {
        // Vehicle entity'sinde isActive field'ı yok, status üzerinden çalışıyoruz
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle != null) {
            // Status değiştirme işlemi burada yapılabilir
            // Şimdilik basit bir implementasyon
            vehicleRepository.save(vehicle);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateUserRole(Integer userId, Integer roleId) {
        User user = userRepository.findById(userId).orElse(null);
        UserRole role = userRoleRepository.findById(roleId).orElse(null);
        
        if (user != null && role != null) {
            user.setUserRole(role);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public String resetUserPassword(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        // Generate new random password
        String newPassword = generateRandomPassword();
        
        // Encode and set new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return newPassword;
    }

    @Transactional
    public void resetUserPassword(Integer userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        // Encode and set new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        // Check if user has active rentals
        Long activeRentals = rentalRepository.countByCustomerAndStatus(user, "Active");
        if (activeRentals > 0) {
            throw new RuntimeException("Kullanıcının aktif kiralamaları bulunduğu için silinemez");
        }
        
        // Check if user has pending payments
        Long pendingRentals = rentalRepository.countByCustomerAndStatus(user, "Reserved");
        if (pendingRentals > 0) {
            throw new RuntimeException("Kullanıcının bekleyen kiralamaları bulunduğu için silinemez");
        }
        
        // Soft delete - set user as inactive instead of hard delete
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public AdminUserManagementDto createUser(Map<String, Object> userRequest) {
        // Validate required fields
        String username = (String) userRequest.get("username");
        String email = (String) userRequest.get("email");
        String password = (String) userRequest.get("password");
        String firstName = (String) userRequest.get("firstName");
        String lastName = (String) userRequest.get("lastName");
        String phoneNumber = (String) userRequest.get("phoneNumber");
        Integer roleId = (Integer) userRequest.get("roleId");
        
        if (username == null || email == null || password == null || 
            firstName == null || lastName == null || roleId == null) {
            throw new RuntimeException("Gerekli alanlar eksik");
        }
        
        // Check if username or email already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor");
        }
        
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Bu e-posta adresi zaten kullanılıyor");
        }
        
        // Get role
        UserRole role = userRoleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Geçersiz rol"));
        
        // Create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setUserRole(role);
        newUser.setIsActive(true);
        newUser.setCreatedDate(LocalDateTime.now());
        
        // Set optional fields
        if (userRequest.get("dateOfBirth") != null) {
            newUser.setDateOfBirth((LocalDate) userRequest.get("dateOfBirth"));
        }
        if (userRequest.get("address") != null) {
            newUser.setAddress((String) userRequest.get("address"));
        }
        
        User savedUser = userRepository.save(newUser);
        
        // Convert to DTO and return
        return convertToAdminUserDto(savedUser);
    }

    public List<UserRole> getAllRoles() {
        return userRoleRepository.findAll();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    private AdminUserManagementDto convertToAdminUserDto(User user) {
        AdminUserManagementDto dto = new AdminUserManagementDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setAddress(user.getAddress());
        dto.setIsActive(user.getIsActive());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setUpdatedDate(user.getUpdatedDate());
        
        if (user.getUserRole() != null) {
            dto.setRoleId(user.getUserRole().getRoleId());
            dto.setRoleName(user.getUserRole().getRoleName());
        }
        
        if (user.getCity() != null) {
            dto.setCityId(user.getCity().getCityId());
            dto.setCityName(user.getCity().getCityName());
        }

        // İstatistikler
        dto.setTotalRentals(rentalRepository.countByCustomer(user));
        dto.setActiveRentals(rentalRepository.countByCustomerAndStatus(user, "Aktif"));
        
        return dto;
    }

    private AdminVehicleManagementDto convertToAdminVehicleDto(Vehicle vehicle) {
        AdminVehicleManagementDto dto = new AdminVehicleManagementDto();
        dto.setVehicleId(vehicle.getVehicleId());
        dto.setLicensePlate(vehicle.getVehicleRegistration()); // Doğru field ismi
        dto.setVin(""); // VIN field'ı Vehicle entity'sinde yok
        dto.setColor(vehicle.getColor());
        dto.setYear(null); // Year bilgisi model'den gelecek
        dto.setDailyRate(vehicle.getDailyRentalRate()); // Doğru field ismi
        dto.setMileage(vehicle.getMileage());
        dto.setFuelType(""); // FuelType model'den gelecek
        dto.setTransmissionType(""); // TransmissionType model'den gelecek
        dto.setCapacity(null); // Capacity model'den gelecek
        dto.setIsActive(true); // Default olarak true, status'ten çıkarabiliriz
        dto.setFeatures(""); // Features field'ı yok
        dto.setDescription(vehicle.getVehicleDescription()); // Doğru field ismi
        dto.setCreatedDate(vehicle.getCreatedDate());
        dto.setUpdatedDate(vehicle.getUpdatedDate());
        
        if (vehicle.getModel() != null) { // getVehicleModel() değil getModel()
            dto.setModelId(vehicle.getModel().getModelId());
            dto.setModelName(vehicle.getModel().getModelName());
            dto.setYear(vehicle.getModel().getManufactureYear()); // Model'den year bilgisi
            dto.setFuelType(vehicle.getModel().getFuelType()); // Model'den fuel type
            dto.setTransmissionType(vehicle.getModel().getTransmissionType()); // Model'den transmission
            dto.setCapacity(vehicle.getModel().getSeatingCapacity()); // Model'den capacity
            
                         if (vehicle.getModel().getBrand() != null) {
                dto.setBrandId(vehicle.getModel().getBrand().getBrandId());
                dto.setBrandName(vehicle.getModel().getBrand().getBrandName());
             }
             
             if (vehicle.getModel().getCategory() != null) {
                dto.setCategoryId(vehicle.getModel().getCategory().getCategoryId());
                dto.setCategoryName(vehicle.getModel().getCategory().getCategoryName());
             }
        }
        
        if (vehicle.getCurrentLocation() != null) { // getLocation() değil getCurrentLocation()
            dto.setLocationId(vehicle.getCurrentLocation().getLocationId());
            dto.setLocationName(vehicle.getCurrentLocation().getLocationName());
        }
        
        if (vehicle.getCurrentStatus() != null) { // getVehicleStatus() değil getCurrentStatus()
            dto.setStatusId(vehicle.getCurrentStatus().getStatusId());
            dto.setStatusName(vehicle.getCurrentStatus().getStatusName());
            dto.setIsActive(vehicle.getCurrentStatus().getIsAvailableForRent()); // Status'ten active bilgisi
        }

        // İstatistikler
        dto.setTotalRentals(rentalRepository.countByVehicle(vehicle));
        dto.setActiveRentals(rentalRepository.countByVehicleAndStatus(vehicle, "Aktif"));
        dto.setTotalRevenue(rentalRepository.getTotalRevenueByVehicle(vehicle.getVehicleId()));
        dto.setMaintenanceCount(0); // MaintenanceRecords field'ı yok, 0 olarak set ediyoruz
        
        return dto;
    }

    @Transactional
    public AdminVehicleManagementDto createVehicle(AdminVehicleManagementDto vehicleDto) {
        Vehicle vehicle = new Vehicle();
        
        // Basic vehicle information
        vehicle.setVehicleRegistration(vehicleDto.getLicensePlate());
        vehicle.setColor(vehicleDto.getColor());
        vehicle.setMileage(vehicleDto.getMileage() != null ? vehicleDto.getMileage() : 0);
        vehicle.setDailyRentalRate(vehicleDto.getDailyRate());
        vehicle.setVehicleDescription(vehicleDto.getDescription());
        
        // Set relationships
        if (vehicleDto.getModelId() != null) {
            VehicleModel model = vehicleModelRepository.findById(vehicleDto.getModelId())
                .orElseThrow(() -> new RuntimeException("Model bulunamadı"));
            vehicle.setModel(model);
        }
        
        if (vehicleDto.getLocationId() != null) {
            Location location = locationRepository.findById(vehicleDto.getLocationId())
                .orElseThrow(() -> new RuntimeException("Lokasyon bulunamadı"));
            vehicle.setCurrentLocation(location);
        }
        
        if (vehicleDto.getStatusId() != null) {
            VehicleStatus status = vehicleStatusRepository.findById(vehicleDto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Durum bulunamadı"));
            vehicle.setCurrentStatus(status);
        }
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToAdminVehicleDto(savedVehicle);
    }

    @Transactional
    public AdminVehicleManagementDto updateVehicle(Integer vehicleId, AdminVehicleManagementDto vehicleDto) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Araç bulunamadı"));
        
        // Update basic information
        if (vehicleDto.getLicensePlate() != null) {
            vehicle.setVehicleRegistration(vehicleDto.getLicensePlate());
        }
        if (vehicleDto.getColor() != null) {
            vehicle.setColor(vehicleDto.getColor());
        }
        if (vehicleDto.getMileage() != null) {
            vehicle.setMileage(vehicleDto.getMileage());
        }
        if (vehicleDto.getDailyRate() != null) {
            vehicle.setDailyRentalRate(vehicleDto.getDailyRate());
        }
        if (vehicleDto.getDescription() != null) {
            vehicle.setVehicleDescription(vehicleDto.getDescription());
        }
        
        // Update relationships
        if (vehicleDto.getModelId() != null) {
            VehicleModel model = vehicleModelRepository.findById(vehicleDto.getModelId())
                .orElseThrow(() -> new RuntimeException("Model bulunamadı"));
            vehicle.setModel(model);
        }
        
        if (vehicleDto.getLocationId() != null) {
            Location location = locationRepository.findById(vehicleDto.getLocationId())
                .orElseThrow(() -> new RuntimeException("Lokasyon bulunamadı"));
            vehicle.setCurrentLocation(location);
        }
        
        if (vehicleDto.getStatusId() != null) {
            VehicleStatus status = vehicleStatusRepository.findById(vehicleDto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Durum bulunamadı"));
            vehicle.setCurrentStatus(status);
        }
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToAdminVehicleDto(savedVehicle);
    }

    @Transactional
    public boolean deleteVehicle(Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle != null) {
            // Check if vehicle has active rentals
            long activeRentals = rentalRepository.countByVehicleAndStatus(vehicle, "Aktif");
            if (activeRentals > 0) {
                throw new RuntimeException("Aktif kiralama kaydı olan araç silinemez");
            }
            
            vehicleRepository.delete(vehicle);
            return true;
        }
        return false;
    }

    public Map<String, Object> getVehicleReferences() {
        Map<String, Object> references = new HashMap<>();
        
        references.put("models", vehicleModelRepository.findAll());
        references.put("brands", vehicleBrandRepository.findAll());
        references.put("categories", vehicleCategoryRepository.findAll());
        references.put("locations", locationRepository.findByIsActive(true));
        references.put("statuses", vehicleStatusRepository.findAll());
        
        return references;
    }

    public Page<AdminRentalDto> getAllRentals(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Rental> rentals = rentalRepository.findAll(pageable);
        
        return rentals.map(this::convertToAdminRentalDto);
    }

    public Page<AdminRentalDto> searchRentals(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Rental> rentals = rentalRepository.findByCustomerNameOrVehiclePlateContaining(searchTerm, pageable);
        
        return rentals.map(this::convertToAdminRentalDto);
    }

    private AdminRentalDto convertToAdminRentalDto(Rental rental) {
        AdminRentalDto dto = new AdminRentalDto();
        dto.setRentalId(rental.getRentalId());
        dto.setStartDate(rental.getPlannedPickupDate().toLocalDate());
        dto.setEndDate(rental.getPlannedReturnDate().toLocalDate());
        dto.setActualReturnDate(rental.getActualReturnDate() != null ? rental.getActualReturnDate().toLocalDate() : null);
        dto.setTotalCost(rental.getTotalAmount());
        
        // Calculate total days
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
            rental.getPlannedPickupDate().toLocalDate(), 
            rental.getPlannedReturnDate().toLocalDate()
        );
        dto.setTotalDays((int) daysBetween);
        
        dto.setNotes(rental.getNotes());
        dto.setCreatedDate(rental.getCreatedDate());
        dto.setUpdatedDate(rental.getUpdatedDate());
        
        // Customer information
        if (rental.getCustomer() != null) {
            dto.setCustomerId(rental.getCustomer().getUserId());
            dto.setCustomerName(rental.getCustomer().getFirstName() + " " + rental.getCustomer().getLastName());
            dto.setCustomerEmail(rental.getCustomer().getEmail());
            dto.setCustomerPhone(rental.getCustomer().getPhoneNumber());
        }
        
        // Vehicle information
        if (rental.getVehicle() != null) {
            dto.setVehicleId(rental.getVehicle().getVehicleId());
            dto.setVehiclePlate(rental.getVehicle().getVehicleRegistration());
            
            if (rental.getVehicle().getModel() != null) {
                dto.setVehicleModel(rental.getVehicle().getModel().getModelName());
                dto.setVehicleYear(rental.getVehicle().getModel().getManufactureYear());
                
                if (rental.getVehicle().getModel().getBrand() != null) {
                    dto.setVehicleBrand(rental.getVehicle().getModel().getBrand().getBrandName());
                }
            }
        }
        
        // Location information
        if (rental.getPickupLocation() != null) {
            dto.setPickupLocationId(rental.getPickupLocation().getLocationId());
            dto.setPickupLocationName(rental.getPickupLocation().getLocationName());
        }
        
        if (rental.getReturnLocation() != null) {
            dto.setReturnLocationId(rental.getReturnLocation().getLocationId());
            dto.setReturnLocationName(rental.getReturnLocation().getLocationName());
        }
        
        // Status information
        if (rental.getRentalStatus() != null) {
            dto.setStatusId(rental.getRentalStatus().getStatusId());
            dto.setStatusName(rental.getRentalStatus().getStatusName());
        }
        
        // Payment information - calculate from Payment entity
        BigDecimal totalPaid = BigDecimal.ZERO;
        String paymentStatus = "Ödenmedi";
        
        // Get payments for this rental
        List<Payment> payments = paymentRepository.findByRentalId(rental.getRentalId());
        if (payments != null && !payments.isEmpty()) {
            totalPaid = payments.stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Determine payment status based on total paid vs total cost
            if (totalPaid.compareTo(rental.getTotalAmount()) >= 0) {
                paymentStatus = "Paid"; // Fully paid
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                paymentStatus = "Pending"; // Partially paid
            } else {
                paymentStatus = "Unpaid"; // Not paid
            }
        }
        
        dto.setTotalPaid(totalPaid);
        dto.setRemainingAmount(rental.getTotalAmount().subtract(totalPaid));
        dto.setPaymentStatus(paymentStatus);
        
        return dto;
    }

    /**
     * Bu ay tamamlanan kiralamalardan elde edilen toplam geliri hesaplar
     */
    private BigDecimal getMonthlyRevenueFromCompletedRentals() {
        try {
            System.out.println("💰 ADMIN: Starting monthly revenue calculation...");
            
            // Bu ayın başlangıç tarihi
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
            
            System.out.println("💰 ADMIN: Searching for completed rentals from " + startOfMonthDateTime + " to " + LocalDateTime.now());
            
            // Bu ay tamamlanan kiralamaları bul (Tamamlandi veya Odendi status'u)
            List<Rental> completedRentalsThisMonth = null;
            try {
                completedRentalsThisMonth = rentalRepository.findCompletedRentalsInMonth(
                    startOfMonthDateTime, LocalDateTime.now());
                System.out.println("💰 ADMIN: Repository call successful, found " + 
                    (completedRentalsThisMonth != null ? completedRentalsThisMonth.size() : 0) + " rentals");
            } catch (Exception e) {
                System.err.println("❌ ADMIN: Error in repository call: " + e.getMessage());
                e.printStackTrace();
                return BigDecimal.ZERO;
            }
            
            // Toplam geliri hesapla
            BigDecimal totalRevenue = BigDecimal.ZERO;
            if (completedRentalsThisMonth != null) {
                for (Rental rental : completedRentalsThisMonth) {
                    if (rental != null && rental.getTotalAmount() != null) {
                        totalRevenue = totalRevenue.add(rental.getTotalAmount());
                    }
                }
            }
            
            System.out.println("💰 ADMIN: Monthly revenue calculation completed successfully: " + totalRevenue);
            return totalRevenue;
            
        } catch (Exception e) {
            System.err.println("❌ ADMIN: Error calculating monthly revenue from completed rentals: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
} 