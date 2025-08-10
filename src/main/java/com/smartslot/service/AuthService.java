package com.smartslot.service;

import com.smartslot.model.User;
import com.smartslot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    // In-memory token storage for demo purposes
    // In production, use JWT tokens or Redis
    private final Map<String, User> tokenStore = new HashMap<>();
    
    /**
     * Demo login - creates or finds user and generates token
     * @param email User email
     * @param role User role
     * @return Map containing user info and token
     */
    public Map<String, Object> demoLogin(String email, User.UserRole role) {
        // Validate email domain
        if (!isValidEmailDomain(email)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Only @rvce.edu.in email addresses are allowed");
            return response;
        }
        
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createDemoUser(email, role));
        
        String token = generateToken();
        tokenStore.put(token, user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("user", createUserResponse(user));
        
        return response;
    }
    
    /**
     * Validate if email domain is allowed (@rvce.edu.in only)
     * @param email User email
     * @return boolean true if email domain is valid
     */
    public boolean isValidEmailDomain(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String domain = email.trim().toLowerCase();
        return domain.endsWith("@rvce.edu.in");
    }
    
    /**
     * Validate token and get user
     * @param token Authentication token
     * @return User if token is valid, null otherwise
     */
    public User validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return tokenStore.get(token);
    }
    
    /**
     * Logout user by removing token
     * @param token Authentication token
     * @return boolean success status
     */
    public boolean logout(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return tokenStore.remove(token) != null;
    }
    
    /**
     * Check if user has required role
     * @param user User to check
     * @param requiredRole Required role
     * @return boolean true if user has required role or higher
     */
    public boolean hasRole(User user, User.UserRole requiredRole) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        
        User.UserRole userRole = user.getRole();
        
        // Admin has access to everything
        if (userRole == User.UserRole.ADMIN) {
            return true;
        }
        
        // Staff has access to staff and user level
        if (userRole == User.UserRole.STAFF && 
            (requiredRole == User.UserRole.STAFF || requiredRole == User.UserRole.USER)) {
            return true;
        }
        
        // User has access to user level only
        return userRole == User.UserRole.USER && requiredRole == User.UserRole.USER;
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return Optional<User>
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Get user by email
     * @param email User email
     * @return Optional<User>
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Create demo user
     * @param email User email
     * @param role User role
     * @return Created user
     */
    private User createDemoUser(String email, User.UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        
        // Set name based on role
        switch (role) {
            case ADMIN:
                user.setName("Admin User");
                break;
            case STAFF:
                user.setName("Staff User");
                break;
            default:
                user.setName("Demo User");
                break;
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Generate authentication token
     * @return String token
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Create user response object (without sensitive data)
     * @param user User entity
     * @return Map user response
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("name", user.getName());
        userResponse.put("role", user.getRole().toString());
        userResponse.put("phoneNumber", user.getPhoneNumber());
        userResponse.put("isActive", user.getIsActive());
        return userResponse;
    }
}

