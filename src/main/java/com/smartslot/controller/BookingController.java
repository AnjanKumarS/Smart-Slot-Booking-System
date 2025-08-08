package com.smartslot.controller;

import com.smartslot.model.Booking;
import com.smartslot.model.User;
import com.smartslot.service.BookingService;
import com.smartslot.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class BookingController {
    @Autowired
    private VenueService venueService;
    
    @Autowired
    private BookingService bookingService;

    @GetMapping("/book")
    public String showBookingPage(HttpSession session, Model model) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("venues", venueService.getVenuesWithDemoData());
        return "booking";
    }

    @GetMapping("/booking")
    public String showBookingPageAlt(HttpSession session, Model model, 
                                   @org.springframework.web.bind.annotation.RequestParam(value = "venueId", required = false) String venueId,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "venueName", required = false) String venueName) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("venues", venueService.getVenuesWithDemoData());
        
        // Add venue parameters to model if provided
        if (venueId != null) {
            model.addAttribute("selectedVenueId", venueId);
        }
        if (venueName != null) {
            model.addAttribute("selectedVenueName", venueName);
        }
        
        return "booking";
    }

    @GetMapping("/verify-booking")
    public String showVerifyBookingPage() {
        return "verify-booking";
    }
    
    @GetMapping("/user/bookings")
    public String showUserBookingsPage(HttpSession session, Model model, 
                                     @RequestParam(value = "status", required = false) String status) {
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            return "redirect:/login";
        }
        
        try {
            // Get all user bookings
            List<Booking> allBookings = bookingService.getUserBookings(userObj);
            
            // Filter by status if provided
            List<Booking> filteredBookings = allBookings;
            if (status != null && !status.isEmpty()) {
                filteredBookings = allBookings.stream()
                    .filter(booking -> booking.getStatus().name().equals(status.toUpperCase()))
                    .toList();
            }
            
            // Add bookings to model
            model.addAttribute("bookings", filteredBookings);
            model.addAttribute("allBookings", allBookings);
            model.addAttribute("selectedStatus", status);
            
            // Add status counts for tabs
            model.addAttribute("confirmedCount", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED).count());
            model.addAttribute("pendingCount", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING).count());
            model.addAttribute("cancelledCount", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED).count());
            model.addAttribute("rejectedCount", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.REJECTED).count());
            
        } catch (Exception e) {
            // Log error and return empty list
            System.err.println("Error loading user bookings: " + e.getMessage());
            model.addAttribute("bookings", List.of());
            model.addAttribute("allBookings", List.of());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("confirmedCount", 0);
            model.addAttribute("pendingCount", 0);
            model.addAttribute("cancelledCount", 0);
            model.addAttribute("rejectedCount", 0);
        }
        
        return "user-bookings";
    }
}

