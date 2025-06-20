package com.example.rentello.service;

import com.example.rentello.entity.*;
import com.example.rentello.entity.view.AvailableVehicle;
import com.example.rentello.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {
    
    private final RentalRepository rentalRepository;
    private final VehicleService vehicleService;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleStatusRepository vehicleStatusRepository;
    private final LocationRepository locationRepository;
    private final PricingService pricingService;
    private final StoredProcedureRepository storedProcedureRepository;
    private final DatabaseFunctionRepository functionRepository;
    private final AvailableVehicleRepository availableVehicleRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final RentalStatusRepository rentalStatusRepository;

    // Basic CRUD operations
    public Optional<Rental> findById(Integer rentalId) {
        return rentalRepository.findById(rentalId);
    }
    
    public void deleteById(Integer rentalId) {
        Rental rental = getRentalById(rentalId);
        rentalRepository.delete(rental);
    }
    
    public Page<Rental> getAllRentals(Pageable pageable) {
        return rentalRepository.findAll(pageable);
    }
    
    public Rental updateRental(Rental rental) {
        return rentalRepository.save(rental);
    }
    
    public List<Rental> getRentalsByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
            return rentalRepository.findByCustomerId(user.getUserId());
        } catch (Exception e) {
            System.err.println("Error getting rentals for user: " + username + " - " + e.getMessage());
            return List.of(); // Return empty list instead of throwing exception
        }
    }
    
    public Rental createRentalFromRequest(Map<String, Object> rentalData, String username) {
        try {
            // Get user
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Get vehicle
            Integer vehicleId = (Integer) rentalData.get("vehicleId");
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
            
            // Create simple rental status - using hardcoded approach for now
            RentalStatus status = new RentalStatus();
            status.setStatusId(1);
            status.setStatusName("Aktif");
            status.setStatusDescription("Kiralama aktif");
            
            // Get default location (vehicle's current location)
            Location pickupLocation = vehicle.getCurrentLocation();
            Location dropoffLocation = pickupLocation; // Same as pickup for now
            
            // Parse dates
            String startDateStr = (String) rentalData.get("startDate");
            String endDateStr = (String) rentalData.get("endDate");
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            
            // Create rental
            Rental rental = new Rental();
            rental.setCustomer(user);
            rental.setVehicle(vehicle);
            rental.setRentalStatus(status);
            rental.setPlannedPickupDate(startDate.atStartOfDay());
            rental.setPlannedReturnDate(endDate.atStartOfDay());
            rental.setPickupLocation(pickupLocation);
            rental.setReturnLocation(dropoffLocation);
            
            // Calculate dynamic pricing
            BigDecimal dynamicPrice = pricingService.calculateDynamicPrice(
                vehicle.getDailyRentalRate(), 
                startDate, 
                endDate
            );
            
            // Use provided total amount or calculated dynamic price
            Object totalAmountObj = rentalData.get("totalAmount");
            BigDecimal totalAmount = totalAmountObj != null ? 
                BigDecimal.valueOf(((Number) totalAmountObj).doubleValue()) : 
                dynamicPrice;
            
            rental.setBaseAmount(dynamicPrice);
            rental.setTotalAmount(totalAmount);
            
            return rentalRepository.save(rental);
            
        } catch (Exception e) {
            System.err.println("Error creating rental: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create rental: " + e.getMessage());
        }
    }
    
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Rental updateRentalStatus(Integer rentalId, Integer statusId) {
        try {
            System.out.println("=== UPDATE RENTAL STATUS DEBUG ===");
            System.out.println("Rental ID: " + rentalId + ", New Status ID: " + statusId);
            
            Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found: " + rentalId));
            
            System.out.println("Current rental status: " + rental.getRentalStatus().getStatusName());
            
            // Create status update with proper names
            RentalStatus status = new RentalStatus();
            status.setStatusId(statusId);
            
            switch (statusId) {
                case 1:
                    status.setStatusName("Rezerve Edildi");
                    status.setStatusDescription("Rezervasyon onaylandı, teslim alınmayı bekliyor");
                    System.out.println("Setting status to: Rezerve Edildi");
                    break;
                case 2:
                    status.setStatusName("Aktif");
                    status.setStatusDescription("Araç teslim alındı ve kullanımda");
                    System.out.println("Setting status to: Aktif");
                    break;
                case 3:
                    status.setStatusName("Gecikmis");
                    status.setStatusDescription("İade tarihi geçti, araç iade edilmedi");
                    System.out.println("Setting status to: Gecikmis");
                    break;
                case 4:
                    status.setStatusName("Tamamlandi");
                    status.setStatusDescription("Araç başarıyla iade edildi");
                    System.out.println("Setting status to: Tamamlandi");
                    
                    System.out.println("=== RENTAL COMPLETION DEBUG ===");
                    System.out.println("Rental ID: " + rentalId + " is being completed");
                    
                    // Vehicle'ı müsait duruma getir - stored procedure kullan
                    Vehicle vehicle = rental.getVehicle();
                    if (vehicle != null) {
                        System.out.println("Vehicle ID: " + vehicle.getVehicleId());
                        System.out.println("Current Vehicle Status: " + vehicle.getCurrentStatus().getStatusName());
                        
                        try {
                            // Stored procedure ile vehicle status'unu güncelle (1 = Müsait)
                            System.out.println("Calling stored procedure to update vehicle status...");
                            Map<String, Object> result = storedProcedureRepository.updateVehicleStatus(
                                vehicle.getVehicleId(), 
                                1, // Müsait status ID
                                "Kiralama tamamlandı - otomatik güncelleme", 
                                1 // System user ID
                            );
                            
                            System.out.println("Stored procedure result: " + result);
                            
                            Boolean isSuccess = (Boolean) result.get("IsSuccess");
                            if (isSuccess != null && isSuccess) {
                                System.out.println("✅ Vehicle " + vehicle.getVehicleId() + " status updated to available via stored procedure");
                            } else {
                                String errorMessage = (String) result.get("ErrorMessage");
                                System.err.println("❌ Failed to update vehicle status: " + errorMessage);
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error updating vehicle status via stored procedure: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("❌ Vehicle is null for rental " + rentalId);
                    }
                    
                    System.out.println("=== END RENTAL COMPLETION DEBUG ===");
                    break;
                case 5:
                    status.setStatusName("İptal Edildi");
                    status.setStatusDescription("Kullanıcı tarafından iptal edildi");
                    System.out.println("Setting status to: İptal Edildi");
                    // Vehicle'ı müsait duruma getir - stored procedure kullan
                    Vehicle cancelledVehicle = rental.getVehicle();
                    if (cancelledVehicle != null) {
                        try {
                            // Stored procedure ile vehicle status'unu güncelle (1 = Müsait)
                            Map<String, Object> result = storedProcedureRepository.updateVehicleStatus(
                                cancelledVehicle.getVehicleId(), 
                                1, // Müsait status ID
                                "Kiralama iptal edildi - otomatik güncelleme", 
                                1 // System user ID
                            );
                            
                            Boolean isSuccess = (Boolean) result.get("IsSuccess");
                            if (isSuccess != null && isSuccess) {
                                System.out.println("Vehicle " + cancelledVehicle.getVehicleId() + " status updated to available via stored procedure");
                            } else {
                                String errorMessage = (String) result.get("ErrorMessage");
                                System.err.println("Failed to update vehicle status: " + errorMessage);
                            }
                        } catch (Exception e) {
                            System.err.println("Error updating vehicle status via stored procedure: " + e.getMessage());
                        }
                    }
                    break;
                case 6:
                    status.setStatusName("Odendi");
                    status.setStatusDescription("Kiralama tamamlandı ve ödeme alındı");
                    System.out.println("Setting status to: Odendi");
                    break;
                default:
                    status.setStatusName("Güncellenmiş");
                    status.setStatusDescription("Durum güncellendi");
            }
            
            // Use direct SQL update to avoid JPA caching issues
            System.out.println("🔄 Using direct SQL update for rental status...");
            int updatedRows = rentalRepository.updateRentalStatusDirect(rentalId, statusId);
            System.out.println("📊 Updated rows: " + updatedRows);
            
            if (updatedRows == 0) {
                throw new RuntimeException("No rows updated for rental: " + rentalId);
            }
            
            // Force flush to ensure database is updated
            rentalRepository.flush();
            
            // Re-fetch from database to ensure we have the latest data
            Rental refreshedRental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found after update: " + rentalId));
            
            System.out.println("✅ Rental status updated successfully!");
            System.out.println("Final status: " + refreshedRental.getRentalStatus().getStatusName());
            System.out.println("=== END UPDATE RENTAL STATUS DEBUG ===");
            
            return refreshedRental;
            
        } catch (Exception e) {
            System.err.println("❌ Error updating rental status: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to prevent rollback
            // Instead, try to recover or return current state
            Rental currentRental = rentalRepository.findById(rentalId).orElse(null);
            if (currentRental != null) {
                System.err.println("❌ Returning current rental state due to error");
                return currentRental;
            }
            throw new RuntimeException("Failed to update rental status: " + e.getMessage());
        }
    }
    
    // Business logic methods
    public Rental createRental(Rental rental) {
        // Check vehicle availability
        List<Rental> conflictingRentals = rentalRepository.findConflictingRentals(
            rental.getVehicle().getVehicleId(),
            rental.getPlannedPickupDate(),
            rental.getPlannedReturnDate()
        );
        
        if (!conflictingRentals.isEmpty()) {
            throw new RuntimeException("Vehicle is not available for the selected dates");
        }
        
        // Calculate total amount
        calculateRentalCost(rental);
        
        return rentalRepository.save(rental);
    }
    
    public Rental getRentalById(Integer rentalId) {
        return rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RuntimeException("Rental not found with id: " + rentalId));
    }
    
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }
    
    public List<Rental> getRentalsByCustomer(Integer customerId) {
        return rentalRepository.findByCustomerId(customerId);
    }
    
    public List<Rental> getRentalsByVehicle(Integer vehicleId) {
        return rentalRepository.findByVehicleId(vehicleId);
    }
    
    public List<Rental> getRentalsByStatus(String statusName) {
        return rentalRepository.findByStatusName(statusName);
    }
    
    public List<Rental> getActiveRentals() {
        return rentalRepository.findByStatusName("Active");
    }
    
    public List<Rental> getRentalsByPickupDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return rentalRepository.findByPickupDateRange(startDate, endDate);
    }
    
    public List<Rental> getRentalsByReturnDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return rentalRepository.findByReturnDateRange(startDate, endDate);
    }
    
    public List<Rental> getOverdueRentals() {
        return rentalRepository.findOverdueRentals(LocalDateTime.now());
    }
    
    public List<Rental> getRentalsByLocation(Integer locationId) {
        return rentalRepository.findByLocationId(locationId);
    }
    
    public List<Rental> searchRentals(String searchTerm) {
        // Implementation for searching rentals
        return rentalRepository.findAll(); // Placeholder
    }
    
    public Object getRevenueReport(LocalDateTime startDate, LocalDateTime endDate, Integer locationId) {
        // Implementation for revenue report
        return "Revenue report placeholder";
    }
    
    public Rental processPickup(Integer rentalId, LocalDateTime actualPickupDate) {
        Rental rental = getRentalById(rentalId);
        
        if (!"Reserved".equals(rental.getRentalStatus().getStatusName())) {
            throw new RuntimeException("Rental is not in reserved status");
        }
        
        rental.setActualPickupDate(actualPickupDate);
        // Update status to Active
        // rental.setRentalStatus(rentalStatusService.getStatusByName("Active"));
        
        return rentalRepository.save(rental);
    }
    
    public Rental processReturn(Integer rentalId, LocalDateTime actualReturnDate, Integer mileage, String damageNotes) {
        Rental rental = getRentalById(rentalId);
        
        if (!"Active".equals(rental.getRentalStatus().getStatusName())) {
            throw new RuntimeException("Rental is not in active status");
        }
        
        rental.setActualReturnDate(actualReturnDate);
        if (damageNotes != null) {
            rental.setNotes(rental.getNotes() + "\nReturn Notes: " + damageNotes);
        }
        
        // Update vehicle mileage
        Vehicle vehicle = rental.getVehicle();
        vehicle.setMileage(mileage);
        
        // Check for late return and calculate additional charges
        if (rental.getActualReturnDate().isAfter(rental.getPlannedReturnDate())) {
            calculateLateFees(rental);
        }
        
        // Update status to Completed
        // rental.setRentalStatus(rentalStatusService.getStatusByName("Completed"));
        
        return rentalRepository.save(rental);
    }
    
    public Rental updateRental(Integer rentalId, Rental rentalDetails) {
        Rental rental = getRentalById(rentalId);
        
        rental.setPlannedPickupDate(rentalDetails.getPlannedPickupDate());
        rental.setPlannedReturnDate(rentalDetails.getPlannedReturnDate());
        rental.setPickupLocation(rentalDetails.getPickupLocation());
        rental.setReturnLocation(rentalDetails.getReturnLocation());
        rental.setNotes(rentalDetails.getNotes());
        
        // Recalculate cost if dates changed
        calculateRentalCost(rental);
        
        return rentalRepository.save(rental);
    }
    
    public Rental pickupVehicle(Integer rentalId) {
        return processPickup(rentalId, LocalDateTime.now());
    }
    
    public Rental returnVehicle(Integer rentalId, Integer mileage, String notes) {
        return processReturn(rentalId, LocalDateTime.now(), mileage, notes);
    }
    
    public Rental cancelRental(Integer rentalId, String reason) {
        Rental rental = getRentalById(rentalId);
        
        if ("Active".equals(rental.getRentalStatus().getStatusName()) || 
            "Completed".equals(rental.getRentalStatus().getStatusName())) {
            throw new RuntimeException("Cannot cancel an active or completed rental");
        }
        
        if (reason != null) {
            rental.setNotes(rental.getNotes() + "\nCancellation Reason: " + reason);
        }
        // rental.setRentalStatus(rentalStatusService.getStatusByName("Cancelled"));
        
        return rentalRepository.save(rental);
    }
    
    private void calculateRentalCost(Rental rental) {
        Vehicle vehicle = rental.getVehicle();
        LocalDate startDate = rental.getPlannedPickupDate().toLocalDate();
        LocalDate endDate = rental.getPlannedReturnDate().toLocalDate();
        
        // Use dynamic pricing
        BigDecimal baseAmount = pricingService.calculateDynamicPrice(
            vehicle.getDailyRentalRate(), 
            startDate, 
            endDate
        );
        rental.setBaseAmount(baseAmount);
        
        // Calculate tax (18% VAT)
        BigDecimal taxAmount = baseAmount.multiply(BigDecimal.valueOf(0.18));
        rental.setTaxAmount(taxAmount);
        
        // Apply discount if any
        BigDecimal discountAmount = rental.getDiscountAmount() != null ? 
            rental.getDiscountAmount() : BigDecimal.ZERO;
        
        BigDecimal totalAmount = baseAmount.add(taxAmount).subtract(discountAmount);
        rental.setTotalAmount(totalAmount);
    }
    
    private void calculateLateFees(Rental rental) {
        long lateHours = ChronoUnit.HOURS.between(
            rental.getPlannedReturnDate(),
            rental.getActualReturnDate()
        );
        
        if (lateHours > 0) {
            // Calculate late fee: 10% of daily rate per hour
            BigDecimal lateFeePerHour = rental.getVehicle().getDailyRentalRate()
                .multiply(BigDecimal.valueOf(0.10));
            BigDecimal totalLateFee = lateFeePerHour.multiply(BigDecimal.valueOf(lateHours));
            
            rental.setTotalAmount(rental.getTotalAmount().add(totalLateFee));
        }
    }
    
    public void deleteRental(Integer rentalId) {
        deleteById(rentalId);
    }

    /**
     * Kullanıcı kimlik doğrulama
     */
    public Map<String, Object> authenticateUser(String username, String password) {
        return storedProcedureRepository.authenticateUser(username, password);
    }

    /**
     * Mevcut araçları arama
     */
    @Transactional(readOnly = true)
    public List<AvailableVehicle> searchAvailableVehicles(String cityName, String categoryName,
                                                         String brandName, BigDecimal maxRate,
                                                         String fuelType, String transmissionType) {
        return availableVehicleRepository.findWithFilters(
                cityName, categoryName, brandName, maxRate, fuelType, transmissionType);
    }

    /**
     * Araç müsaitlik kontrolü
     */
    @Transactional(readOnly = true)
    public boolean isVehicleAvailable(Integer vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return functionRepository.isVehicleAvailable(vehicleId, startDate, endDate);
    }

    /**
     * Kiralama süresi hesaplama
     */
    @Transactional(readOnly = true)
    public Integer calculateRentalDuration(LocalDateTime startDate, LocalDateTime endDate) {
        return functionRepository.calculateRentalDuration(startDate, endDate);
    }

    /**
     * İndirim hesaplama
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Integer customerId, Integer rentalDays) {
        return functionRepository.calculateDiscountPercentage(customerId, rentalDays);
    }

    /**
     * Müşteri sadakat seviyesi
     */
    @Transactional(readOnly = true)
    public String getCustomerLoyaltyTier(Integer customerId) {
        return functionRepository.getCustomerLoyaltyTier(customerId);
    }

    /**
     * Yeni kiralama oluştur
     */
    public Map<String, Object> createRental(Integer customerId, Integer vehicleId,
                                          Integer pickupLocationId, Integer returnLocationId,
                                          LocalDateTime plannedPickupDate, LocalDateTime plannedReturnDate,
                                          Integer createdBy) {
        return storedProcedureRepository.createRental(
                customerId, vehicleId, pickupLocationId, returnLocationId,
                plannedPickupDate, plannedReturnDate, createdBy);
    }

    /**
     * Araç iade işlemi
     */
    public Map<String, Object> processVehicleReturn(Integer rentalId, LocalDateTime actualReturnDate,
                                                   Integer mileage, String damageNotes, Integer processedBy) {
        return storedProcedureRepository.processVehicleReturn(
                rentalId, actualReturnDate, mileage, damageNotes, processedBy);
    }

    /**
     * Ödeme işlemi
     */
    public Map<String, Object> processPayment(Integer rentalId, Integer paymentMethodId,
                                            BigDecimal paymentAmount, String transactionReference,
                                            Integer processedBy) {
        return storedProcedureRepository.processPayment(
                rentalId, paymentMethodId, paymentAmount, transactionReference, processedBy);
    }

    /**
     * Payment tablosunun tüm alanlarını doldurup trigger'ın çalışmasını sağlayan ödeme işlemi
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Rental processPayment(Integer rentalId) {
        try {
            System.out.println("💳 SERVICE: Processing payment for rental ID: " + rentalId);
            
            Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found: " + rentalId));
            
            System.out.println("💳 SERVICE: Current rental status: " + rental.getRentalStatus().getStatusName());
            System.out.println("💳 SERVICE: Total amount: " + rental.getTotalAmount());
            
            // PaymentMethod entity'sini veritabanından getir
            PaymentMethod paymentMethod = paymentMethodRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
            
            // Processing fee hesapla
            BigDecimal processingFee = rental.getTotalAmount().multiply(paymentMethod.getProcessingFeePercentage());
            
            // Payment kaydı oluştur - tüm alanları doldur
            Payment payment = new Payment();
            payment.setRental(rental);                                          // RentalID
            payment.setPaymentMethod(paymentMethod);                           // PaymentMethodID
            payment.setPaymentAmount(rental.getTotalAmount());                 // PaymentAmount
            payment.setPaymentDate(LocalDateTime.now());                       // PaymentDate
            payment.setTransactionReference("PAY-" + System.currentTimeMillis()); // TransactionReference
            payment.setPaymentStatus("Completed");                             // PaymentStatus (trigger bunu arar)
            payment.setProcessingFee(processingFee);                          // ProcessingFee
            payment.setNotes("Online ödeme - otomatik işlem");               // Notes
            
            // CreatedBy alanını set et (System user ID: 1)
            User systemUser = userRepository.findById(1)
                .orElse(null);
            if (systemUser != null) {
                payment.setCreatedBy(systemUser);                             // CreatedBy
            }
            
            System.out.println("💳 SERVICE: Creating payment record with all fields:");
            System.out.println("  - Amount: " + payment.getPaymentAmount());
            System.out.println("  - Status: " + payment.getPaymentStatus());
            System.out.println("  - Processing Fee: " + payment.getProcessingFee());
            System.out.println("  - Transaction Ref: " + payment.getTransactionReference());
            
            // Payment'ı kaydet - trigger otomatik olarak rental status'unu güncelleyecek
            Payment savedPayment = paymentRepository.save(payment);
            System.out.println("💳 SERVICE: Payment record created with ID: " + savedPayment.getPaymentId());
            
            // Rental'ı yeniden yükle (trigger tarafından güncellenen status'u almak için)
            Rental updatedRental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found after payment: " + rentalId));
            System.out.println("💳 SERVICE: Payment processed, rental status updated to: " + updatedRental.getRentalStatus().getStatusName());
            
            return updatedRental;
            
        } catch (Exception e) {
            System.err.println("❌ SERVICE: Error processing payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }
    }

    /**
     * Araç durumu güncelleme
     */
    public Map<String, Object> updateVehicleStatus(Integer vehicleId, Integer newStatusId,
                                                  String notes, Integer updatedBy) {
        return storedProcedureRepository.updateVehicleStatus(vehicleId, newStatusId, notes, updatedBy);
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
     * Müşteri kiralama geçmişi
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCustomerRentalHistory(Integer customerId, Integer limit) {
        return functionRepository.getCustomerRentalHistory(customerId, limit);
    }

    /**
     * Araç bakım geçmişi
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVehicleMaintenanceHistory(Integer vehicleId) {
        return functionRepository.getVehicleMaintenanceHistory(vehicleId);
    }

    /**
     * Gecikmiş kiralamalar - Database function kullanarak
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOverdueRentalsFromFunction() {
        return functionRepository.getOverdueRentals();
    }

    /**
     * Popüler araçlar
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopularVehicles(Integer limit) {
        return functionRepository.getPopularVehicles(limit);
    }

    /**
     * Araç kullanım oranı
     */
    @Transactional(readOnly = true)
    public BigDecimal getVehicleUtilizationRate(Integer vehicleId, Integer days) {
        return functionRepository.calculateVehicleUtilizationRate(vehicleId, days);
    }

    /**
     * Para formatı
     */
    @Transactional(readOnly = true)
    public String formatCurrency(BigDecimal amount) {
        return functionRepository.formatCurrency(amount);
    }
}
