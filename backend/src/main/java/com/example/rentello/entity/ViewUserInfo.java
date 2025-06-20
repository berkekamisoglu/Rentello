package com.example.rentello.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "vw_UserInfo")
public class ViewUserInfo {
    
    @Id
    @Column(name = "UserID")
    private Integer userId;
    
    @Column(name = "Username")
    private String username;
    
    @Column(name = "Email")
    private String email;
    
    @Column(name = "FirstName")
    private String firstName;
    
    @Column(name = "LastName")
    private String lastName;
    
    @Column(name = "PhoneNumber")
    private String phoneNumber;
    
    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;
    
    @Column(name = "RoleName")
    private String roleName;
    
    @Column(name = "CityName")
    private String cityName;
    
    @Column(name = "CountryName")
    private String countryName;
    
    @Column(name = "IsActive")
    private Boolean isActive;
    
    @Column(name = "LastLoginDate")
    private LocalDateTime lastLoginDate;
    
    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
} 