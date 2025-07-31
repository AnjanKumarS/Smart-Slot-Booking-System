package com.smartslot.service;

import com.google.firebase.auth.FirebaseToken;
import com.smartslot.model.User;
import com.smartslot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FirebaseUserDetailsService {
    @Autowired
    private UserRepository userRepository;

    public User loadUserByFirebaseToken(FirebaseToken firebaseToken) {
        String uid = firebaseToken.getUid();
        String email = firebaseToken.getEmail();
        
        // Try to find existing user by email
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Create new user if not exists (auto-registration)
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(firebaseToken.getName() != null ? firebaseToken.getName() : email);
        newUser.setFirebaseUid(uid);
        newUser.setRole(User.UserRole.USER);
        newUser.setActive(true);
        
        return userRepository.save(newUser);
    }
} 