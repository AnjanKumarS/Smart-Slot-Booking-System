package com.smartslot.service;

import com.smartslot.model.Booking;
import com.smartslot.repository.BookingRepository;
import com.smartslot.repository.UserRepository;
import com.smartslot.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get comprehensive analytics data
     * @return Map containing analytics information
     */
    public Map<String, Object> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Basic counts
        analytics.put("total_bookings", bookingRepository.count());
        analytics.put("total_venues", venueRepository.countActiveVenues());
        analytics.put("total_users", userRepository.count());
        
        // Status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Booking.BookingStatus status : Booking.BookingStatus.values()) {
            statusDistribution.put(status.toString(), bookingRepository.countByStatus(status));
        }
        analytics.put("status_distribution", statusDistribution);
        
        // Most booked venues
        List<Object[]> venueStats = bookingRepository.findMostBookedVenues();
        List<Map<String, Object>> mostBookedVenues = venueStats.stream()
            .limit(5)
            .map(stat -> {
                Map<String, Object> venue = new HashMap<>();
                venue.put("venue_name", stat[0]);
                venue.put("booking_count", stat[1]);
                return venue;
            })
            .collect(Collectors.toList());
        analytics.put("most_booked_venues", mostBookedVenues);
        
        // Peak hours
        List<Object[]> hourStats = bookingRepository.findPeakHours();
        List<Map<String, Object>> peakHours = hourStats.stream()
            .limit(5)
            .map(stat -> {
                Map<String, Object> hour = new HashMap<>();
                hour.put("hour", stat[0] + ":00");
                hour.put("booking_count", stat[1]);
                return hour;
            })
            .collect(Collectors.toList());
        analytics.put("peak_hours", peakHours);
        
        // Monthly trends (last 6 months)
        List<Map<String, Object>> monthlyTrends = getMonthlyTrends();
        analytics.put("monthly_trends", monthlyTrends);
        
        // Cancellation rate
        long totalBookings = bookingRepository.count();
        long cancelledBookings = bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED);
        double cancellationRate = totalBookings > 0 ? (double) cancelledBookings / totalBookings * 100 : 0;
        analytics.put("cancellation_rate", Math.round(cancellationRate * 100.0) / 100.0);
        
        // Confirmation rate
        long confirmedBookings = bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);
        long rejectedBookings = bookingRepository.countByStatus(Booking.BookingStatus.REJECTED);
        long processedBookings = confirmedBookings + rejectedBookings;
        double confirmationRate = processedBookings > 0 ? (double) confirmedBookings / processedBookings * 100 : 0;
        analytics.put("confirmation_rate", Math.round(confirmationRate * 100.0) / 100.0);
        
        return analytics;
    }
    
    /**
     * Get booking statistics: totalBookings and statusCounts
     */
    public Map<String, Object> getBookingStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Booking> bookings = bookingRepository.findAll();
        stats.put("totalBookings", bookings.size());
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("CONFIRMED", bookings.stream().filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED).count());
        statusCounts.put("REJECTED", bookings.stream().filter(b -> b.getStatus() == Booking.BookingStatus.REJECTED).count());
        statusCounts.put("PENDING", bookings.stream().filter(b -> b.getStatus() == Booking.BookingStatus.PENDING).count());
        statusCounts.put("CANCELLED", bookings.stream().filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED).count());
        stats.put("statusCounts", statusCounts);
        return stats;
    }
    
    /**
     * Get monthly booking trends for the last 6 months
     * @return List of monthly trend data
     */
    private List<Map<String, Object>> getMonthlyTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(5).withDayOfMonth(1);
        
        for (LocalDate date = startDate; !date.isAfter(endDate.withDayOfMonth(1)); date = date.plusMonths(1)) {
            LocalDate monthStart = date.withDayOfMonth(1);
            LocalDate monthEnd = date.withDayOfMonth(date.lengthOfMonth());
            
            List<Booking> monthBookings = bookingRepository.findByBookingDateBetween(monthStart, monthEnd);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", date.format(DateTimeFormatter.ofPattern("MMM yyyy")));
            monthData.put("booking_count", monthBookings.size());
            monthData.put("confirmed_count", monthBookings.stream()
                .mapToInt(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED ? 1 : 0)
                .sum());
            monthData.put("cancelled_count", monthBookings.stream()
                .mapToInt(b -> b.getStatus() == Booking.BookingStatus.CANCELLED ? 1 : 0)
                .sum());
            
            trends.add(monthData);
        }
        
        return trends;
    }
    
    /**
     * Get venue utilization statistics
     * @return Map containing venue utilization data
     */
    public Map<String, Object> getVenueUtilization() {
        Map<String, Object> utilization = new HashMap<>();
        
        List<Object[]> venueStats = bookingRepository.findMostBookedVenues();
        List<Map<String, Object>> venueUtilization = venueStats.stream()
            .map(stat -> {
                Map<String, Object> venue = new HashMap<>();
                venue.put("venue_name", stat[0]);
                venue.put("booking_count", stat[1]);
                
                // Calculate utilization percentage (assuming 9 hours per day, 30 days per month)
                long bookingCount = (Long) stat[1];
                double maxPossibleBookings = 9 * 30; // 9 hours * 30 days
                double utilizationPercentage = (bookingCount / maxPossibleBookings) * 100;
                venue.put("utilization_percentage", Math.round(utilizationPercentage * 100.0) / 100.0);
                
                return venue;
            })
            .collect(Collectors.toList());
        
        utilization.put("venue_utilization", venueUtilization);
        return utilization;
    }
    
    /**
     * Get booking statistics for a specific date range
     * @param startDate Start date
     * @param endDate End date
     * @return Map containing date range statistics
     */
    public Map<String, Object> getDateRangeStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Booking> bookings = bookingRepository.findByBookingDateBetween(startDate, endDate);
        
        stats.put("total_bookings", bookings.size());
        stats.put("start_date", startDate.toString());
        stats.put("end_date", endDate.toString());
        
        // Status breakdown
        Map<String, Long> statusBreakdown = bookings.stream()
            .collect(Collectors.groupingBy(
                booking -> booking.getStatus().toString(),
                Collectors.counting()
            ));
        stats.put("status_breakdown", statusBreakdown);
        
        // Daily breakdown
        Map<String, Long> dailyBreakdown = bookings.stream()
            .collect(Collectors.groupingBy(
                booking -> booking.getBookingDate().toString(),
                Collectors.counting()
            ));
        stats.put("daily_breakdown", dailyBreakdown);
        
        return stats;
    }
    
    /**
     * Get user activity statistics
     * @return Map containing user activity data
     */
    public Map<String, Object> getUserActivityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // User role distribution
        Map<String, Long> roleDistribution = new HashMap<>();
        for (com.smartslot.model.User.UserRole role : com.smartslot.model.User.UserRole.values()) {
            roleDistribution.put(role.toString(), userRepository.countByRoleAndIsActiveTrue(role));
        }
        stats.put("role_distribution", roleDistribution);
        
        // Active users
        stats.put("active_users", userRepository.findByIsActiveTrue().size());
        
        return stats;
    }
}

