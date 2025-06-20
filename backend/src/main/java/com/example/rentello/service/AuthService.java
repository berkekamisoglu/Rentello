package com.example.rentello.service;

import com.example.rentello.dto.LoginRequest;
import com.example.rentello.dto.LoginResponse;
import com.example.rentello.dto.RegisterRequest;
import com.example.rentello.entity.User;
import com.example.rentello.entity.UserRole;
import com.example.rentello.repository.UserRepository;
import com.example.rentello.repository.UserRoleRepository;
import com.example.rentello.repository.CityRepository;
import com.example.rentello.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // User username could be email or actual username
            Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(userDetails.getUsername());
            }
            User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update last login date
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);
            
            return new LoginResponse(token, user);
            
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }
    
    public User register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
          // Get default customer role
        UserRole customerRole = userRoleRepository.findByRoleName("Musteri")
                .orElseThrow(() -> new RuntimeException("Musteri role not found"));
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setDateOfBirth(registerRequest.getDateOfBirth());
        user.setUserRole(customerRole);
        user.setAddress(registerRequest.getAddress());
        user.setIsActive(true);
        
        // Set city if provided
        if (registerRequest.getCityId() != null) {
            cityRepository.findById(registerRequest.getCityId())
                    .ifPresent(user::setCity);
        }
        
        // Encrypt national ID (simplified - in real implementation use proper encryption)
        if (registerRequest.getNationalId() != null) {
            user.setNationalId(registerRequest.getNationalId().getBytes());
        }
        
        return userRepository.save(user);
    }
    
    public void logout(String token) {
        // In a real implementation, you would add the token to a blacklist
        // For now, we'll just do nothing as JWT tokens are stateless
        // You could store blacklisted tokens in Redis or database
    }
    
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            return jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }
    
    public User getCurrentUser(String usernameOrEmail) {
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        return userOpt.orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public LoginResponse refreshToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            if (jwtUtil.validateToken(token, userDetails)) {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isEmpty()) {
                    userOpt = userRepository.findByEmail(username);
                }
                User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
                
                String newToken = jwtUtil.generateToken(userDetails);
                
                return new LoginResponse(newToken, user);
            } else {
                throw new RuntimeException("Invalid token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed");
        }
    }
    
    public void changePassword(String usernameOrEmail, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid old password");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // In a real implementation, you would:
        // 1. Generate a secure random token
        // 2. Store it in database with expiration time
        // 3. Send email with reset link
        // For now, we'll just throw an exception to indicate the feature is not implemented
        throw new RuntimeException("Password reset feature not implemented yet");
    }
} 