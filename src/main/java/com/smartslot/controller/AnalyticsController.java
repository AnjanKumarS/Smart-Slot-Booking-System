package com.smartslot.controller;

import com.smartslot.model.User;
import com.smartslot.service.AnalyticsService;
import com.smartslot.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Get booking statistics: totalBookings and statusCounts
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBookingStats() {
        Map<String, Object> stats = analyticsService.getBookingStats();
        stats.put("success", true);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get comprehensive analytics (Admin only)
     * @param token Authorization token
     * @return ResponseEntity with analytics data
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            Map<String, Object> analytics = analyticsService.getAnalytics();
            analytics.put("success", true);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get analytics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get venue utilization statistics (Admin only)
     * @param token Authorization token
     * @return ResponseEntity with venue utilization data
     */
    @GetMapping("/venue-utilization")
    public ResponseEntity<Map<String, Object>> getVenueUtilization(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            Map<String, Object> utilization = analyticsService.getVenueUtilization();
            utilization.put("success", true);
            return ResponseEntity.ok(utilization);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get venue utilization: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get date range statistics (Admin only)
     * @param startDate Start date
     * @param endDate End date
     * @param token Authorization token
     * @return ResponseEntity with date range statistics
     */
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getDateRangeStatistics(@RequestParam String startDate,
                                                                      @RequestParam String endDate,
                                                                      @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            Map<String, Object> statistics = analyticsService.getDateRangeStatistics(start, end);
            statistics.put("success", true);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get date range statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get user activity statistics (Admin only)
     * @param token Authorization token
     * @return ResponseEntity with user activity data
     */
    @GetMapping("/user-activity")
    public ResponseEntity<Map<String, Object>> getUserActivityStatistics(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.ADMIN)) {
            response.put("success", false);
            response.put("error", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            Map<String, Object> statistics = analyticsService.getUserActivityStatistics();
            statistics.put("success", true);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get user activity statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

