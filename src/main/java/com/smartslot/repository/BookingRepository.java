package com.smartslot.repository;

import com.smartslot.model.Booking;
import com.smartslot.model.User;
import com.smartslot.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    
    List<Booking> findByVenueAndBookingDate(Venue venue, LocalDate date);
    
    List<Booking> findByVenueAndBookingDateBetween(Venue venue, LocalDate start, LocalDate end);
    
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' ORDER BY b.createdAt DESC")
    List<Booking> findPendingBookingsOrderByCreatedAt();
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :createdAt")
    List<Booking> findByStatusAndCreatedAtBefore(@Param("status") Booking.BookingStatus status, @Param("createdAt") LocalDateTime createdAt);
    
    List<Booking> findByUserEmail(String email);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingDate = CURRENT_DATE")
    List<Booking> findTodaysBookings();
    
    // Additional methods needed by services
    List<Booking> findByUser(User user);
    
    List<Booking> findByVenue(Venue venue);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    List<Booking> findByBookingDate(LocalDate bookingDate);
    
    // New methods for user bookings with status filtering
    List<Booking> findByUserAndStatusOrderByCreatedAtDesc(User user, Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findUserBookingsByStatus(@Param("user") User user, @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user = :user AND b.status = :status")
    Long countUserBookingsByStatus(@Param("user") User user, @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.venue = :venue AND b.bookingDate = :date AND " +
           "((b.startTime <= :startTime AND b.endTime > :startTime) OR " +
           "(b.startTime < :endTime AND b.endTime >= :endTime) OR " +
           "(b.startTime >= :startTime AND b.endTime <= :endTime)) AND " +
           "b.status IN ('PENDING', 'CONFIRMED')")
    List<Booking> findConflictingBookings(@Param("venue") Venue venue, 
                                         @Param("date") LocalDate date,
                                         @Param("startTime") LocalTime startTime, 
                                         @Param("endTime") LocalTime endTime);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByBookingDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") Booking.BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.venue = :venue AND b.status IN ('CONFIRMED', 'PENDING')")
    Long countActiveBookingsByVenue(@Param("venue") Venue venue);
    
    @Query("SELECT b FROM Booking b WHERE b.otpCode = :otpCode AND b.otpExpiresAt > :currentTime")
    Optional<Booking> findByOtpCodeAndOtpExpiresAtAfter(@Param("otpCode") String otpCode, 
                                                        @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingDate = :date AND b.status IN ('CONFIRMED', 'PENDING') ORDER BY b.startTime")
    List<Booking> findTodaysBookings(@Param("date") LocalDate date);
    
    @Query("SELECT v.name as venueName, COUNT(b) as bookingCount FROM Booking b " +
           "JOIN b.venue v WHERE b.status = 'CONFIRMED' " +
           "GROUP BY v.id, v.name ORDER BY bookingCount DESC")
    List<Object[]> findMostBookedVenues();
    
    @Query("SELECT EXTRACT(HOUR FROM b.startTime) as hour, COUNT(b) as bookingCount FROM Booking b " +
           "WHERE b.status = 'CONFIRMED' " +
           "GROUP BY EXTRACT(HOUR FROM b.startTime) ORDER BY bookingCount DESC")
    List<Object[]> findPeakHours();

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.venue")
    List<Booking> findAllWithUserAndVenue();
}

