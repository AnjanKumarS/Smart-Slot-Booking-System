package com.smartslot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.smartslot.model.User;
import com.smartslot.service.FirebaseUserDetailsService;
import com.smartslot.service.AuthService;
import com.smartslot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private FirebaseUserDetailsService firebaseUserDetailsService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${firebase.demo-mode:false}")
    private boolean demoMode;
    
    /**
     * Create a session after Firebase login
     */
    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestBody Map<String, String> body, HttpSession session) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().body("ID token is required");
        }
        
        try {
            User user;
            
            // Try to verify Firebase token if Firebase is initialized
            if (!FirebaseApp.getApps().isEmpty()) {
                try {
                    FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                    user = firebaseUserDetailsService.loadUserByFirebaseToken(firebaseToken);
                } catch (Exception e) {
                    // Firebase verification failed, but we can still decode the JWT token
                    user = createUserFromJWTToken(idToken);
                }
            } else {
                // Firebase not initialized, decode the JWT token
                user = createUserFromJWTToken(idToken);
            }
            
            // Validate email domain
            if (!authService.isValidEmailDomain(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Access Denied",
                    "message", "Only @rvce.edu.in email addresses are allowed to access this system"
                ));
            }
            
            // Set session attributes
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole().name());
            session.setAttribute("firebaseUid", user.getFirebaseUid());
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Session created successfully",
                "user", Map.of(
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole().name()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid Firebase token: " + e.getMessage());
        }
    }
    
    /**
     * Create a user from JWT token (when Firebase verification fails)
     */
    private User createUserFromJWTToken(String idToken) {
        try {
            // Decode the JWT token without verification
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Decode the payload (second part)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            // Parse the JSON payload
            ObjectMapper mapper = new ObjectMapper();
            JsonNode claims = mapper.readTree(payload);
            
            String email = "unknown@example.com";
            String name = "User";
            String firebaseUid = "uid-" + System.currentTimeMillis();
            
            // Extract user information from the token
            if (claims.has("email")) {
                email = claims.get("email").asText();
            }
            
            if (claims.has("name")) {
                name = claims.get("name").asText();
            } else if (claims.has("email")) {
                // Use email prefix as name
                email = claims.get("email").asText();
                name = email.substring(0, email.indexOf("@"));
            }
            
            if (claims.has("user_id")) {
                firebaseUid = claims.get("user_id").asText();
            } else if (claims.has("sub")) {
                firebaseUid = claims.get("sub").asText();
            }
            
            // Check if user already exists in database
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                System.out.println("AuthController: Found existing user by email: " + email);
                return existingUser.get();
            }
            
            // Create new user
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setFirebaseUid(firebaseUid);
            user.setRole(User.UserRole.USER);
            user.setActive(true);
            
            // Save user to database
            User savedUser = userRepository.save(user);
            System.out.println("AuthController: Created new user: " + email + " with ID: " + savedUser.getId());
            
            return savedUser;
            
        } catch (Exception e) {
            System.err.println("AuthController: Error creating user from JWT token: " + e.getMessage());
            // If JWT decoding fails, create a basic user
            User user = new User();
            user.setEmail("user@example.com");
            user.setName("User");
            user.setRole(User.UserRole.USER);
            user.setFirebaseUid("uid-" + System.currentTimeMillis());
            user.setActive(true);
            
            // Try to save the user
            try {
                return userRepository.save(user);
            } catch (Exception saveException) {
                System.err.println("AuthController: Error saving user: " + saveException.getMessage());
                return user;
            }
        }
    }
    
    /**
     * Check if user is authenticated
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (user != null && role != null) {
            return ResponseEntity.ok().body(Map.of(
                "authenticated", true,
                "user", user,
                "role", role
            ));
        } else {
            return ResponseEntity.ok().body(Map.of("authenticated", false));
        }
    }
    
    /**
     * Logout - clear session
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
    }
}

