package com.smartslot.service;

import com.smartslot.model.Booking;
import com.smartslot.model.User;
import com.smartslot.model.Venue;
import com.smartslot.repository.BookingRepository;
import com.smartslot.repository.UserRepository;
import com.smartslot.repository.VenueRepository;
import com.smartslot.util.OtpUtil;
import com.smartslot.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OtpUtil otpUtil;
    @Autowired
    private EmailUtil emailUtil;
    
    /**
     * Create new booking
     * @param booking Booking to create
     * @param user User creating the booking
     * @return Map containing booking result
     */
    public Map<String, Object> createBooking(Booking booking, User user) {
        Map<String, Object> response = new HashMap<>();
        
        // Check for conflicts
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            booking.getVenue(), booking.getBookingDate(), 
            booking.getStartTime(), booking.getEndTime()
        );
        
        if (!conflicts.isEmpty()) {
            response.put("success", false);
            response.put("error", "Time slot conflicts with existing booking");
            response.put("conflicting_booking", conflicts.get(0));
            return response;
        }
        
        // Set user and generate OTP
        booking.setUser(user);
        String otp = otpUtil.generateOtp();
        booking.setOtpCode(otp);
        booking.setOtpExpiresAt(otpUtil.getOtpExpiryTime());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        response.put("success", true);
        response.put("booking_id", savedBooking.getId());
        response.put("otp", otp);
        response.put("message", "Booking created successfully. Please verify with OTP.");
        
        return response;
    }
    
    /**
     * Verify OTP for booking
     * @param bookingId Booking ID
     * @param providedOtp OTP provided by user
     * @return Map containing verification result
     */
    public Map<String, Object> verifyOtp(Long bookingId, String providedOtp) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            response.put("success", false);
            response.put("error", "Booking not found");
            return response;
        }
        
        Booking booking = optionalBooking.get();
        
        if (otpUtil.validateOtp(providedOtp, booking.getOtpCode(), booking.getOtpExpiresAt())) {
            booking.setOtpVerified(true);
            bookingRepository.save(booking);
            
            response.put("success", true);
            response.put("message", "Booking confirmed successfully");
            response.put("booking", booking);
        } else {
            response.put("success", false);
            response.put("error", "Invalid or expired OTP");
        }
        
        return response;
    }
    
    /**
     * Get user bookings
     * @param user User
     * @return List of user bookings
     */
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get user bookings (for session-based auth)
     * @param userObj User object from session
     * @return List of user bookings
     */
    public List<Booking> getUserBookings(Object userObj) {
        if (userObj instanceof User) {
            User user = (User) userObj;
            System.out.println("BookingService: Processing user bookings for user: " + user.getEmail() + ", ID: " + user.getId());
            
            // Check if the user has an ID (is a proper entity)
            if (user.getId() != null) {
                System.out.println("BookingService: User has ID, using direct lookup");
                return getUserBookings(user);
            } else {
                // User doesn't have an ID, try to find by email
                if (user.getEmail() != null) {
                    System.out.println("BookingService: User doesn't have ID, looking up by email: " + user.getEmail());
                    Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
                    if (existingUser.isPresent()) {
                        System.out.println("BookingService: Found existing user by email, ID: " + existingUser.get().getId());
                        return getUserBookings(existingUser.get());
                    } else {
                        System.out.println("BookingService: No existing user found by email: " + user.getEmail());
                        // User doesn't exist in database, return empty list
                        return new ArrayList<>();
                    }
                } else {
                    System.out.println("BookingService: User email is null");
                    return new ArrayList<>();
                }
            }
        } else {
            System.out.println("BookingService: userObj is not a User instance: " + (userObj != null ? userObj.getClass().getName() : "null"));
            // Handle case where userObj might be a different type
            // For now, return empty list
            return new ArrayList<>();
        }
    }
    
    /**
     * Get user bookings filtered by status
     * @param userObj User object from session
     * @param status Booking status to filter by
     * @return List of filtered user bookings
     */
    public List<Booking> getUserBookingsByStatus(Object userObj, Booking.BookingStatus status) {
        if (userObj instanceof User) {
            User user = (User) userObj;
            
            // Check if the user has an ID (is a proper entity)
            if (user.getId() != null) {
                return bookingRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
            } else {
                // User doesn't have an ID, try to find by email
                if (user.getEmail() != null) {
                    Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
                    if (existingUser.isPresent()) {
                        return bookingRepository.findByUserAndStatusOrderByCreatedAtDesc(existingUser.get(), status);
                    }
                }
            }
        }
        return new ArrayList<>();
    }
    
    /**
     * Get all bookings (admin only)
     * @return List of all bookings
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllWithUserAndVenue();
    }
    
    /**
     * Get pending bookings
     * @return List of pending bookings
     */
    public List<Booking> getPendingBookings() {
        return bookingRepository.findPendingBookingsOrderByCreatedAt();
    }
    
    /**
     * Approve booking
     * @param bookingId Booking ID
     * @param approvedBy Admin who approved
     * @return Map containing approval result
     */
    public Map<String, Object> approveBooking(Long bookingId, String approvedBy) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            response.put("success", false);
            response.put("error", "Booking not found");
            return response;
        }
        
        Booking booking = optionalBooking.get();
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            response.put("success", false);
            response.put("error", "Only pending bookings can be approved");
            return response;
        }
        
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setApprovedAt(LocalDateTime.now());
        booking.setConfirmedAt(LocalDateTime.now());
        booking.setApprovedBy(approvedBy);
        bookingRepository.save(booking);
        // Send approval email
        if (booking.getUser() != null && booking.getUser().getEmail() != null) {
            String emailContent = String.format(
                "Dear %s,\n\n" +
                "🎉 Your booking has been CONFIRMED!\n\n" +
                "Booking Details:\n" +
                "• Booking ID: %d\n" +
                "• Venue: %s\n" +
                "• Date: %s\n" +
                "• Time: %s - %s\n" +
                "• Purpose: %s\n\n" +
                "Your venue booking is now confirmed and ready for use. Please arrive on time and enjoy your event!\n\n" +
                "If you have any questions, please contact the administration.\n\n" +
                "Best regards,\nSmart Slot Booking System",
                booking.getUser().getName(),
                booking.getId(),
                booking.getVenue().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
            );
            
            emailUtil.sendEmail(
                booking.getUser().getEmail(),
                "🎉 Booking Confirmed - Smart Slot Booking System",
                emailContent
            );
        }
        
        response.put("success", true);
        response.put("message", "Booking confirmed successfully");
        response.put("booking", booking);
        
        return response;
    }
    
    /**
     * Reject booking
     * @param bookingId Booking ID
     * @return Map containing rejection result
     */
    public Map<String, Object> rejectBooking(Long bookingId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            response.put("success", false);
            response.put("error", "Booking not found");
            return response;
        }
        
        Booking booking = optionalBooking.get();
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            response.put("success", false);
            response.put("error", "Only pending bookings can be rejected");
            return response;
        }
        
        booking.setStatus(Booking.BookingStatus.REJECTED);
        bookingRepository.save(booking);
        // Send rejection email
        if (booking.getUser() != null && booking.getUser().getEmail() != null) {
            String emailContent = String.format(
                "Dear %s,\n\n" +
                "❌ Your booking has been REJECTED\n\n" +
                "Booking Details:\n" +
                "• Booking ID: %d\n" +
                "• Venue: %s\n" +
                "• Date: %s\n" +
                "• Time: %s - %s\n" +
                "• Purpose: %s\n\n" +
                "Unfortunately, your venue booking request could not be approved at this time. This may be due to:\n" +
                "• Venue unavailability\n" +
                "• Scheduling conflicts\n" +
                "• Administrative requirements\n\n" +
                "You can submit a new booking request for a different time slot or venue.\n\n" +
                "If you have any questions, please contact the administration.\n\n" +
                "Best regards,\nSmart Slot Booking System",
                booking.getUser().getName(),
                booking.getId(),
                booking.getVenue().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
            );
            
            emailUtil.sendEmail(
                booking.getUser().getEmail(),
                "❌ Booking Rejected - Smart Slot Booking System",
                emailContent
            );
        }
        
        response.put("success", true);
        response.put("message", "Booking rejected successfully");
        response.put("booking", booking);
        
        return response;
    }
    
    /**
     * Cancel booking
     * @param bookingId Booking ID
     * @param user User
     * @return Map containing cancellation result
     */
    public Map<String, Object> cancelBooking(Long bookingId, User user) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            response.put("success", false);
            response.put("error", "Booking not found");
            return response;
        }
        
        Booking booking = optionalBooking.get();
        
        // Check if user owns this booking
        if (!booking.getUser().getId().equals(user.getId())) {
            response.put("success", false);
            response.put("error", "You can only cancel your own bookings");
            return response;
        }
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            response.put("success", false);
            response.put("error", "Only pending bookings can be cancelled");
            return response;
        }
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        response.put("success", true);
        response.put("message", "Booking cancelled successfully");
        response.put("booking", booking);
        
        return response;
    }
    
    /**
     * Cancel booking (for session-based auth)
     * @param bookingId Booking ID
     * @param userObj User object from session
     * @return Map containing cancellation result
     */
    public Map<String, Object> cancelBooking(Long bookingId, Object userObj) {
        if (userObj instanceof User) {
            return cancelBooking(bookingId, (User) userObj);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Invalid user object");
            return response;
        }
    }

    /**
     * Admin cancel booking
     * @param bookingId Booking ID
     * @return Map containing cancellation result
     */
    public Map<String, Object> adminCancelBooking(Long bookingId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            response.put("success", false);
            response.put("error", "Booking not found");
            return response;
        }
        
        Booking booking = optionalBooking.get();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        response.put("success", true);
        response.put("message", "Booking cancelled successfully");
        
        return response;
    }
    
    /**
     * Check availability for venue on specific date
     * @param venueId Venue ID
     * @param date Date to check
     * @return Map containing availability information
     */
    public Map<String, Object> checkAvailability(Long venueId, LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Venue> optionalVenue = venueRepository.findById(venueId);
        if (optionalVenue.isEmpty()) {
            response.put("success", false);
            response.put("error", "Venue not found");
            return response;
        }
        
        Venue venue = optionalVenue.get();
        List<Booking> existingBookings = bookingRepository.findByVenueAndBookingDate(venue, date);
        
        // Generate time slots (9 AM to 6 PM, 1-hour slots)
        List<Map<String, Object>> slots = new ArrayList<>();
        for (int hour = 9; hour < 18; hour++) {
            LocalTime startTime = LocalTime.of(hour, 0);
            LocalTime endTime = LocalTime.of(hour + 1, 0);
            
            boolean available = existingBookings.stream()
                .noneMatch(booking -> 
                    booking.getStatus() != Booking.BookingStatus.CANCELLED &&
                    booking.getStatus() != Booking.BookingStatus.REJECTED &&
                    isTimeOverlapping(startTime, endTime, booking.getStartTime(), booking.getEndTime())
                );
            
            Map<String, Object> slot = new HashMap<>();
            slot.put("start_time", startTime.toString());
            slot.put("end_time", endTime.toString());
            slot.put("available", available);
            slots.add(slot);
        }
        
        response.put("success", true);
        response.put("venue", venue);
        response.put("date", date.toString());
        response.put("slots", slots);
        response.put("existing_bookings", existingBookings);
        
        return response;
    }
    
    /**
     * Get calendar view for venue
     * @param venueId Venue ID
     * @param month Month
     * @param year Year
     * @return Map containing calendar data
     */
    public Map<String, Object> getCalendarView(Long venueId, int month, int year) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Venue> optionalVenue = venueRepository.findById(venueId);
        if (optionalVenue.isEmpty()) {
            response.put("success", false);
            response.put("error", "Venue not found");
            return response;
        }
        
        Venue venue = optionalVenue.get();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<Booking> monthBookings = bookingRepository.findByBookingDateBetween(startDate, endDate);
        
        Map<String, Object> calendar = new HashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            List<Booking> dayBookings = monthBookings.stream()
                .filter(booking -> booking.getBookingDate().equals(currentDate) &&
                                 booking.getVenue().getId().equals(venueId))
                .toList();
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("bookings", dayBookings);
            dayData.put("booking_count", dayBookings.size());
            
            // Determine availability
            String availability = "full";
            if (dayBookings.size() > 0) {
                availability = dayBookings.size() >= 9 ? "none" : "partial";
            }
            dayData.put("availability", availability);
            
            calendar.put(date.toString(), dayData);
        }
        
        response.put("success", true);
        response.put("venue", venue);
        response.put("month", month);
        response.put("year", year);
        response.put("calendar", calendar);
        
        return response;
    }
    
    /**
     * Get today's bookings
     * @return List of today's bookings
     */
    public List<Booking> getTodaysBookings() {
        return bookingRepository.findTodaysBookings();
    }
    
    /**
     * Get bookings by user email
     * @param email User email
     * @return List of bookings for the user
     */
    public List<Booking> getBookingsByUserEmail(String email) {
        return bookingRepository.findByUserEmail(email);
    }
    
    /**
     * Get booking by ID
     * @param bookingId Booking ID
     * @return Booking or null if not found
     */
    public Booking getBookingById(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        return booking.orElse(null);
    }
    
    /**
     * Check if two time ranges overlap
     * @param start1 Start time of first range
     * @param end1 End time of first range
     * @param start2 Start time of second range
     * @param end2 End time of second range
     * @return boolean true if overlapping
     */
    private boolean isTimeOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    /**
     * Check if a slot is available for booking
     * @param venueId Venue ID
     * @param date Date string (YYYY-MM-DD)
     * @param startTime Start time string (HH:mm)
     * @param endTime End time string (HH:mm)
     * @return boolean true if slot is available
     */
    public boolean isSlotAvailable(Long venueId, String date, String startTime, String endTime) {
        LocalDate bookingDate = LocalDate.parse(date);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        Optional<Venue> venue = venueRepository.findById(venueId);
        if (venue.isEmpty()) {
            return false;
        }
        
        List<Booking> existingBookings = bookingRepository.findByVenueAndBookingDate(venue.get(), bookingDate);
        
        return existingBookings.stream()
            .noneMatch(booking -> 
                booking.getStatus() != Booking.BookingStatus.CANCELLED &&
                booking.getStatus() != Booking.BookingStatus.REJECTED &&
                isTimeOverlapping(start, end, booking.getStartTime(), booking.getEndTime())
            );
    }
    
    /**
     * Save a pending booking
     * @param userObj User object
     * @param venueId Venue ID
     * @param date Date string (YYYY-MM-DD)
     * @param startTime Start time string (HH:mm)
     * @param endTime End time string (HH:mm)
     * @return Saved booking
     */
    public Booking savePendingBooking(Object userObj, Long venueId, String date, String startTime, String endTime, String title, String purpose) {
        User user = (User) userObj;
        Optional<Venue> venue = venueRepository.findById(venueId);
        if (venue.isEmpty()) {
            throw new RuntimeException("Venue not found");
        }
        
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setVenue(venue.get());
        booking.setBookingDate(LocalDate.parse(date));
        booking.setStartTime(LocalTime.parse(startTime));
        booking.setEndTime(LocalTime.parse(endTime));
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        
        // Set title (use provided title or default)
        if (title != null && !title.trim().isEmpty()) {
            booking.setTitle(title);
        } else {
            booking.setTitle("Booking for " + venue.get().getName() + " on " + date);
        }
        
        // Set description
        booking.setDescription("Booking from " + startTime + " to " + endTime + " at " + venue.get().getName());
        
        // Set purpose (use provided purpose or default)
        if (purpose != null && !purpose.trim().isEmpty()) {
            booking.setPurpose(purpose);
        } else {
            booking.setPurpose("General booking");
        }
        
        return bookingRepository.save(booking);
    }

    public void markOtpAsVerified(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setOtpVerified(true);
            bookingRepository.save(booking);
        }
    }
    
    /**
     * Confirm a booking by ID
     * @param bookingId Booking ID
     */
    public void confirmBooking(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {
            Booking b = booking.get();
            b.setStatus(Booking.BookingStatus.CONFIRMED);
            b.setConfirmedAt(LocalDateTime.now());
            bookingRepository.save(b);
        }
    }
    
    /**
     * Get bookings for a venue in a date range
     * @param venueId Venue ID
     * @param start Start date
     * @param end End date
     * @return List of bookings
     */
    public List<Booking> getBookingsForVenueAndDateRange(Long venueId, LocalDate start, LocalDate end) {
        Optional<Venue> venue = venueRepository.findById(venueId);
        if (venue.isEmpty()) {
            return new ArrayList<>();
        }
        
        return bookingRepository.findByVenueAndBookingDateBetween(venue.get(), start, end);
    }
    
    /**
     * Check if a day is available for a venue
     * @param venueId Venue ID
     * @param date Date to check
     * @param bookings List of existing bookings
     * @return boolean true if day is available
     */
    public boolean isDayAvailable(Long venueId, LocalDate date, List<Booking> bookings) {
        return bookings.stream()
            .filter(booking -> booking.getVenue().getId().equals(venueId) && 
                              booking.getBookingDate().equals(date))
            .filter(booking -> booking.getStatus() != Booking.BookingStatus.CANCELLED &&
                              booking.getStatus() != Booking.BookingStatus.REJECTED)
            .count() < 9; // Assuming 9 slots per day (9 AM to 6 PM)
    }
    
    /**
     * Suggest alternate booking slots
     * @param venueId Venue ID
     * @param date Date string (YYYY-MM-DD)
     * @param startTime Start time string (HH:mm)
     * @param endTime End time string (HH:mm)
     * @return Map with suggestions
     */
    public Map<String, Object> suggestAlternateSlots(Long venueId, String date, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        LocalDate bookingDate = LocalDate.parse(date);
        LocalTime requestedStart = LocalTime.parse(startTime);
        LocalTime requestedEnd = LocalTime.parse(endTime);
        
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        // Suggest same time on different days (next 7 days)
        for (int i = 1; i <= 7; i++) {
            LocalDate suggestedDate = bookingDate.plusDays(i);
            if (isSlotAvailable(venueId, suggestedDate.toString(), startTime, endTime)) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("date", suggestedDate.toString());
                suggestion.put("startTime", startTime);
                suggestion.put("endTime", endTime);
                suggestion.put("type", "same_time_different_day");
                suggestions.add(suggestion);
            }
        }
        
        // Suggest different times on the same day
        List<LocalTime> timeSlots = Arrays.asList(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
        );
        
        for (LocalTime slot : timeSlots) {
            LocalTime slotEnd = slot.plusHours(1);
            if (!slot.equals(requestedStart) && 
                isSlotAvailable(venueId, date, slot.toString(), slotEnd.toString())) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("date", date);
                suggestion.put("startTime", slot.toString());
                suggestion.put("endTime", slotEnd.toString());
                suggestion.put("type", "different_time_same_day");
                suggestions.add(suggestion);
            }
        }
        
        result.put("original_request", Map.of(
            "venueId", venueId,
            "date", date,
            "startTime", startTime,
            "endTime", endTime
        ));
        result.put("suggestions", suggestions);
        result.put("suggestion_count", suggestions.size());
        
        return result;
    }
    
    /**
     * Enhanced booking creation with conflict detection and status management
     * @param userObj User object
     * @param venueId Venue ID
     * @param date Date string (YYYY-MM-DD)
     * @param startTime Start time string (HH:mm)
     * @param endTime End time string (HH:mm)
     * @param title Event title
     * @param purpose Event purpose
     * @return Map containing booking result
     */
    public Map<String, Object> createEnhancedBooking(Object userObj, Long venueId, String date, String startTime, String endTime, String title, String purpose) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = (User) userObj;
            LocalDate bookingDate = LocalDate.parse(date);
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            
            // Validate input
            if (start.isAfter(end)) {
                result.put("success", false);
                result.put("error", "Start time cannot be after end time");
                return result;
            }
            
            if (bookingDate.isBefore(LocalDate.now())) {
                result.put("success", false);
                result.put("error", "Cannot book for past dates");
                return result;
            }
            
            Optional<Venue> venue = venueRepository.findById(venueId);
            if (venue.isEmpty()) {
                result.put("success", false);
                result.put("error", "Venue not found");
                return result;
            }
            
            // Check for conflicts
            BookingConflict conflict = checkBookingConflicts(venue.get(), bookingDate, start, end);
            
            if (conflict.hasConflict()) {
                result.put("success", false);
                result.put("error", conflict.getMessage());
                result.put("conflictType", conflict.getType());
                result.put("conflictingBooking", conflict.getConflictingBooking());
                return result;
            }
            
            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setVenue(venue.get());
            booking.setBookingDate(bookingDate);
            booking.setStartTime(start);
            booking.setEndTime(end);
            booking.setTitle(title != null ? title : "Booking for " + venue.get().getName() + " on " + date);
            booking.setPurpose(purpose != null ? purpose : "General booking");
            booking.setDescription("Booking from " + startTime + " to " + endTime + " at " + venue.get().getName());
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setCreatedAt(LocalDateTime.now());
            
            // Always set to PENDING to require OTP verification
            booking.setStatus(Booking.BookingStatus.PENDING);
            
            Booking savedBooking = bookingRepository.save(booking);
            
            result.put("success", true);
            result.put("booking", savedBooking);
            result.put("status", savedBooking.getStatus().toString());
            result.put("message", "Booking created successfully! Please check your email for OTP verification.");
            
            return result;
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to create booking: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Check for booking conflicts
     * @param venue Venue
     * @param date Booking date
     * @param startTime Start time
     * @param endTime End time
     * @return BookingConflict object
     */
    private BookingConflict checkBookingConflicts(Venue venue, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Booking> existingBookings = bookingRepository.findByVenueAndBookingDate(venue, date);
        
        for (Booking existingBooking : existingBookings) {
            // Skip cancelled and rejected bookings
            if (existingBooking.getStatus() == Booking.BookingStatus.CANCELLED || 
                existingBooking.getStatus() == Booking.BookingStatus.REJECTED) {
                continue;
            }
            
            // Check for time overlap
            if (isTimeOverlapping(startTime, endTime, existingBooking.getStartTime(), existingBooking.getEndTime())) {
                String message = "Time slot conflicts with existing " + existingBooking.getStatus().toString().toLowerCase() + " booking";
                return new BookingConflict(true, message, existingBooking.getStatus().toString(), existingBooking);
            }
        }
        
        return new BookingConflict(false, null, null, null);
    }
    
    /**
     * Auto-expire pending bookings after specified minutes
     * @param expiryMinutes Minutes after which pending bookings expire
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void expirePendingBookings() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(30); // 30 minutes default
        
        List<Booking> pendingBookings = bookingRepository.findByStatusAndCreatedAtBefore(
            Booking.BookingStatus.PENDING, expiryTime);
        
        for (Booking booking : pendingBookings) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            
            // Send notification to user
            if (booking.getUser() != null && booking.getUser().getEmail() != null) {
                try {
                    emailUtil.sendEmail(
                        booking.getUser().getEmail(),
                        "Booking Expired",
                        "Your pending booking for " + booking.getVenue().getName() + 
                        " on " + booking.getBookingDate() + " has expired due to inactivity."
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send expiry email: " + e.getMessage());
                }
            }
        }
        
        if (!pendingBookings.isEmpty()) {
            System.out.println("Expired " + pendingBookings.size() + " pending bookings");
        }
    }
    
    /**
     * Get available time slots for a venue on a specific date
     * @param venueId Venue ID
     * @param date Date
     * @return List of available time slots
     */
    public List<Map<String, Object>> getAvailableTimeSlots(Long venueId, LocalDate date) {
        List<Map<String, Object>> availableSlots = new ArrayList<>();
        
        Optional<Venue> venue = venueRepository.findById(venueId);
        if (venue.isEmpty()) {
            return availableSlots;
        }
        
        // Define business hours (9 AM to 6 PM)
        LocalTime businessStart = LocalTime.of(9, 0);
        LocalTime businessEnd = LocalTime.of(18, 0);
        int slotDuration = 60; // 1 hour slots
        
        List<Booking> existingBookings = bookingRepository.findByVenueAndBookingDate(venue.get(), date);
        
        LocalTime currentTime = businessStart;
        while (currentTime.plusHours(1).isBefore(businessEnd) || currentTime.plusHours(1).equals(businessEnd)) {
            LocalTime slotStart = currentTime;
            LocalTime slotEnd = currentTime.plusHours(1);
            
            boolean isAvailable = true;
            for (Booking booking : existingBookings) {
                if ((booking.getStatus() == Booking.BookingStatus.CONFIRMED || 
                     booking.getStatus() == Booking.BookingStatus.PENDING) &&
                    isTimeOverlapping(slotStart, slotEnd, booking.getStartTime(), booking.getEndTime())) {
                    isAvailable = false;
                    break;
                }
            }
            
            if (isAvailable) {
                Map<String, Object> slot = new HashMap<>();
                slot.put("startTime", slotStart.toString());
                slot.put("endTime", slotEnd.toString());
                slot.put("available", true);
                availableSlots.add(slot);
            }
            
            currentTime = currentTime.plusHours(1);
        }
        
        return availableSlots;
    }
    
    /**
     * Inner class to represent booking conflicts
     */
    private static class BookingConflict {
        private final boolean hasConflict;
        private final String message;
        private final String type;
        private final Booking conflictingBooking;
        
        public BookingConflict(boolean hasConflict, String message, String type, Booking conflictingBooking) {
            this.hasConflict = hasConflict;
            this.message = message;
            this.type = type;
            this.conflictingBooking = conflictingBooking;
        }
        
        public boolean hasConflict() { return hasConflict; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public Booking getConflictingBooking() { return conflictingBooking; }
    }
}

