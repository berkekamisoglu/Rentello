package com.example.rentello.controller;

import com.example.rentello.dto.AdminUserManagementDto;
import com.example.rentello.dto.AdminVehicleManagementDto;
import com.example.rentello.dto.DashboardStatsDto;
import com.example.rentello.dto.AdminRentalDto;
import com.example.rentello.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('YONETİCİ') or hasRole('MUDUR')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        try {
            System.out.println("🔍 CONTROLLER: Dashboard stats request received");
            DashboardStatsDto stats = adminService.getDashboardStats();
            System.out.println("✅ CONTROLLER: Dashboard stats returned successfully");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ CONTROLLER: Error in getDashboardStats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserManagementDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        try {
            Page<AdminUserManagementDto> users = adminService.getAllUsers(page, size, sortBy, sortDirection);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users/search")
    public ResponseEntity<Page<AdminUserManagementDto>> searchUsers(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<AdminUserManagementDto> users = adminService.searchUsers(searchTerm, page, size);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Integer userId) {
        try {
            boolean success = adminService.toggleUserStatus(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Kullanıcı durumu güncellendi" : "Kullanıcı bulunamadı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Bir hata oluştu");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/users/{userId}/role/{roleId}")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Integer userId,
            @PathVariable Integer roleId
    ) {
        try {
            boolean success = adminService.updateUserRole(userId, roleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Kullanıcı rolü güncellendi" : "Kullanıcı veya rol bulunamadı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Bir hata oluştu");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/users/{userId}/password")
    public ResponseEntity<Map<String, String>> resetUserPassword(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String newPassword = request.get("newPassword");
            adminService.resetUserPassword(userId, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Şifre başarıyla sıfırlandı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Şifre sıfırlama hatası: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Integer userId) {
        try {
            adminService.deleteUser(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Kullanıcı başarıyla silindi");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Kullanıcı silme hatası: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userRequest) {
        try {
            AdminUserManagementDto newUserDto = adminService.createUser(userRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Kullanıcı başarıyla oluşturuldu");
            response.put("user", newUserDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Kullanıcı oluşturma hatası: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            return ResponseEntity.ok(adminService.getAllRoles());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Page<AdminVehicleManagementDto>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        try {
            Page<AdminVehicleManagementDto> vehicles = adminService.getAllVehicles(page, size, sortBy, sortDirection);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/vehicles/search")
    public ResponseEntity<Page<AdminVehicleManagementDto>> searchVehicles(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<AdminVehicleManagementDto> vehicles = adminService.searchVehicles(searchTerm, page, size);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/vehicles/{vehicleId}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleVehicleStatus(@PathVariable Integer vehicleId) {
        try {
            boolean success = adminService.toggleVehicleStatus(vehicleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Araç durumu güncellendi" : "Araç bulunamadı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Bir hata oluştu");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/vehicles")
    public ResponseEntity<Map<String, Object>> createVehicle(@RequestBody AdminVehicleManagementDto vehicleDto) {
        try {
            AdminVehicleManagementDto createdVehicle = adminService.createVehicle(vehicleDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Araç başarıyla oluşturuldu");
            response.put("vehicle", createdVehicle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Araç oluşturulurken hata: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Map<String, Object>> updateVehicle(
            @PathVariable Integer vehicleId,
            @RequestBody AdminVehicleManagementDto vehicleDto
    ) {
        try {
            AdminVehicleManagementDto updatedVehicle = adminService.updateVehicle(vehicleId, vehicleDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Araç başarıyla güncellendi");
            response.put("vehicle", updatedVehicle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Araç güncellenirken hata: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Map<String, Object>> deleteVehicle(@PathVariable Integer vehicleId) {
        try {
            boolean success = adminService.deleteVehicle(vehicleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Araç başarıyla silindi" : "Araç bulunamadı veya silinemez");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Araç silinirken hata: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/vehicles/references")
    public ResponseEntity<Map<String, Object>> getVehicleReferences() {
        try {
            Map<String, Object> references = adminService.getVehicleReferences();
            return ResponseEntity.ok(references);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rentals")
    public ResponseEntity<Page<AdminRentalDto>> getAllRentals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        try {
            Page<AdminRentalDto> rentals = adminService.getAllRentals(page, size, sortBy, sortDirection);
            return ResponseEntity.ok(rentals);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rentals/search")
    public ResponseEntity<Page<AdminRentalDto>> searchRentals(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<AdminRentalDto> rentals = adminService.searchRentals(searchTerm, page, size);
            return ResponseEntity.ok(rentals);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 