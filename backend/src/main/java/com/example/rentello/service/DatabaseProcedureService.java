package com.example.rentello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class DatabaseProcedureService {

    @Autowired
    private EntityManager entityManager;

    /**
     * Kullanıcı kimlik doğrulama için stored procedure
     */
    public AuthResult authenticateUser(String username, String password) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_AuthenticateUser");
        
        query.registerStoredProcedureParameter("Username", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("Password", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("UserID", Integer.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("RoleName", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("IsSuccess", Boolean.class, ParameterMode.OUT);
        
        query.setParameter("Username", username);
        query.setParameter("Password", password);
        
        query.execute();
        
        AuthResult result = new AuthResult();
        result.setUserId((Integer) query.getOutputParameterValue("UserID"));
        result.setRoleName((String) query.getOutputParameterValue("RoleName"));
        result.setSuccess((Boolean) query.getOutputParameterValue("IsSuccess"));
        
        return result;
    }

    /**
     * Yeni kiralama oluşturma için stored procedure
     */
    public RentalCreationResult createRental(Integer customerId, Integer vehicleId, 
                                           Integer pickupLocationId, Integer returnLocationId,
                                           LocalDateTime plannedPickupDate, LocalDateTime plannedReturnDate,
                                           Integer createdBy) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_CreateRental");
        
        query.registerStoredProcedureParameter("CustomerID", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("VehicleID", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PickupLocationID", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("ReturnLocationID", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PlannedPickupDate", LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PlannedReturnDate", LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("CreatedBy", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RentalID", Integer.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("TotalAmount", BigDecimal.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("IsSuccess", Boolean.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("ErrorMessage", String.class, ParameterMode.OUT);
        
        query.setParameter("CustomerID", customerId);
        query.setParameter("VehicleID", vehicleId);
        query.setParameter("PickupLocationID", pickupLocationId);
        query.setParameter("ReturnLocationID", returnLocationId);
        query.setParameter("PlannedPickupDate", plannedPickupDate);
        query.setParameter("PlannedReturnDate", plannedReturnDate);
        query.setParameter("CreatedBy", createdBy);
        
        query.execute();
        
        RentalCreationResult result = new RentalCreationResult();
        result.setRentalId((Integer) query.getOutputParameterValue("RentalID"));
        result.setTotalAmount((BigDecimal) query.getOutputParameterValue("TotalAmount"));
        result.setSuccess((Boolean) query.getOutputParameterValue("IsSuccess"));
        result.setErrorMessage((String) query.getOutputParameterValue("ErrorMessage"));
        
        return result;
    }

    /**
     * Araç iade işlemi için stored procedure
     */
    public VehicleReturnResult processVehicleReturn(Integer rentalId, LocalDateTime actualReturnDate, 
                                                  Integer mileage, String damageNotes, Integer processedBy) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_ProcessVehicleReturn");
        
        query.registerStoredProcedureParameter("RentalID", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("ActualReturnDate", LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("Mileage", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("DamageNotes", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("ProcessedBy", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("IsSuccess", Boolean.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("LateFee", BigDecimal.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("ErrorMessage", String.class, ParameterMode.OUT);
        
        query.setParameter("RentalID", rentalId);
        query.setParameter("ActualReturnDate", actualReturnDate);
        query.setParameter("Mileage", mileage);
        query.setParameter("DamageNotes", damageNotes);
        query.setParameter("ProcessedBy", processedBy);
        
        query.execute();
        
        VehicleReturnResult result = new VehicleReturnResult();
        result.setSuccess((Boolean) query.getOutputParameterValue("IsSuccess"));
        result.setLateFee((BigDecimal) query.getOutputParameterValue("LateFee"));
        result.setErrorMessage((String) query.getOutputParameterValue("ErrorMessage"));
        
        return result;
    }

    // Result DTOs
    public static class AuthResult {
        private Integer userId;
        private String roleName;
        private Boolean success;

        // Getters and Setters
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
    }

    public static class RentalCreationResult {
        private Integer rentalId;
        private BigDecimal totalAmount;
        private Boolean success;
        private String errorMessage;

        // Getters and Setters
        public Integer getRentalId() { return rentalId; }
        public void setRentalId(Integer rentalId) { this.rentalId = rentalId; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class VehicleReturnResult {
        private Boolean success;
        private BigDecimal lateFee;
        private String errorMessage;

        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public BigDecimal getLateFee() { return lateFee; }
        public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
} 