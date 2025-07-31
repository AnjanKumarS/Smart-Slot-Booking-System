package com.smartslot.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.smartslot.model.User;
import com.smartslot.service.FirebaseUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FirebaseUserDetailsService firebaseUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Check if user is already authenticated in Spring Security context
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            System.out.println("User already authenticated: " + SecurityContextHolder.getContext().getAuthentication().getName());
            filterChain.doFilter(request, response);
            return;
        }
        
        // First, try Bearer token authentication
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Processing Bearer token authentication for: " + request.getRequestURI());
            
            try {
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