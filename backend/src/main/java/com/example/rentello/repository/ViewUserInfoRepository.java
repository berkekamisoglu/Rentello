package com.example.rentello.repository;

import com.example.rentello.entity.ViewUserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewUserInfoRepository extends JpaRepository<ViewUserInfo, Integer> {
    
    @Query("SELECT v FROM ViewUserInfo v WHERE " +
           "LOWER(v.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ViewUserInfo> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(v) FROM ViewUserInfo v WHERE v.isActive = true")
    Long countActiveCustomers();
    
    @Query("SELECT COUNT(v) FROM ViewUserInfo v WHERE v.createdDate >= :startDate")
    Long countNewCustomersThisMonth(@Param("startDate") java.time.LocalDateTime startDate);
} 