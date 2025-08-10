package com.smartslot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Column(name = "contact_number")
    private String contactNumber;
    
    @Column(name = "expected_attendees")
    private Integer expectedAttendees;
    
    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;
    
    @Column(name = "purpose")
    private String purpose;
    
    @Column(name = "otp_code")
    private String otpCode;
    
    @Column(name = "otp_verified")
    private Boolean otpVerified = false;
    
    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;
    
    @Column(name = "recurring_info", columnDefinition = "TEXT")
    private String recurringInfo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    // Constructors
    public Booking() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Booking(String title, Venue venue, User user, LocalDate bookingDate, 
                   LocalTime startTime, LocalTime endTime) {
        this();
        this.title = title;
        this.venue = venue;
        this.user = user;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Venue getVenue() {
        return venue;
    }
    
    public void setVenue(Venue venue) {
        this.venue = venue;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public Integer getExpectedAttendees() {
        return expectedAttendees;
    }
    
    public void setExpectedAttendees(Integer expectedAttendees) {
        this.expectedAttendees = expectedAttendees;
    }
    
    public String getSpecialRequirements() {
        return specialRequirements;
    }
    
    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public String getOtpCode() {
        return otpCode;
    }
    
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    public Boolean getOtpVerified() {
        return otpVerified;
    }
    
    public void setOtpVerified(Boolean otpVerified) {
        this.otpVerified = otpVerified;
    }
    
    public LocalDateTime getOtpExpiresAt() {
        return otpExpiresAt;
    }
    
    public void setOtpExpiresAt(LocalDateTime otpExpiresAt) {
        this.otpExpiresAt = otpExpiresAt;
    }
    
    public String getRecurringInfo() {
        return recurringInfo;
    }
    
    public void setRecurringInfo(String recurringInfo) {
        this.recurringInfo = recurringInfo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    
    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum BookingStatus {
        PENDING, APPROVED, CONFIRMED, REJECTED, CANCELLED
    }
}

