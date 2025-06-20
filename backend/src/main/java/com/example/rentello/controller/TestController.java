package com.example.rentello.controller;

import com.example.rentello.entity.User;
import com.example.rentello.entity.UserRole;
import com.example.rentello.repository.UserRepository;
import com.example.rentello.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestController {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/create-test-user")
    public String createTestUser() {
        try {
            // Check if user already exists
            if (userRepository.findByUsername("testuser").isPresent()) {
                return "Test user already exists";
            }
            
            // Get or create customer role
            UserRole customerRole = userRoleRepository.findByRoleName("Musteri")
                    .orElseGet(() -> {
                        UserRole role = new UserRole();
                        role.setRoleName("Musteri");
                        role.setRoleDescription("Test Customer Role");
                        role.setPermissions("{\"profile\": \"update\", \"rentals\": \"read_own\"}");
                        return userRoleRepository.save(role);
                    });
            
            // Create test user
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@test.com");
            testUser.setPasswordHash(passwordEncoder.encode("test123"));
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setUserRole(customerRole);
            testUser.setIsActive(true);
            
            userRepository.save(testUser);
            
            return "Test user created successfully - Username: testuser, Password: test123";
            
        } catch (Exception e) {
            return "Error creating test user: " + e.getMessage();
        }
    }
    
    @PostMapping("/fix-existing-passwords")
    public String fixExistingPasswords() {
        try {
            // Fix berkekamisoglu user password
            User berkeUser = userRepository.findByUsername("berkekamisoglu").orElse(null);
            if (berkeUser != null) {
                berkeUser.setPasswordHash(passwordEncoder.encode("Berke0204"));
                userRepository.save(berkeUser);
            }
            
            // Fix admin password
            User adminUser = userRepository.findByUsername("admin").orElse(null);
            if (adminUser != null) {
                adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
                userRepository.save(adminUser);
            }
            
            return "Existing passwords fixed successfully";
        } catch (Exception e) {
            return "Error fixing passwords: " + e.getMessage();
        }
    }
    
    @GetMapping("/check-roles")
    public String checkRoles() {
        try {
            StringBuilder result = new StringBuilder("Available roles:\n");
            userRoleRepository.findAll().forEach(role -> 
                result.append("- ").append(role.getRoleName()).append("\n")
            );
            return result.toString();
        } catch (Exception e) {
            return "Error checking roles: " + e.getMessage();
        }
    }
}
