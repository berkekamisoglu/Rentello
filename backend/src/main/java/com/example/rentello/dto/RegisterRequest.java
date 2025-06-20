package com.example.rentello.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Phone number should be valid")
    private String phoneNumber;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    private String nationalId;
    
    private Integer cityId;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[MASKED]'" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", nationalId='" + nationalId + '\'' +
                ", cityId=" + cityId +
                ", address='" + address + '\'' +
                '}';
    }
} 