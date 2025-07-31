package com.smartslot.service;

import com.smartslot.model.Booking;
import com.smartslot.model.User;
import com.smartslot.model.Venue;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotService {
    
    @Autowired
    private VenueService venueService;
    
    @Autowired
    private BookingService bookingService;
    
    @Value("${openai.api.key:}")
    private String openAiApiKey;
    
    @Value("${openai.demo-mode:true}")
    private boolean demoMode;
    
    private OpenAiService openAiService;
    
    /**
     * Process chat message and generate response
     * @param message User message
     * @param user Current user
     * @return Map containing chat response
     */
    public Map<String, Object> processMessage(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Analyze message intent
            String intent = analyzeIntent(message);
            
            switch (intent) {
                case "booking":
                    return handleBookingIntent(message, user);
                case "availability":
                    return handleAvailabilityIntent(message, user);
                case "venues":
                    return handleVenuesIntent(message, user);
                case "my_bookings":
                    return handleMyBookingsIntent(message, user);
                case "help":
                    return handleHelpIntent(message, user);
                default:
                    return handleGeneralIntent(message, user);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Sorry, I encountered an error processing your request.");
            response.put("response", "I'm having trouble understanding your request. Could you please try rephrasing it?");
            return response;
        }
    }
    
    /**
     * Get chat suggestions
     * @return List of suggested messages
     */
    public List<String> getChatSuggestions() {
        return Arrays.asList(
            "Book the main auditorium for tomorrow at 2 PM",
            "Check availability for Conference Room A",
            "Show me my bookings",
            "What venues are available?",
            "Cancel my booking for today",
            "Find a venue for 50 people"
        );
    }
    
    /**
     * Confirm AI-suggested booking
     * @param bookingDetails Booking details from AI
     * @param user Current user
     * @return Map containing confirmation result
     */
    public Map<String, Object> confirmAiBooking(Map<String, Object> bookingDetails, User user) {
        try {
            // Create booking from AI details
            Booking booking = new Booking();
            booking.setTitle((String) bookingDetails.get("title"));
            booking.setDescription((String) bookingDetails.get("description"));
            booking.setPurpose((String) bookingDetails.get("purpose"));
            
            // Set venue
            Long venueId = Long.valueOf(bookingDetails.get("venue_id").toString());
            Optional<Venue> venue = venueService.getVenueById(venueId);
            if (venue.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Venue not found");
                return response;
            }
            booking.setVenue(venue.get());
            
            // Set date and time
            booking.setBookingDate(LocalDate.parse((String) bookingDetails.get("date")));
            booking.setStartTime(LocalTime.parse((String) bookingDetails.get("start_time")));
            booking.setEndTime(LocalTime.parse((String) bookingDetails.get("end_time")));
            
            // Set optional fields
            if (bookingDetails.containsKey("expected_attendees")) {
                booking.setExpectedAttendees((Integer) bookingDetails.get("expected_attendees"));
            }
            if (bookingDetails.containsKey("contact_number")) {
                booking.setContactNumber((String) bookingDetails.get("contact_number"));
            }
            
            // Create booking
            Map<String, Object> result = bookingService.createBooking(booking, user);
            
            if ((Boolean) result.get("success")) {
                result.put("response", "Great! Your booking has been created successfully. Please check your email for the OTP to confirm your booking.");
            }
            
            return result;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to create booking: " + e.getMessage());
            response.put("response", "Sorry, I couldn't create your booking. Please try again or use the booking form.");
            return response;
        }
    }
    
    /**
     * Analyze message intent using pattern matching
     * @param message User message
     * @return String intent
     */
    private String analyzeIntent(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Booking patterns
        if (lowerMessage.contains("book") || lowerMessage.contains("reserve") || 
            lowerMessage.contains("schedule") || lowerMessage.contains("meeting")) {
            return "booking";
        }
        
        // Availability patterns
        if (lowerMessage.contains("available") || lowerMessage.contains("free") || 
            lowerMessage.contains("check") || lowerMessage.contains("when")) {
            return "availability";
        }
        
        // Venues patterns
        if (lowerMessage.contains("venue") || lowerMessage.contains("room") || 
            lowerMessage.contains("hall") || lowerMessage.contains("space")) {
            return "venues";
        }
        
        // My bookings patterns
        if (lowerMessage.contains("my booking") || lowerMessage.contains("my reservation") || 
            lowerMessage.contains("my meeting") || lowerMessage.contains("cancel")) {
            return "my_bookings";
        }
        
        // Help patterns
        if (lowerMessage.contains("help") || lowerMessage.contains("how") || 
            lowerMessage.contains("what can") || lowerMessage.contains("assist")) {
            return "help";
        }
        
        return "general";
    }
    
    /**
     * Handle booking intent
     * @param message User message
     * @param user Current user
     * @return Map containing response
     */
    private Map<String, Object> handleBookingIntent(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        
        // Extract booking details from message
        Map<String, Object> bookingDetails = extractBookingDetails(message);
        
        if (bookingDetails.isEmpty()) {
            response.put("success", true);
            response.put("response", "I'd be happy to help you make a booking! Could you please provide more details like the venue, date, and time?");
            response.put("available_venues", venueService.getAllActiveVenues());
            return response;
        }
        
        // If we have enough details, suggest booking
        if (bookingDetails.containsKey("venue_id") && bookingDetails.containsKey("date") && 
            bookingDetails.containsKey("start_time")) {
            response.put("success", true);
            response.put("response", "I found the details for your booking. Please review and confirm:");
            response.put("booking_details", bookingDetails);
            
            // Get venue details
            Long venueId = (Long) bookingDetails.get("venue_id");
            Optional<Venue> venue = venueService.getVenueById(venueId);
            if (venue.isPresent()) {
                response.put("venue", venue.get());
            }
        } else {
            response.put("success", true);
            response.put("response", "I need a bit more information to complete your booking. Please specify the venue, date, and time.");
            response.put("available_venues", venueService.getAllActiveVenues());
        }
        
        return response;
    }
    
    /**
     * Handle availability intent
     * @param message User message
     * @param user Current user
     * @return Map containing response
     */
    private Map<String, Object> handleAvailabilityIntent(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        
        // Extract venue and date from message
        String venueName = extractVenueName(message);
        LocalDate date = extractDate(message);
        
        if (venueName != null && date != null) {
            List<Venue> venues = venueService.searchVenuesByName(venueName);
            if (!venues.isEmpty()) {
                Venue venue = venues.get(0);
                Map<String, Object> availability = bookingService.checkAvailability(venue.getId(), date);
                
                response.put("success", true);
                response.put("response", String.format("Here's the availability for %s on %s:", 
                    venue.getName(), date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))));
                response.putAll(availability);
                return response;
            }
        }
        
        response.put("success", true);
        response.put("response", "I can help you check availability! Please specify which venue and date you're interested in.");
        response.put("available_venues", venueService.getAllActiveVenues());
        
        return response;
    }
    
    /**
     * Handle venues intent
     * @param message User message
     * @param user Current user
     * @return Map containing response
     */
    private Map<String, Object> handleVenuesIntent(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        
        List<Venue> venues = venueService.getAllActiveVenues();
        
        response.put("success", true);
        response.put("response", "Here are our available venues:");
        response.put("available_venues", venues);
        
        return response;
    }
    
    /**
     * Handle my bookings intent
     * @param message User message
     * @param user Current user
     * @return Map containing response
     */
    private Map<String, Object> handleMyBookingsIntent(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        
        List<Booking> userBookings = bookingService.getUserBookings(user);
        
        if (userBookings.isEmpty()) {
            response.put("success", true);
            response.put("response", "You don't have any bookings yet. Would you like to make one?");
        } else {
            response.put("success", true);
            response.put("response", "Here are your recent bookings:");
            response.put("bookings", userBookings);
        }
        
        return response;
    }
    
    /**
     * Handle help intent
     * @param message User message
     * @param user Current user
     * @return Map containing response
     */
    private Map<String, Object> handleHelpIntent(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        
        String helpMessage = "I'm your booking assistant! Here's what I can help you with:\n\n" +
            "📅 **Make Bookings**: \"Book the main auditorium for tomorrow at 2 PM\"\n" +
            "🔍 **Check Availability**: \"Is Conference Room A available on Friday?\"\n" +
            "🏢 **Browse Venues**: \"Show me all available venues\"\n" +
            "📋 **View Your Bookings**: \"Show me my bookings\"\n" +
            "❌ **Cancel Bookings**: \"Cancel my booking for today\"\n\n" +
            "Just tell me what you need in natural language, and I'll help you out!";
        
        response.put("success", true);
        response.put("response", helpMessage);
        
        return response;
    }
    
    /**
     * Handle general intent using OpenAI (if available)
     * @param message User message
     * @param user Current user
     * @return Map containing response
     */
    private Map<String, Object> handleGeneralIntent(String message, User user) {
        Map<String, Object> response = new HashMap<>();
        
        if (demoMode || openAiApiKey == null || openAiApiKey.isEmpty()) {
            // Demo response
            response.put("success", true);
            response.put("response", "I'm here to help you with venue bookings! You can ask me to book a venue, check availability, or view your bookings. What would you like to do?");
            return response;
        }
        
        try {
            if (openAiService == null) {
                openAiService = new OpenAiService(openAiApiKey);
            }
            
            String systemPrompt = "You are a helpful booking assistant for a venue booking system. " +
                "Help users with booking venues, checking availability, and managing their reservations. " +
                "Keep responses concise and helpful. If you can't help with something, direct them to use the booking form.";
            
            List<ChatMessage> messages = Arrays.asList(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                new ChatMessage(ChatMessageRole.USER.value(), message)
            );
            
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(150)
                .temperature(0.7)
                .build();
            
            String aiResponse = openAiService.createChatCompletion(completionRequest)
                .getChoices().get(0).getMessage().getContent();
            
            response.put("success", true);
            response.put("response", aiResponse);
            
        } catch (Exception e) {
            response.put("success", true);
            response.put("response", "I'm here to help you with venue bookings! You can ask me to book a venue, check availability, or view your bookings. What would you like to do?");
        }
        
        return response;
    }
    
    /**
     * Extract booking details from message
     * @param message User message
     * @return Map containing extracted details
     */
    private Map<String, Object> extractBookingDetails(String message) {
        Map<String, Object> details = new HashMap<>();
        
        // Extract venue name
        String venueName = extractVenueName(message);
        if (venueName != null) {
            List<Venue> venues = venueService.searchVenuesByName(venueName);
            if (!venues.isEmpty()) {
                details.put("venue_id", venues.get(0).getId());
                details.put("title", "Meeting at " + venues.get(0).getName());
            }
        }
        
        // Extract date
        LocalDate date = extractDate(message);
        if (date != null) {
            details.put("date", date.toString());
        }
        
        // Extract time
        LocalTime time = extractTime(message);
        if (time != null) {
            details.put("start_time", time.toString());
            details.put("end_time", time.plusHours(1).toString()); // Default 1 hour duration
        }
        
        return details;
    }
    
    /**
     * Extract venue name from message
     * @param message User message
     * @return String venue name or null
     */
    private String extractVenueName(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Common venue patterns
        if (lowerMessage.contains("auditorium") || lowerMessage.contains("main hall")) {
            return "auditorium";
        }
        if (lowerMessage.contains("conference room a") || lowerMessage.contains("room a")) {
            return "conference room a";
        }
        if (lowerMessage.contains("seminar hall") || lowerMessage.contains("hall b")) {
            return "seminar hall";
        }
        if (lowerMessage.contains("meeting room") || lowerMessage.contains("room c")) {
            return "meeting room";
        }
        
        return null;
    }
    
    /**
     * Extract date from message
     * @param message User message
     * @return LocalDate or null
     */
    private LocalDate extractDate(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("today")) {
            return LocalDate.now();
        }
        if (lowerMessage.contains("tomorrow")) {
            return LocalDate.now().plusDays(1);
        }
        if (lowerMessage.contains("next week")) {
            return LocalDate.now().plusWeeks(1);
        }
        
        // Try to extract specific date patterns
        Pattern datePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");
        Matcher matcher = datePattern.matcher(message);
        if (matcher.find()) {
            try {
                int month = Integer.parseInt(matcher.group(1));
                int day = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                // Invalid date format
            }
        }
        
        return null;
    }
    
    /**
     * Extract time from message
     * @param message User message
     * @return LocalTime or null
     */
    private LocalTime extractTime(String message) {
        // Pattern for time like "2 PM", "14:00", "2:30 PM"
        Pattern timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))? ?(am|pm|AM|PM)?");
        Matcher matcher = timePattern.matcher(message);
        
        if (matcher.find()) {
            try {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                String ampm = matcher.group(3);
                
                if (ampm != null && ampm.toLowerCase().equals("pm") && hour != 12) {
                    hour += 12;
                } else if (ampm != null && ampm.toLowerCase().equals("am") && hour == 12) {
                    hour = 0;
                }
                
                return LocalTime.of(hour, minute);
            } catch (Exception e) {
                // Invalid time format
            }
        }
        
        return null;
    }
}

