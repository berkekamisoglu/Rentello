package com.example.rentello.dto;

import com.example.rentello.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String role;
    private Integer userId;
    private UserDto user;
    
    public LoginResponse(String token, String username, String email, String role, Integer userId) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }
    
    public LoginResponse(String token, User user) {
        this.token = token;
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getUserRole().getRoleName();
        this.userId = user.getUserId();
        
        // Create safe user DTO without circular references
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setDateOfBirth(user.getDateOfBirth());
        userDto.setAddress(user.getAddress());
        userDto.setIsActive(user.getIsActive());
        userDto.setRole(user.getUserRole().getRoleName());
        if (user.getCity() != null) {
            userDto.setCityName(user.getCity().getCityName());
        }
        this.user = userDto;
    }
} 