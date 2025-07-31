package com.smartslot.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.smartslot.model.User;
import com.smartslot.service.FirebaseUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private FirebaseUserDetailsService firebaseUserDetailsService;
    
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
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            User user = firebaseUserDetailsService.loadUserByFirebaseToken(firebaseToken);
            
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

