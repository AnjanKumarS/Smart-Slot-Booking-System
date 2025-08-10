package com.smartslot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpSession;
import com.smartslot.model.Venue;
import com.smartslot.service.VenueService;
import java.util.List;
import java.time.LocalDateTime;

@Controller
public class HomeController {
    
    @Value("${app.development.mode:false}")
    private boolean developmentMode;
    
    private final VenueService venueService;

    public HomeController(VenueService venueService) {
        this.venueService = venueService;
    }
    
    /**
     * Handle logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    /**
     * Test endpoint to check venue data
     */
    @GetMapping("/test-venues")
    public String testVenues(HttpSession session) {
        try {
            // This will trigger venue creation if none exist
            List<Venue> venues = venueService.getVenuesWithDemoData();
            return "Venues test: Found " + venues.size() + " venues. First venue: " + 
                   (venues.isEmpty() ? "None" : venues.get(0).getName());
        } catch (Exception e) {
            return "Venues test failed: " + e.getMessage();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }
    
    /**
     * Test endpoint to check authentication status
     */
    @GetMapping("/test-auth")
    public String testAuth(HttpSession session) {
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (user != null && role != null) {
            return "Authentication working! User: " + user.toString() + ", Role: " + role;
        } else {
            return "No authentication found in session";
        }
    }
    
    /**
     * Serve the main application page
     * @return String template name
     */
    @GetMapping("/")
    public String home(HttpSession session) {
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        if (user == null || role == null) {
            if (developmentMode) {
                // For development: serve landing page instead of redirecting to login
                return "landing";
            } else {
                // For production: redirect to login
                return "redirect:/login";
            }
        }
        return "redirect:/dashboard";
    }
    


    /**
     * Helper method to check session user and role
     */
    private boolean isAuthorized(HttpSession session, String requiredRole) {
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        return user != null && role != null && role.equals(requiredRole);
    }

    /**
     * Serve the main dashboard page if logged in, else redirect to login
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, org.springframework.ui.Model model) {
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        if (user == null || role == null) {
            return "redirect:/login";
        }
        
        // Load venues for the dashboard
        List<Venue> venues = venueService.getVenuesWithDemoData();
        model.addAttribute("venues", venues);
        
        return "dashboard";
    }

    /**
     * Serve the user dashboard page if logged in, else redirect to login
     */
    @GetMapping("/user-dashboard")
    public String userDashboard(HttpSession session) {
        if (!isAuthorized(session, "USER")) {
            return "redirect:/login";
        }
        return "user-dashboard";
    }

    /**
     * Serve the admin dashboard page if logged in as ADMIN, else redirect to login
     */
    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session) {
        if (!isAuthorized(session, "ADMIN")) {
            return "redirect:/login";
        }
        return "admin-dashboard";
    }

    /**
     * Serve the staff dashboard page if logged in as STAFF, else redirect to login
     */
    @GetMapping("/staff-dashboard")
    public String staffDashboard(HttpSession session) {
        if (!isAuthorized(session, "STAFF")) {
            return "redirect:/login";
        }
        return "staff-dashboard";
    }

    /**
     * Serve the analytics dashboard page if logged in as ADMIN, else redirect to login
     */
    @GetMapping("/admin/analytics")
    public String showAnalyticsPage(HttpSession session) {
        if (!isAuthorized(session, "ADMIN")) {
            return "redirect:/login";
        }
        return "analytics-dashboard";
    }
    
    /**
     * Serve the admin login page
     */
    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }

    /**
     * Serve the login page
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * Serve the register page
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    /**
     * Serve the chatbot page
     */
    @GetMapping("/chatbot")
    public String chatbot(HttpSession session) {
        Object user = session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        if (user == null || role == null) {
            return "redirect:/login";
        }
        return "chatbot";
    }
}

