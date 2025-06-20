package com.example.rentello.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StoredProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Kullanıcı kimlik doğrulama
     */
    public Map<String, Object> authenticateUser(String username, String password) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_AuthenticateUser")
                .declareParameters(
                        new SqlParameter("Username", Types.NVARCHAR),
                        new SqlParameter("Password", Types.NVARCHAR),
                        new SqlOutParameter("UserID", Types.INTEGER),
                        new SqlOutParameter("RoleName", Types.NVARCHAR),
                        new SqlOutParameter("IsSuccess", Types.BIT)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("Username", username);
        inParams.put("Password", password);

        return jdbcCall.execute(inParams);
    }

    /**
     * Yeni kiralama oluşturma
     */
    public Map<String, Object> createRental(Integer customerId, Integer vehicleId, 
                                          Integer pickupLocationId, Integer returnLocationId,
                                          LocalDateTime plannedPickupDate, LocalDateTime plannedReturnDate,
                                          Integer createdBy) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_CreateRental")
                .declareParameters(
                        new SqlParameter("CustomerID", Types.INTEGER),
                        new SqlParameter("VehicleID", Types.INTEGER),
                        new SqlParameter("PickupLocationID", Types.INTEGER),
                        new SqlParameter("ReturnLocationID", Types.INTEGER),
                        new SqlParameter("PlannedPickupDate", Types.TIMESTAMP),
                        new SqlParameter("PlannedReturnDate", Types.TIMESTAMP),
                        new SqlParameter("CreatedBy", Types.INTEGER),
                        new SqlOutParameter("RentalID", Types.INTEGER),
                        new SqlOutParameter("TotalAmount", Types.DECIMAL),
                        new SqlOutParameter("IsSuccess", Types.BIT),
                        new SqlOutParameter("ErrorMessage", Types.NVARCHAR)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("CustomerID", customerId);
        inParams.put("VehicleID", vehicleId);
        inParams.put("PickupLocationID", pickupLocationId);
        inParams.put("ReturnLocationID", returnLocationId);
        inParams.put("PlannedPickupDate", plannedPickupDate);
        inParams.put("PlannedReturnDate", plannedReturnDate);
        inParams.put("CreatedBy", createdBy);

        return jdbcCall.execute(inParams);
    }

    /**
     * Araç iade işlemi
     */
    public Map<String, Object> processVehicleReturn(Integer rentalId, LocalDateTime actualReturnDate,
                                                   Integer mileage, String damageNotes, Integer processedBy) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_ProcessVehicleReturn")
                .declareParameters(
                        new SqlParameter("RentalID", Types.INTEGER),
                        new SqlParameter("ActualReturnDate", Types.TIMESTAMP),
                        new SqlParameter("Mileage", Types.INTEGER),
                        new SqlParameter("DamageNotes", Types.NVARCHAR),
                        new SqlParameter("ProcessedBy", Types.INTEGER),
                        new SqlOutParameter("IsSuccess", Types.BIT),
                        new SqlOutParameter("LateFee", Types.DECIMAL),
                        new SqlOutParameter("ErrorMessage", Types.NVARCHAR)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("RentalID", rentalId);
        inParams.put("ActualReturnDate", actualReturnDate);
        inParams.put("Mileage", mileage);
        inParams.put("DamageNotes", damageNotes);
        inParams.put("ProcessedBy", processedBy);

        return jdbcCall.execute(inParams);
    }

    /**
     * Müşteri kaydı
     */
    public Map<String, Object> registerCustomer(String username, String email, String passwordHash,
                                               String firstName, String lastName, String phoneNumber,
                                               String dateOfBirth, String nationalId, Integer cityId, String address) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_RegisterCustomer")
                .declareParameters(
                        new SqlParameter("Username", Types.NVARCHAR),
                        new SqlParameter("Email", Types.NVARCHAR),
                        new SqlParameter("PasswordHash", Types.NVARCHAR),
                        new SqlParameter("FirstName", Types.NVARCHAR),
                        new SqlParameter("LastName", Types.NVARCHAR),
                        new SqlParameter("PhoneNumber", Types.NVARCHAR),
                        new SqlParameter("DateOfBirth", Types.DATE),
                        new SqlParameter("NationalID", Types.NVARCHAR),
                        new SqlParameter("CityID", Types.INTEGER),
                        new SqlParameter("Address", Types.NVARCHAR),
                        new SqlOutParameter("UserID", Types.INTEGER),
                        new SqlOutParameter("IsSuccess", Types.BIT),
                        new SqlOutParameter("ErrorMessage", Types.NVARCHAR)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("Username", username);
        inParams.put("Email", email);
        inParams.put("PasswordHash", passwordHash);
        inParams.put("FirstName", firstName);
        inParams.put("LastName", lastName);
        inParams.put("PhoneNumber", phoneNumber);
        inParams.put("DateOfBirth", dateOfBirth);
        inParams.put("NationalID", nationalId);
        inParams.put("CityID", cityId);
        inParams.put("Address", address);

        return jdbcCall.execute(inParams);
    }

    /**
     * Ödeme işlemi
     */
    public Map<String, Object> processPayment(Integer rentalId, Integer paymentMethodId,
                                             BigDecimal paymentAmount, String transactionReference,
                                             Integer processedBy) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_ProcessPayment")
                .declareParameters(
                        new SqlParameter("RentalID", Types.INTEGER),
                        new SqlParameter("PaymentMethodID", Types.INTEGER),
                        new SqlParameter("PaymentAmount", Types.DECIMAL),
                        new SqlParameter("TransactionReference", Types.NVARCHAR),
                        new SqlParameter("ProcessedBy", Types.INTEGER),
                        new SqlOutParameter("PaymentID", Types.INTEGER),
                        new SqlOutParameter("IsSuccess", Types.BIT),
                        new SqlOutParameter("ErrorMessage", Types.NVARCHAR)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("RentalID", rentalId);
        inParams.put("PaymentMethodID", paymentMethodId);
        inParams.put("PaymentAmount", paymentAmount);
        inParams.put("TransactionReference", transactionReference);
        inParams.put("ProcessedBy", processedBy);

        return jdbcCall.execute(inParams);
    }

    /**
     * Araç durumu güncelleme
     */
    public Map<String, Object> updateVehicleStatus(Integer vehicleId, Integer newStatusId,
                                                  String notes, Integer updatedBy) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_UpdateVehicleStatus")
                .declareParameters(
                        new SqlParameter("VehicleID", Types.INTEGER),
                        new SqlParameter("NewStatusID", Types.INTEGER),
                        new SqlParameter("Notes", Types.NVARCHAR),
                        new SqlParameter("UpdatedBy", Types.INTEGER),
                        new SqlOutParameter("IsSuccess", Types.BIT),
                        new SqlOutParameter("ErrorMessage", Types.NVARCHAR)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("VehicleID", vehicleId);
        inParams.put("NewStatusID", newStatusId);
        inParams.put("Notes", notes);
        inParams.put("UpdatedBy", updatedBy);

        return jdbcCall.execute(inParams);
    }
} 