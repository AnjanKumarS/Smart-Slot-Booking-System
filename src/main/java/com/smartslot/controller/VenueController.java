package com.smartslot.controller;

import com.smartslot.model.User;
import com.smartslot.model.Venue;
import com.smartslot.service.AuthService;
import com.smartslot.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/venues")
@CrossOrigin(origins = "*")
public class VenueController {
    
    @Autowired
    private VenueService venueService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Test endpoint with hardcoded venue data
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> getTestVenues() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== VenueController: Getting test venues ===");
            
            List<Map<String, Object>> testVenues = new ArrayList<>();
            
            Map<String, Object> venue1 = new HashMap<>();
            venue1.put("id", 1L);
            venue1.put("name", "CS Auditorium");
            venue1.put("description", "Computer Science Department's main auditorium");
            venue1.put("capacity", 300);
            venue1.put("location", "CS Building, Ground Floor");
            venue1.put("hourly_rate", 120.00);
            venue1.put("image_url", "https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
            venue1.put("is_active", true);
            venue1.put("amenities", Arrays.asList("4K Projector", "Sound System", "Air Conditioning"));
            testVenues.add(venue1);
            
            Map<String, Object> venue2 = new HashMap<>();
            venue2.put("id", 2L);
            venue2.put("name", "ISE Seminar Hall");
            venue2.put("description", "Information Science Engineering seminar hall");
            venue2.put("capacity", 150);
            venue2.put("location", "ISE Building, 1st Floor");
            venue2.put("hourly_rate", 100.00);
            venue2.put("image_url", "https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
            venue2.put("is_active", true);
            venue2.put("amenities", Arrays.asList("HD Projector", "Sound System", "Air Conditioning"));
            testVenues.add(venue2);
            
            response.put("success", true);
            response.put("venues", testVenues);
            System.out.println("VenueController: Returning test venues successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== VenueController: ERROR getting test venues ===");
            System.err.println("VenueController: Error message: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Failed to get test venues: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get all venues
     * @return ResponseEntity with venues
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVenues() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== VenueController: Getting all venues ===");
            System.out.println("VenueController: Calling venueService.getVenuesWithDemoData()...");
            
            List<Venue> venues = venueService.getVenuesWithDemoData();
            System.out.println("VenueController: Found " + venues.size() + " venues");
            
            // Convert to simple DTOs to avoid serialization issues
            List<Map<String, Object>> venueDtos = new ArrayList<>();
            for (Venue venue : venues) {
                Map<String, Object> venueDto = new HashMap<>();
                venueDto.put("id", venue.getId());
                venueDto.put("name", venue.getName());
                venueDto.put("description", venue.getDescription());
                venueDto.put("capacity", venue.getCapacity());
                venueDto.put("location", venue.getLocation());
                venueDto.put("hourly_rate", venue.getHourlyRate());
                venueDto.put("image_url", venue.getImageUrl());
                venueDto.put("is_active", venue.getIsActive());
                venueDto.put("amenities", venue.getAmenities() != null ? venue.getAmenities() : new ArrayList<>());
                venueDtos.add(venueDto);
            }
            
            // Debug each venue
            for (int i = 0; i < venues.size(); i++) {
                Venue venue = venues.get(i);
                System.out.println("Venue " + (i+1) + ": ID=" + venue.getId() + ", Name=" + venue.getName() + ", Active=" + venue.getIsActive());
            }
            
            response.put("success", true);
            response.put("venues", venueDtos);
            System.out.println("VenueController: Returning response with " + venues.size() + " venues");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== VenueController: ERROR getting venues ===");
            System.err.println("VenueController: Error message: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Failed to get venues: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get venue by ID
     * @param id Venue ID
     * @return ResponseEntity with venue details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVenueById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Venue> venue = venueService.getVenueById(id);
        if (venue.isEmpty()) {
            response.put("success", false);
            response.put("error", "Venue not found");
            return ResponseEntity.notFound().build();
        }
        
        response.put("success", true);
        response.put("venue", venue.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create new venue (Admin only)
     * @param venue Venue to create
     * @param token Authorization token
     * @return ResponseEntity with creation result
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createVenue(@RequestBody Venue venue, 
                                                           @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate admin access
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            Venue createdVenue = venueService.createVenue(venue);
            response.put("success", true);
            response.put("venue", createdVenue);
            response.put("message", "Venue created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to create venue: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Update venue (Admin only)
     * @param id Venue ID
     * @param venueDetails Updated venue details
     * @param token Authorization token
     * @return ResponseEntity with update result
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVenue(@PathVariable Long id, 
                                                           @RequestBody Venue venueDetails,
                                                           @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate admin access
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            Venue updatedVenue = venueService.updateVenue(id, venueDetails);
            if (updatedVenue == null) {
                response.put("success", false);
                response.put("error", "Venue not found");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("venue", updatedVenue);
            response.put("message", "Venue updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to update venue: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Delete venue (Admin only)
     * @param id Venue ID
     * @param token Authorization token
     * @return ResponseEntity with deletion result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVenue(@PathVariable Long id,
                                                           @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate admin access
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            boolean deleted = venueService.deleteVenue(id);
            if (!deleted) {
                response.put("success", false);
                response.put("error", "Venue not found");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("message", "Venue deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete venue: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Search venues by name
     * @param name Search term
     * @return ResponseEntity with search results
     */
    @GetMapping("/search")
    public ResponseEntity<List<Venue>> searchVenues(@RequestParam String name) {
        List<Venue> venues = venueService.searchVenuesByName(name);
        return ResponseEntity.ok(venues);
    }
    
    /**
     * Filter venues by capacity
     * @param minCapacity Minimum capacity
     * @return ResponseEntity with filtered venues
     */
    @GetMapping("/filter/capacity")
    public ResponseEntity<List<Venue>> filterByCapacity(@RequestParam Integer minCapacity) {
        List<Venue> venues = venueService.getVenuesByMinCapacity(minCapacity);
        return ResponseEntity.ok(venues);
    }
    
    /**
     * Filter venues by location
     * @param location Location search term
     * @return ResponseEntity with filtered venues
     */
    @GetMapping("/filter/location")
    public ResponseEntity<List<Venue>> filterByLocation(@RequestParam String location) {
        List<Venue> venues = venueService.getVenuesByLocation(location);
        return ResponseEntity.ok(venues);
    }
    
    /**
     * Filter venues by price range
     * @param minRate Minimum hourly rate
     * @param maxRate Maximum hourly rate
     * @return ResponseEntity with filtered venues
     */
    @GetMapping("/filter/price")
    public ResponseEntity<List<Venue>> filterByPrice(@RequestParam BigDecimal minRate, 
                                                     @RequestParam BigDecimal maxRate) {
        List<Venue> venues = venueService.getVenuesByPriceRange(minRate, maxRate);
        return ResponseEntity.ok(venues);
    }
    
    /**
     * Get venue statistics (Admin only)
     * @param token Authorization token
     * @return ResponseEntity with venue statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getVenueStatistics(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate admin access
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        Map<String, Object> statistics = venueService.getVenueStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Reset and recreate all demo venues (Admin only)
     * @param token Authorization token
     * @return ResponseEntity with reset result
     */
    @PostMapping("/reset-demo")
    public ResponseEntity<Map<String, Object>> resetDemoVenues(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate admin access
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            List<Venue> venues = venueService.resetAndCreateDemoVenues();
            response.put("success", true);
            response.put("message", "Successfully reset and created " + venues.size() + " demo venues");
            response.put("venues", venues);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to reset demo venues: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Reset and recreate all demo venues (Public endpoint for testing)
     * @return ResponseEntity with reset result
     */
    @PostMapping("/reset-demo-public")
    public ResponseEntity<Map<String, Object>> resetDemoVenuesPublic() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Venue> venues = venueService.resetAndCreateDemoVenues();
            response.put("success", true);
            response.put("message", "Successfully reset and created " + venues.size() + " demo venues");
            response.put("venues", venues);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to reset demo venues: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

