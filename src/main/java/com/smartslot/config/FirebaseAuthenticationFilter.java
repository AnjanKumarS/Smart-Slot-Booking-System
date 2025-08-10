package com.smartslot.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.smartslot.model.User;
import com.smartslot.service.FirebaseUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    @Autowired
    private FirebaseUserDetailsService firebaseUserDetailsService;

    @Value("${firebase.demo-mode:false}")
    private boolean demoMode;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip authentication for admin login endpoint
        if (request.getRequestURI().equals("/admin/login") && request.getMethod().equals("POST")) {
            System.out.println("Skipping authentication for admin login endpoint");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check if user is already authenticated in Spring Security context
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            System.out.println("User already authenticated: " + SecurityContextHolder.getContext().getAuthentication().getName());
            filterChain.doFilter(request, response);
            return;
        }
        
        // If in demo mode, only check for session-based authentication
        if (demoMode) {
            // Check for session-based authentication only
            Object user = request.getSession().getAttribute("user");
            String role = (String) request.getSession().getAttribute("role");
            
            System.out.println("Demo mode: Checking session authentication for: " + request.getRequestURI());
            System.out.println("Session user: " + (user != null ? "present" : "null"));
            System.out.println("Session role: " + role);
            
            if (user != null && role != null && user instanceof User) {
                User sessionUser = (User) user;
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        sessionUser,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + sessionUser.getRole().name()))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Demo mode: Session authentication successful for user: " + sessionUser.getEmail());
            } else {
                System.out.println("Demo mode: No valid session authentication found");
            }
            
            filterChain.doFilter(request, response);
            return;
        }
        
        // First, try Bearer token authentication (only if not in demo mode)
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Processing Bearer token authentication for: " + request.getRequestURI());
            
            try {
                // Check if Firebase is initialized before trying to use it
                if (FirebaseApp.getApps().isEmpty()) {
                    logger.warn("Firebase not initialized, skipping token verification");
                } else {
                    FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    User user = firebaseUserDetailsService.loadUserByFirebaseToken(firebaseToken);
                    
                    if (user != null) {
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                            );
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // Set session attributes for Thymeleaf/controllers
                        request.getSession().setAttribute("user", user);
                        request.getSession().setAttribute("role", user.getRole().name());
                        
                        System.out.println("Bearer token authentication successful for user: " + user.getEmail());
                    }
                }
            } catch (Exception e) {
                logger.warn("Invalid Firebase token: " + e.getMessage());
            }
        } else {
            // If no Bearer token, check for session-based authentication
            Object user = request.getSession().getAttribute("user");
            String role = (String) request.getSession().getAttribute("role");
            
            System.out.println("Checking session authentication for: " + request.getRequestURI());
            System.out.println("Session user: " + (user != null ? "present" : "null"));
            System.out.println("Session role: " + role);
            
            if (user != null && role != null && user instanceof User) {
                User sessionUser = (User) user;
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        sessionUser,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + sessionUser.getRole().name()))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Session authentication successful for user: " + sessionUser.getEmail());
            } else {
                System.out.println("No valid session authentication found");
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 