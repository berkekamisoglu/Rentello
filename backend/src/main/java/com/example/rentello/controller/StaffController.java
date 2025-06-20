package com.example.rentello.controller;

import com.example.rentello.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('CALISAN') or hasRole('YONETİCİ') or hasRole('MUDUR')")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @PostMapping("/vehicles/{vehicleId}/images")
    public ResponseEntity<Map<String, Object>> addVehicleImage(
            @PathVariable Integer vehicleId,
            @RequestBody Map<String, String> imageData
    ) {
        try {
            String imageUrl = imageData.get("imageUrl");
            boolean success = staffService.addVehicleImage(vehicleId, imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Resim başarıyla eklendi" : "Araç bulunamadı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Bir hata oluştu: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/vehicles/{vehicleId}/images")
    public ResponseEntity<List<String>> getVehicleImages(@PathVariable Integer vehicleId) {
        try {
            List<String> images = staffService.getVehicleImages(vehicleId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/vehicles/{vehicleId}/images")
    public ResponseEntity<Map<String, Object>> removeVehicleImage(
            @PathVariable Integer vehicleId,
            @RequestParam String imageUrl
    ) {
        try {
            boolean success = staffService.removeVehicleImage(vehicleId, imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Resim başarıyla silindi" : "Resim bulunamadı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Bir hata oluştu: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/vehicles")
    public ResponseEntity<?> getAllVehiclesForStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            return ResponseEntity.ok(staffService.getAllVehiclesForStaff(page, size));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 