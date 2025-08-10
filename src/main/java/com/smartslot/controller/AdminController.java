package com.smartslot.controller;

import com.smartslot.model.Booking;
import com.smartslot.model.User;
import com.smartslot.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/admin/panel")
    public String adminPanel(HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        return "admin-dashboard";
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if ("admin".equals(username) && "admin123".equals(password)) {
            User adminUser = new User();
            adminUser.setEmail("admin@smartslot.com");
            adminUser.setName("Admin");
            adminUser.setRole(User.UserRole.ADMIN);
            session.setAttribute("user", adminUser);
            session.setAttribute("role", "ADMIN");
            return ResponseEntity.ok(Map.of("success", true, "redirectUrl", "/admin/panel"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Invalid credentials"));
        }
    }

    @GetMapping("/api/admin/bookings")
    @ResponseBody
    public ResponseEntity<?> getAllBookings(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(Map.of("success", true, "bookings", bookings));
    }

    @PostMapping("/api/admin/bookings/{id}/approve")
    @ResponseBody
    public ResponseEntity<?> approveBooking(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        User admin = (User) session.getAttribute("user");
        Map<String, Object> result = bookingService.approveBooking(id, admin.getEmail());
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Booking approved successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", result.get("error")));
        }
    }

    @PostMapping("/api/admin/bookings/{id}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectBooking(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Map<String, Object> result = bookingService.rejectBooking(id);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Booking rejected successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", result.get("error")));
        }
    }

    @PostMapping("/api/admin/bookings/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Map<String, Object> result = bookingService.adminCancelBooking(id);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Booking cancelled successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", result.get("error")));
        }
    }

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "ADMIN".equals(role);
    }
}
