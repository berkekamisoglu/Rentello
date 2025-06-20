package com.example.rentello.repository;

import com.example.rentello.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.userRole.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.city.cityId = :cityId")
    List<User> findByCityId(@Param("cityId") Integer cityId);
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Admin Service için eklenen metodlar
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdDate >= :startDate")
    Long countNewCustomersThisMonth(@Param("startDate") java.time.LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveCustomers();
    
    org.springframework.data.domain.Page<User> findByUsernameContainingOrEmailContainingOrFirstNameContainingOrLastNameContaining(
        String username, String email, String firstName, String lastName, org.springframework.data.domain.Pageable pageable);
}
