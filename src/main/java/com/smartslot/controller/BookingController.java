package com.smartslot.controller;

import com.smartslot.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpSession;

@Controller
public class BookingController {
    @Autowired
    private VenueService venueService;

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
    public String showUserBookingsPage(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "user-bookings";
    }
}

