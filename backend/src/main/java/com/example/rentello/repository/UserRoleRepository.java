package com.example.rentello.repository;

import com.example.rentello.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Optional<UserRole> findByRoleName(String roleName);
    boolean existsByRoleName(String roleName);
}
