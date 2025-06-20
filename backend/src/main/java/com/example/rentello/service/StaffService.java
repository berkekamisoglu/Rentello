package com.example.rentello.service;

import com.example.rentello.entity.Vehicle;
import com.example.rentello.repository.VehicleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StaffService {

    @Autowired
    private VehicleRepository vehicleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean addVehicleImage(Integer vehicleId, String imageUrl) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
            if (vehicle == null) {
                return false;
            }

            List<String> imageUrls = getVehicleImagesList(vehicle);
            if (!imageUrls.contains(imageUrl)) {
                imageUrls.add(imageUrl);
                vehicle.setImageUrls(objectMapper.writeValueAsString(imageUrls));
                vehicleRepository.save(vehicle);
                System.out.println("DEBUG: Added image " + imageUrl + " to vehicle " + vehicleId);
                System.out.println("DEBUG: Current images for vehicle " + vehicleId + ": " + imageUrls);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getVehicleImages(Integer vehicleId) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
            if (vehicle == null) {
                return new ArrayList<>();
            }
            List<String> images = getVehicleImagesList(vehicle);
            System.out.println("DEBUG: Getting images for vehicle " + vehicleId + ": " + images);
            return images;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean removeVehicleImage(Integer vehicleId, String imageUrl) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
            if (vehicle == null) {
                return false;
            }

            List<String> imageUrls = getVehicleImagesList(vehicle);
            if (imageUrls.remove(imageUrl)) {
                vehicle.setImageUrls(objectMapper.writeValueAsString(imageUrls));
                vehicleRepository.save(vehicle);
                System.out.println("DEBUG: Removed image " + imageUrl + " from vehicle " + vehicleId);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Page<Vehicle> getAllVehiclesForStaff(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return vehicleRepository.findAll(pageable);
    }

    private List<String> getVehicleImagesList(Vehicle vehicle) {
        try {
            if (vehicle.getImageUrls() == null || vehicle.getImageUrls().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(vehicle.getImageUrls(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
} 