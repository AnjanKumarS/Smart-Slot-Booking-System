package com.smartslot.controller;

import com.smartslot.model.Booking;
import com.smartslot.model.User;
import com.smartslot.service.AuthService;
import com.smartslot.service.BookingService;
import com.smartslot.service.VenueService;
import com.smartslot.util.OtpUtil;
import com.smartslot.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingApiController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private OtpUtil otpUtil;
    
    @Autowired
    private EmailUtil emailUtil;
    
    @Autowired
    private VenueService venueService;
    
    /**
     * Create new booking
     * @param booking Booking to create
     * @param token Authorization token
     * @return ResponseEntity with creation result
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> bookingData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // Extract booking data
            Long venueId = Long.parseLong(bookingData.get("venueId").toString());
            String date = bookingData.get("date").toString();
            String startTime = bookingData.get("startTime").toString();
            String endTime = bookingData.get("endTime").toString();
            
            // Get optional fields
            String title = bookingData.containsKey("title") ? bookingData.get("title").toString() : null;
            String purpose = bookingData.containsKey("purpose") ? bookingData.get("purpose").toString() : null;
            
            // Use enhanced booking method with conflict detection
            Map<String, Object> bookingResult = bookingService.createEnhancedBooking(
                userObj, venueId, date, startTime, endTime, title, purpose);
            
            if ((Boolean) bookingResult.get("success")) {
                Booking booking = (Booking) bookingResult.get("booking");
                
                // Generate OTP and send email for all bookings
                String otp = otpUtil.generateOtp();
                session.setAttribute("bookingOtp", otp);
                session.setAttribute("bookingId", booking.getId());
                
                // Send OTP via email
                try {
                    User user = (User) userObj;
                    String subject = "Booking OTP Verification - Smart Slot Booking System";
                    String body = String.format(
                        "Hello %s,\n\n" +
                        "Your booking OTP is: %s\n\n" +
                        "Please enter this OTP to confirm your booking.\n" +
                        "This OTP is valid for 10 minutes.\n\n" +
                        "Booking Details:\n" +
                        "- Venue: %s\n" +
                        "- Date: %s\n" +
                        "- Time: %s - %s\n\n" +
                        "If you didn't make this booking, please ignore this email.\n\n" +
                        "Best regards,\nSmart Slot Booking System",
                        user.getName(),
                        otp,
                        booking.getVenue().getName(),
                        date,
                        startTime,
                        endTime
                    );
                    
                    emailUtil.sendEmail(user.getEmail(), subject, body);
                    System.out.println("OTP sent to: " + user.getEmail() + " - OTP: " + otp);
                } catch (Exception e) {
                    System.err.println("Failed to send OTP email: " + e.getMessage());
                }
                
                response.put("redirectUrl", "/verify-booking");
                
                response.put("success", true);
                response.put("bookingId", booking.getId());
                response.put("status", booking.getStatus().toString());
                response.put("message", bookingResult.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", bookingResult.get("error"));
                if (bookingResult.containsKey("conflictType")) {
                    response.put("conflictType", bookingResult.get("conflictType"));
                }
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to create booking: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get current OTP (for development)
     * @param session HttpSession
     * @return ResponseEntity with OTP
     */
    @GetMapping("/get-otp")
    public ResponseEntity<Map<String, Object>> getCurrentOtp(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String sessionOtp = (String) session.getAttribute("bookingOtp");
        Long bookingId = (Long) session.getAttribute("bookingId");
        
        if (sessionOtp == null || bookingId == null) {
            response.put("success", false);
            response.put("error", "No OTP session found");
            return ResponseEntity.status(400).body(response);
        }
        
        response.put("success", true);
        response.put("otp", sessionOtp);
        response.put("bookingId", bookingId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Resend OTP (for development)
     * @param session HttpSession
     * @return ResponseEntity with new OTP
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long bookingId = (Long) session.getAttribute("bookingId");
        
        if (bookingId == null) {
            response.put("success", false);
            response.put("error", "No booking session found");
            return ResponseEntity.status(400).body(response);
        }
        
        try {
            // Get booking details
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                response.put("success", false);
                response.put("error", "Booking not found");
                return ResponseEntity.status(400).body(response);
            }
            
            // Generate new OTP
            String newOtp = otpUtil.generateOtp();
            session.setAttribute("bookingOtp", newOtp);
            
            // Send new OTP via email
            try {
                String subject = "New Booking OTP - Smart Slot Booking System";
                String body = String.format(
                    "Hello %s,\n\n" +
                    "Your new booking OTP is: %s\n\n" +
                    "Please enter this OTP to confirm your booking.\n" +
                    "This OTP is valid for 10 minutes.\n\n" +
                    "Booking Details:\n" +
                    "- Venue: %s\n" +
                    "- Date: %s\n" +
                    "- Time: %s - %s\n\n" +
                    "If you didn't request this OTP, please ignore this email.\n\n" +
                    "Best regards,\nSmart Slot Booking System",
                    booking.getUser().getName(),
                    newOtp,
                    booking.getVenue().getName(),
                    booking.getBookingDate(),
                    booking.getStartTime(),
                    booking.getEndTime()
                );
                
                emailUtil.sendEmail(booking.getUser().getEmail(), subject, body);
                System.out.println("New OTP sent to: " + booking.getUser().getEmail() + " - OTP: " + newOtp);
            } catch (Exception e) {
                System.err.println("Failed to send new OTP email: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("otp", newOtp);
            response.put("message", "OTP resent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to resend OTP: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Verify OTP for booking
     * @param request OTP verification request
     * @return ResponseEntity with verification result
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyBookingOtp(@RequestBody Map<String, Object> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String providedOtp = request.get("otp").toString();
        String sessionOtp = (String) session.getAttribute("bookingOtp");
        Long bookingId = (Long) session.getAttribute("bookingId");
        if (sessionOtp == null || bookingId == null) {
            response.put("success", false);
            response.put("error", "No OTP session found");
            return ResponseEntity.status(400).body(response);
        }
        if (providedOtp.equals(sessionOtp)) {
            bookingService.confirmBooking(bookingId);
            session.removeAttribute("bookingOtp");
            session.removeAttribute("bookingId");
            response.put("success", true);
            response.put("message", "Booking confirmed");
        } else {
            response.put("success", false);
            response.put("error", "Invalid OTP");
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's bookings
     * @param session HttpSession
     * @return ResponseEntity with user bookings
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserBookings(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            List<Booking> bookings = bookingService.getUserBookings(userObj);
            
            // Convert to DTOs to include venue information
            List<Map<String, Object>> bookingDtos = bookings.stream()
                .map(booking -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", booking.getId());
                    dto.put("title", booking.getTitle());
                    dto.put("description", booking.getDescription());
                    dto.put("purpose", booking.getPurpose());
                    dto.put("bookingDate", booking.getBookingDate());
                    dto.put("startTime", booking.getStartTime());
                    dto.put("endTime", booking.getEndTime());
                    dto.put("status", booking.getStatus());
                    dto.put("createdAt", booking.getCreatedAt());
                    dto.put("confirmedAt", booking.getConfirmedAt());
                    
                    // Include venue information
                    if (booking.getVenue() != null) {
                        Map<String, Object> venueDto = new HashMap<>();
                        venueDto.put("id", booking.getVenue().getId());
                        venueDto.put("name", booking.getVenue().getName());
                        venueDto.put("location", booking.getVenue().getLocation());
                        venueDto.put("capacity", booking.getVenue().getCapacity());
                        venueDto.put("hourlyRate", booking.getVenue().getHourlyRate());
                        dto.put("venue", venueDto);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("bookings", bookingDtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get bookings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get user's bookings (for token-based auth)
     * @param token Authorization token
     * @return ResponseEntity with user bookings
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<Map<String, Object>> getUserBookings(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null) {
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            List<Booking> bookings = bookingService.getUserBookings(user);
            response.put("success", true);
            response.put("bookings", bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get bookings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get all bookings (Admin/Staff only)
     * @param token Authorization token
     * @return ResponseEntity with all bookings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBookings(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.STAFF)) {
            response.put("success", false);
            response.put("error", "Staff access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            response.put("success", true);
            response.put("bookings", bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get bookings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get pending bookings (Admin/Staff only)
     * @param token Authorization token
     * @return ResponseEntity with pending bookings
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingBookings(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.STAFF)) {
            response.put("success", false);
            response.put("error", "Staff access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            List<Booking> bookings = bookingService.getPendingBookings();
            response.put("success", true);
            response.put("bookings", bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get pending bookings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Approve a booking (STAFF or ADMIN only)
     */
    @PostMapping("/{bookingId}/approve")
    public ResponseEntity<Map<String, Object>> approveBooking(@PathVariable Long bookingId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        if (user == null || role == null || !(role.equals("STAFF") || role.equals("ADMIN"))) {
            response.put("success", false);
            response.put("error", "Unauthorized");
            return ResponseEntity.status(403).body(response);
        }
        try {
            bookingService.approveBooking(bookingId, ((User) user).getEmail());
            response.put("success", true);
            response.put("message", "Booking approved");
            // TODO: Send email notification
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Reject a booking (STAFF or ADMIN only)
     */
    @PostMapping("/{bookingId}/reject")
    public ResponseEntity<Map<String, Object>> rejectBooking(@PathVariable Long bookingId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        if (user == null || role == null || !(role.equals("STAFF") || role.equals("ADMIN"))) {
            response.put("success", false);
            response.put("error", "Unauthorized");
            return ResponseEntity.status(403).body(response);
        }
        try {
            bookingService.rejectBooking(bookingId);
            response.put("success", true);
            response.put("message", "Booking rejected");
            // TODO: Send email notification
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Cancel booking
     * @param id Booking ID
     * @param session HttpSession
     * @return ResponseEntity with cancellation result
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            response.put("success", false);
            response.put("error", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            Map<String, Object> result = bookingService.cancelBooking(id, userObj);
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", result.get("error"));
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to cancel booking: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Check availability for venue
     * @param venueId Venue ID
     * @param date Date to check
     * @return ResponseEntity with availability information
     */
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(@RequestParam Long venueId,
                                                                 @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            Map<String, Object> result = bookingService.checkAvailability(venueId, localDate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to check availability: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get calendar availability for a venue in a given month
     * @param venueId Venue ID
     * @param month Month (1-12)
     * @param year Year (e.g. 2025)
     * @return JSON list of days with availability
     */
    @GetMapping("/calendar")
    public ResponseEntity<List<Map<String, Object>>> getVenueCalendar(@RequestParam Long venueId,
                                                                     @RequestParam int month,
                                                                     @RequestParam int year) {
        List<Map<String, Object>> calendar = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        // Fetch all bookings for the venue in the month
        List<Booking> bookings = bookingService.getBookingsForVenueAndDateRange(venueId, start, end);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            boolean available = bookingService.isDayAvailable(venueId, date, bookings);
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.toString());
            day.put("available", available);
            calendar.add(day);
        }
        return ResponseEntity.ok(calendar);
    }
    
    /**
     * Get available time slots for a venue on a specific date
     * @param venueId Venue ID
     * @param date Date string (YYYY-MM-DD)
     * @return ResponseEntity with available time slots
     */
    @GetMapping("/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableTimeSlots(
            @RequestParam Long venueId, 
            @RequestParam String date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate bookingDate = LocalDate.parse(date);
            List<Map<String, Object>> availableSlots = bookingService.getAvailableTimeSlots(venueId, bookingDate);
            
            response.put("success", true);
            response.put("venueId", venueId);
            response.put("date", date);
            response.put("availableSlots", availableSlots);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get available slots: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get today's bookings
     * @param token Authorization token
     * @return ResponseEntity with today's bookings
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodaysBookings(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        
        User user = authService.validateToken(token);
        if (user == null || !authService.hasRole(user, User.UserRole.STAFF)) {
            response.put("success", false);
            response.put("error", "Staff access required");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            List<Booking> bookings = bookingService.getTodaysBookings();
            response.put("success", true);
            response.put("bookings", bookings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get today's bookings: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Suggest alternate booking slots if desired slot is unavailable
     * @param venueId Venue ID
     * @param date Date (YYYY-MM-DD)
     * @param startTime Start time (HH:mm)
     * @param endTime End time (HH:mm)
     * @return JSON with original request and suggestions
     */
    @GetMapping("/suggest-alternates")
    public ResponseEntity<Map<String, Object>> suggestAlternates(@RequestParam Long venueId,
                                                                @RequestParam String date,
                                                                @RequestParam String startTime,
                                                                @RequestParam String endTime) {
        Map<String, Object> result = bookingService.suggestAlternateSlots(venueId, date, startTime, endTime);
        return ResponseEntity.ok(result);
    }
} 