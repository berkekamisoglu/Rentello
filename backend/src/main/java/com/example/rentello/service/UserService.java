package com.example.rentello.service;

import com.example.rentello.entity.User;
import com.example.rentello.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by username first, then by email
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        
        return buildUserForAuthentication(user);
    }
    
    private UserDetails buildUserForAuthentication(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getUserRole().getRoleName().toUpperCase())
        );
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
    
    // Basic CRUD operations
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }
    
    public void deleteById(Integer userId) {
        userRepository.deleteById(userId);
    }
    
    // Business logic methods
    public User createUser(User user) {
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Encode password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        return userRepository.save(user);
    }
    
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }
    
    public List<User> getUsersByCity(Integer cityId) {
        return userRepository.findByCityId(cityId);
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findActiveUsers();
    }
    
    public User updateUser(Integer userId, User userDetails) {
        User user = getUserById(userId);
        
        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setDateOfBirth(userDetails.getDateOfBirth());
        user.setAddress(userDetails.getAddress());
        user.setCity(userDetails.getCity());
        
        return userRepository.save(user);
    }
    
    public User changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid old password");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    public User activateUser(Integer userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        return userRepository.save(user);
    }
    
    public User deactivateUser(Integer userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        return userRepository.save(user);
    }
    
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getIsActive() && passwordEncoder.matches(password, user.getPasswordHash());
        }
        return false;
    }
    
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm);
    }
    
    public User updateUserProfile(Integer userId, User userDetails) {
        return updateUser(userId, userDetails);
    }
    
    public User updateLastLogin(String username) {
        User user = getUserByUsername(username);
        user.setLastLoginDate(LocalDateTime.now());
        return userRepository.save(user);
    }
}
