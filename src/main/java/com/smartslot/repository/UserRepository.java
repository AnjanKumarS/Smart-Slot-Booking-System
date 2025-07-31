package com.smartslot.repository;

import com.smartslot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByFirebaseUid(String firebaseUid);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByIsActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.isActive = true")
    List<User> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    Long countByRoleAndIsActiveTrue(@Param("role") User.UserRole role);
    
    boolean existsByEmail(String email);
}

