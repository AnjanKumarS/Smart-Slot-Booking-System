package com.smartslot.controller;

import com.smartslot.model.User;
import com.smartslot.service.AuthService;
import com.smartslot.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class ChatbotController {
    
    @Autowired
    private ChatbotService chatbotService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Process chat message
     * @param request Chat request containing message
     * @param token Authorization token
     * @return ResponseEntity with chat response
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> processMessage(@RequestBody Map<String, String> request,
                                                              @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null) {
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Message is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Map<String, Object> result = chatbotService.processMessage(message, user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to process message: " + e.getMessage());
            response.put("response", "I'm sorry, I encountered an error. Please try again.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get chat suggestions
     * @return ResponseEntity with chat suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getChatSuggestions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> suggestions = chatbotService.getChatSuggestions();
            response.put("success", true);
            response.put("suggestions", suggestions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get suggestions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Confirm AI-suggested booking
     * @param request Booking confirmation request
     * @param token Authorization token
     * @return ResponseEntity with confirmation result
     */
    @PostMapping("/confirm-booking")
    public ResponseEntity<Map<String, Object>> confirmAiBooking(@RequestBody Map<String, Object> request,
                                                                @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null) {
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> bookingDetails = (Map<String, Object>) request.get("booking_details");
        if (bookingDetails == null) {
            response.put("success", false);
            response.put("error", "Booking details are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Map<String, Object> result = chatbotService.confirmAiBooking(bookingDetails, user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to confirm booking: " + e.getMessage());
            response.put("response", "Sorry, I couldn't create your booking. Please try again or use the booking form.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

