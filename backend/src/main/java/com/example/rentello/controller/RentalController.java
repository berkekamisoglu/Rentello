package com.example.rentello.controller;

import com.example.rentello.entity.Rental;
import com.example.rentello.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RentalController {
    
    private final RentalService rentalService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Page<Rental>> getAllRentals(Pageable pageable) {
        return ResponseEntity.ok(rentalService.getAllRentals(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Integer id) {
        return rentalService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/my-rentals")
    public ResponseEntity<List<Rental>> getMyRentals() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        try {
            List<Rental> rentals = rentalService.getRentalsByUsername(username);
            return ResponseEntity.ok(rentals);
        } catch (Exception e) {
            System.err.println("Error fetching user rentals: " + e.getMessage());
            return ResponseEntity.ok(List.of()); // Return empty list if error
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rental>> getUserRentals(@PathVariable Integer userId) {
        try {
            List<Rental> rentals = rentalService.getRentalsByCustomer(userId);
            return ResponseEntity.ok(rentals);
        } catch (Exception e) {
            System.err.println("Error fetching user rentals: " + e.getMessage());
            return ResponseEntity.ok(List.of()); // Return empty list if error
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Rental>> getRentalsByCustomer(@PathVariable Integer customerId) {
        List<Rental> rentals = rentalService.getRentalsByCustomer(customerId);
        return ResponseEntity.ok(rentals);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Rental>> getActiveRentals() {
        List<Rental> activeRentals = rentalService.getActiveRentals();
        return ResponseEntity.ok(activeRentals);
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Rental>> getOverdueRentals() {
        List<Rental> overdueRentals = rentalService.getOverdueRentals();
        return ResponseEntity.ok(overdueRentals);
    }
    
    @PostMapping
    public ResponseEntity<Rental> createRental(@Valid @RequestBody Map<String, Object> rentalData) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            Rental savedRental = rentalService.createRentalFromRequest(rentalData, username);
            return ResponseEntity.ok(savedRental);
        } catch (Exception e) {
            System.err.println("Error creating rental: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Rental> cancelRental(@PathVariable Integer id) {
        try {
            Rental cancelledRental = rentalService.cancelRental(id, "İptal edildi");
            return ResponseEntity.ok(cancelledRental);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Rental> updateRentalStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> statusData) {
        try {
            System.out.println("🔥 CONTROLLER: updateRentalStatus called for rental ID: " + id);
            System.out.println("🔥 CONTROLLER: statusData: " + statusData);
            
            Integer statusId = statusData.get("statusId");
            System.out.println("🔥 CONTROLLER: extracted statusId: " + statusId);
            
            System.out.println("🔥 CONTROLLER: calling rentalService.updateRentalStatus...");
            Rental updatedRental = rentalService.updateRentalStatus(id, statusId);
            
            System.out.println("🔥 CONTROLLER: rental status updated successfully");
            return ResponseEntity.ok(updatedRental);
        } catch (Exception e) {
            System.err.println("❌ CONTROLLER: Error updating rental status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/payment")
    public ResponseEntity<Rental> processPayment(@PathVariable Integer id, @RequestBody Map<String, Object> paymentData) {
        try {
            System.out.println("💳 CONTROLLER: processPayment called for rental ID: " + id);
            System.out.println("💳 CONTROLLER: paymentData: " + paymentData);
            
            // Ödeme işlemi - gerçek uygulamada ödeme gateway'i entegrasyonu olacak
            Rental updatedRental = rentalService.processPayment(id);
            
            System.out.println("💳 CONTROLLER: payment processed successfully");
            return ResponseEntity.ok(updatedRental);
        } catch (Exception e) {
            System.err.println("❌ CONTROLLER: Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Rental> updateRental(@PathVariable Integer id, @Valid @RequestBody Rental rental) {
        return rentalService.findById(id)
                .map(existingRental -> {
                    rental.setRentalId(id);
                    return ResponseEntity.ok(rentalService.updateRental(rental));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/pickup")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Rental> processPickup(
            @PathVariable Integer id,
            @RequestParam LocalDateTime actualPickupDate) {
        
        try {
            Rental updatedRental = rentalService.processPickup(id, actualPickupDate);
            return ResponseEntity.ok(updatedRental);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Rental> processReturn(
            @PathVariable Integer id,
            @RequestParam LocalDateTime actualReturnDate,
            @RequestParam Integer mileage,
            @RequestParam(required = false) String damageNotes) {
        
        try {
            Rental updatedRental = rentalService.processReturn(id, actualReturnDate, mileage, damageNotes);
            return ResponseEntity.ok(updatedRental);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Rental>> getRentalsByVehicle(@PathVariable Integer vehicleId) {
        List<Rental> rentals = rentalService.getRentalsByVehicle(vehicleId);
        return ResponseEntity.ok(rentals);
    }
    
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Object> getRevenueReport(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Integer locationId) {
        
        Object revenueReport = rentalService.getRevenueReport(startDate, endDate, locationId);
        return ResponseEntity.ok(revenueReport);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Rental>> searchRentals(@RequestParam String searchTerm) {
        List<Rental> rentals = rentalService.searchRentals(searchTerm);
        return ResponseEntity.ok(rentals);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRental(@PathVariable Integer id) {
        return rentalService.findById(id)
                .map(rental -> {
                    rentalService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 