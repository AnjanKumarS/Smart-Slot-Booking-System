package com.smartslot.controller;

import com.smartslot.model.User;
import com.smartslot.model.Venue;
import com.smartslot.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class VenueController {
    
    @Autowired
    private VenueService venueService;
    
    // Admin venue management page
    @GetMapping("/admin/venues")
    public String adminVenues(HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        return "admin-venues";
    }

    // Get all venues for admin
    @GetMapping("/api/admin/venues")
    @ResponseBody
    public ResponseEntity<?> getAllVenues(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        List<Venue> venues = venueService.getAllVenues();
        return ResponseEntity.ok(Map.of("success", true, "venues", venues));
    }

    // Get all active venues for public users (dashboard)
    @GetMapping("/api/venues")
    @ResponseBody
    public ResponseEntity<?> getPublicVenues() {
        List<Venue> venues = venueService.getActiveVenues();
        return ResponseEntity.ok(Map.of("success", true, "venues", venues));
    }

    // Get venue by ID
    @GetMapping("/api/admin/venues/{id}")
    @ResponseBody
    public ResponseEntity<?> getVenueById(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Optional<Venue> venue = venueService.getVenueById(id);
        if (venue.isPresent()) {
            return ResponseEntity.ok(Map.of("success", true, "venue", venue.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Venue not found"));
        }
    }

    // Create new venue
    @PostMapping("/api/admin/venues")
    @ResponseBody
    public ResponseEntity<?> createVenue(@RequestBody Venue venue, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Map<String, Object> response = new HashMap<>();
        
        // Validation
        if (venue.getName() == null || venue.getName().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Venue name is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (venue.getCapacity() == null || venue.getCapacity() <= 0) {
            response.put("success", false);
            response.put("message", "Valid capacity is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (venue.getLocation() == null || venue.getLocation().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Venue location is required");
            return ResponseEntity.badRequest().body(response);
        }

        // For college venues, hourly rate should be 0 (free)
        if (venue.getHourlyRate() == null) {
            venue.setHourlyRate(BigDecimal.ZERO);
        }

        // Check if venue name already exists
        if (venueService.venueNameExists(venue.getName())) {
            response.put("success", false);
            response.put("message", "Venue name already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Venue createdVenue = venueService.createVenue(venue);
            response.put("success", true);
            response.put("message", "Venue created successfully");
            response.put("venue", createdVenue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create venue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Update venue
    @PutMapping("/api/admin/venues/{id}")
    @ResponseBody
    public ResponseEntity<?> updateVenue(@PathVariable Long id, @RequestBody Venue venueDetails, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Map<String, Object> response = new HashMap<>();
        
        // Validation
        if (venueDetails.getName() == null || venueDetails.getName().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Venue name is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (venueDetails.getCapacity() == null || venueDetails.getCapacity() <= 0) {
            response.put("success", false);
            response.put("message", "Valid capacity is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (venueDetails.getLocation() == null || venueDetails.getLocation().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Venue location is required");
            return ResponseEntity.badRequest().body(response);
        }

        // For college venues, hourly rate should be 0 (free)
        if (venueDetails.getHourlyRate() == null) {
            venueDetails.setHourlyRate(BigDecimal.ZERO);
        }

        // Check if venue exists
        if (!venueService.venueExists(id)) {
            response.put("success", false);
            response.put("message", "Venue not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check if venue name already exists (excluding current venue)
        if (venueService.venueNameExistsExcludingCurrent(venueDetails.getName(), id)) {
            response.put("success", false);
            response.put("message", "Venue name already exists");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Venue updatedVenue = venueService.updateVenue(id, venueDetails);
            if (updatedVenue != null) {
                response.put("success", true);
                response.put("message", "Venue updated successfully");
                response.put("venue", updatedVenue);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update venue");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update venue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Delete venue (soft delete)
    @DeleteMapping("/api/admin/venues/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteVenue(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Map<String, Object> response = new HashMap<>();
        
        if (!venueService.venueExists(id)) {
            response.put("success", false);
            response.put("message", "Venue not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        try {
            boolean deleted = venueService.deleteVenue(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Venue deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete venue");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete venue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Hard delete venue
    @DeleteMapping("/api/admin/venues/{id}/hard")
    @ResponseBody
    public ResponseEntity<?> hardDeleteVenue(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Map<String, Object> response = new HashMap<>();
        
        if (!venueService.venueExists(id)) {
            response.put("success", false);
            response.put("message", "Venue not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        try {
            boolean deleted = venueService.hardDeleteVenue(id);
            if (deleted) {
            response.put("success", true);
                response.put("message", "Venue permanently deleted");
            return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete venue");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete venue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Search venues
    @GetMapping("/api/admin/venues/search")
    @ResponseBody
    public ResponseEntity<?> searchVenues(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String location,
                                        @RequestParam(required = false) Integer minCapacity,
                                        @RequestParam(required = false) Integer maxCapacity,
                                        @RequestParam(required = false) BigDecimal minRate,
                                        @RequestParam(required = false) BigDecimal maxRate,
                                        HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        List<Venue> venues;

        if (name != null && !name.trim().isEmpty()) {
            venues = venueService.searchVenuesByName(name);
        } else if (location != null && !location.trim().isEmpty()) {
            venues = venueService.searchVenuesByLocation(location);
        } else if (minCapacity != null && maxCapacity != null) {
            venues = venueService.getVenuesByCapacityRange(minCapacity, maxCapacity);
        } else if (minRate != null && maxRate != null) {
            venues = venueService.getVenuesByPriceRange(minRate, maxRate);
        } else if (minCapacity != null) {
            venues = venueService.getVenuesByMinCapacity(minCapacity);
        } else {
            venues = venueService.getAllVenues();
        }

        return ResponseEntity.ok(Map.of("success", true, "venues", venues));
    }

    // Get venue statistics
    @GetMapping("/api/admin/venues/stats")
    @ResponseBody
    public ResponseEntity<?> getVenueStats(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVenues", venueService.getAllVenues().size());
        stats.put("activeVenues", venueService.getActiveVenueCount());
        stats.put("inactiveVenues", venueService.getAllVenues().size() - venueService.getActiveVenueCount());

        return ResponseEntity.ok(Map.of("success", true, "stats", stats));
    }

    // Fix existing venues (set hourly rate to 0 and is_active to true)
    @PostMapping("/api/admin/venues/fix")
    @ResponseBody
    public ResponseEntity<?> fixExistingVenues(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            venueService.fixExistingVenues();
            return ResponseEntity.ok(Map.of("success", true, "message", "Venues fixed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fix venues: " + e.getMessage()));
        }
    }

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "ADMIN".equals(role);
    }
}

