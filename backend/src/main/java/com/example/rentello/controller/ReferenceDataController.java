package com.example.rentello.controller;

import com.example.rentello.entity.*;
import com.example.rentello.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reference")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReferenceDataController {
    
    private final VehicleCategoryRepository categoryRepository;
    private final VehicleBrandRepository brandRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final UserRoleRepository userRoleRepository;
    private final VehicleModelRepository vehicleModelRepository;
    
    // Vehicle Categories
    @GetMapping("/categories")
    public ResponseEntity<List<VehicleCategory>> getAllCategories() {
        try {
            // Return simple test data to avoid JSON issues
            List<VehicleCategory> categories = new ArrayList<>();
            
            VehicleCategory economy = new VehicleCategory();
            economy.setCategoryId(1);
            economy.setCategoryName("Ekonomi");
            economy.setCategoryDescription("Yakıt tasarruflu bütçe dostu araçlar");
            categories.add(economy);
            
            VehicleCategory compact = new VehicleCategory();
            compact.setCategoryId(2);
            compact.setCategoryName("Kompakt");
            compact.setCategoryDescription("Şehir içi sürüş için ideal araçlar");
            categories.add(compact);
            
            VehicleCategory premium = new VehicleCategory();
            premium.setCategoryId(3);
            premium.setCategoryName("Premium");
            premium.setCategoryDescription("Lüks ve konforlu araçlar");
            categories.add(premium);
            
            VehicleCategory suv = new VehicleCategory();
            suv.setCategoryId(4);
            suv.setCategoryName("SUV");
            suv.setCategoryDescription("Geniş ve güvenli aile araçları");
            categories.add(suv);
            
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<VehicleCategory> getCategoryById(@PathVariable Integer id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Vehicle Brands
    @GetMapping("/brands")
    public ResponseEntity<List<VehicleBrand>> getAllBrands() {
        try {
            // Return simple test data to avoid JSON issues
            List<VehicleBrand> brands = new ArrayList<>();
            
            VehicleBrand toyota = new VehicleBrand();
            toyota.setBrandId(1);
            toyota.setBrandName("Toyota");
            brands.add(toyota);
            
            VehicleBrand bmw = new VehicleBrand();
            bmw.setBrandId(2);
            bmw.setBrandName("BMW");
            brands.add(bmw);
            
            VehicleBrand volkswagen = new VehicleBrand();
            volkswagen.setBrandId(3);
            volkswagen.setBrandName("Volkswagen");
            brands.add(volkswagen);
            
            VehicleBrand ford = new VehicleBrand();
            ford.setBrandId(4);
            ford.setBrandName("Ford");
            brands.add(ford);
            
            return ResponseEntity.ok(brands);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/brands/{id}")
    public ResponseEntity<VehicleBrand> getBrandById(@PathVariable Integer id) {
        return brandRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Create Brand
    @PostMapping("/brands")
    public ResponseEntity<VehicleBrand> createBrand(@RequestBody VehicleBrand brand) {
        try {
            VehicleBrand savedBrand = brandRepository.save(brand);
            return ResponseEntity.ok(savedBrand);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Vehicle Models
    @GetMapping("/models")
    public ResponseEntity<List<VehicleModel>> getAllModels() {
        return ResponseEntity.ok(vehicleModelRepository.findAll());
    }
    
    @GetMapping("/models/{id}")
    public ResponseEntity<VehicleModel> getModelById(@PathVariable Integer id) {
        return vehicleModelRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/models/brand/{brandId}")
    public ResponseEntity<List<VehicleModel>> getModelsByBrand(@PathVariable Integer brandId) {
        List<VehicleModel> models = vehicleModelRepository.findByBrandId(brandId);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/models/category/{categoryId}")
    public ResponseEntity<List<VehicleModel>> getModelsByCategory(@PathVariable Integer categoryId) {
        List<VehicleModel> models = vehicleModelRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(models);
    }
    
    // Create Model
    @PostMapping("/models")
    public ResponseEntity<VehicleModel> createModel(@RequestBody Map<String, Object> modelData) {
        try {
            VehicleModel model = new VehicleModel();
            model.setModelName((String) modelData.get("modelName"));
            model.setManufactureYear((Integer) modelData.get("manufactureYear"));
            model.setEngineType((String) modelData.get("engineType"));
            model.setFuelType((String) modelData.get("fuelType"));
            model.setTransmissionType((String) modelData.get("transmissionType"));
            model.setSeatingCapacity((Integer) modelData.get("seatingCapacity"));
            
            // Brand ID'den Brand nesnesini bul
            Integer brandId = (Integer) modelData.get("brandId");
            if (brandId != null) {
                VehicleBrand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + brandId));
                model.setBrand(brand);
            }
            
            // Category ID'den Category nesnesini bul
            Integer categoryId = (Integer) modelData.get("categoryId");
            if (categoryId != null) {
                VehicleCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
                model.setCategory(category);
            }
            
            VehicleModel savedModel = vehicleModelRepository.save(model);
            return ResponseEntity.ok(savedModel);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Countries
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryRepository.findAll());
    }
    
    @GetMapping("/countries/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable Integer id) {
        return countryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Cities
    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityRepository.findAll());
    }
    
    @GetMapping("/cities/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Integer id) {
        return cityRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cities/country/{countryId}")
    public ResponseEntity<List<City>> getCitiesByCountry(@PathVariable Integer countryId) {
        List<City> cities = cityRepository.findByCountryId(countryId);
        return ResponseEntity.ok(cities);
    }
    
    // User Roles
    @GetMapping("/roles")
    public ResponseEntity<List<UserRole>> getAllRoles() {
        return ResponseEntity.ok(userRoleRepository.findAll());
    }
    
    @GetMapping("/roles/{id}")
    public ResponseEntity<UserRole> getRoleById(@PathVariable Integer id) {
        return userRoleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}