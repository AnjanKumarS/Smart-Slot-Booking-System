package com.smartslot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venues")
public class Venue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Column(nullable = false)
    private String location;
    
    @JsonProperty("hourly_rate")
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;
    
    @JsonIgnore
    @ElementCollection
    @CollectionTable(name = "venue_amenities", joinColumns = @JoinColumn(name = "venue_id"))
    @Column(name = "amenity")
    private List<String> amenities;
    
    @JsonProperty("image_url")
    @Column(name = "image_url")
    private String imageUrl;
    
    @JsonProperty("is_active")
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @JsonProperty("created_at")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @JsonIgnore
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
    
    // Constructors
    public Venue() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Venue(String name, String description, Integer capacity, String location, BigDecimal hourlyRate) {
        this();
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.location = location;
        this.hourlyRate = hourlyRate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }
    
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    
    public List<String> getAmenities() {
        return amenities;
    }
    
    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
    
    /**
     * Get amenities for JSON serialization
     * @return List of amenities
     */
    @JsonProperty("amenities")
    public List<String> getAmenitiesForJson() {
        return amenities;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    public List<Booking> getBookings() {
        return bookings;
    }
    
    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

