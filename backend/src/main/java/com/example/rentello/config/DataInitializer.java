package com.example.rentello.config;

import com.example.rentello.entity.UserRole;
import com.example.rentello.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeUserRoles();
    }

    private void initializeUserRoles() {
        // Create Musteri role if it doesn't exist
        if (userRoleRepository.findByRoleName("Musteri").isEmpty()) {
            UserRole musteriRole = new UserRole();
            musteriRole.setRoleName("Musteri");
            musteriRole.setRoleDescription("Kiralama Musterisi");
            musteriRole.setPermissions("{\"profile\": \"update\", \"rentals\": \"read_own\"}");
            userRoleRepository.save(musteriRole);
            System.out.println("Musteri role created successfully!");
        }

        // Create other basic roles if they don't exist
        if (userRoleRepository.findByRoleName("Yonetici").isEmpty()) {
            UserRole yoneticiRole = new UserRole();
            yoneticiRole.setRoleName("Yonetici");
            yoneticiRole.setRoleDescription("Sistem Yoneticisi");
            yoneticiRole.setPermissions("{\"all\": true}");
            userRoleRepository.save(yoneticiRole);
            System.out.println("Yonetici role created successfully!");
        }

        if (userRoleRepository.findByRoleName("Mudur").isEmpty()) {
            UserRole mudurRole = new UserRole();
            mudurRole.setRoleName("Mudur");
            mudurRole.setRoleDescription("Lokasyon Muduru");
            mudurRole.setPermissions("{\"users\": \"read\", \"rentals\": \"all\", \"vehicles\": \"all\", \"reports\": \"read\"}");
            userRoleRepository.save(mudurRole);
            System.out.println("Mudur role created successfully!");
        }

        if (userRoleRepository.findByRoleName("Calisan").isEmpty()) {
            UserRole calisanRole = new UserRole();
            calisanRole.setRoleName("Calisan");
            calisanRole.setRoleDescription("Musteri Hizmetleri Calisani");
            calisanRole.setPermissions("{\"rentals\": \"create_update\", \"customers\": \"read\", \"vehicles\": \"read\"}");
            userRoleRepository.save(calisanRole);
            System.out.println("Calisan role created successfully!");
        }
    }
} 