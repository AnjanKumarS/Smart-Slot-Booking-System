package com.smartslot.service;

import com.smartslot.model.Venue;
import com.smartslot.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class VenueService {
    
    @Autowired
    private VenueRepository venueRepository;
    
    // Get all venues
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    // Get all active venues
    public List<Venue> getActiveVenues() {
        return venueRepository.findByIsActiveTrue();
    }

    // Get all active venues (alias for compatibility)
    public List<Venue> getAllActiveVenues() {
        return venueRepository.findByIsActiveTrue();
    }
    
    // Get venues with demo data (for compatibility)
    public List<Venue> getVenuesWithDemoData() {
        List<Venue> venues = getAllActiveVenues();
        System.out.println("Current active venues in database: " + venues.size());
        
        // If no venues exist, create some basic ones
        if (venues.isEmpty()) {
            System.out.println("No venues found, creating basic venues...");
            venues = createBasicVenues();
            System.out.println("Created " + venues.size() + " basic venues");
        } else {
            System.out.println("Found " + venues.size() + " existing venues");
        }
        
        return venues;
    }

    // Create basic venues for testing
    private List<Venue> createBasicVenues() {
        List<Venue> basicVenues = new ArrayList<>();
        
        // 1. CS Auditorium
        Venue csAuditorium = new Venue();
        csAuditorium.setName("CS Auditorium");
        csAuditorium.setDescription("Computer Science Department's main auditorium with advanced AV systems");
        csAuditorium.setCapacity(300);
        csAuditorium.setLocation("CS Building, Ground Floor");
        csAuditorium.setHourlyRate(BigDecimal.ZERO); // Free for college venues
        csAuditorium.setAmenities(Arrays.asList("4K Projector", "Dolby Sound System", "Air Conditioning", "Stage", "Wireless Microphones", "Video Recording"));
        csAuditorium.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        basicVenues.add(venueRepository.save(csAuditorium));
        
        // 2. ISE Seminar Hall
        Venue iseSeminar = new Venue();
        iseSeminar.setName("ISE Seminar Hall");
        iseSeminar.setDescription("Information Science Engineering seminar hall with modern presentation facilities");
        iseSeminar.setCapacity(150);
        iseSeminar.setLocation("ISE Building, 1st Floor");
        iseSeminar.setHourlyRate(BigDecimal.ZERO); // Free for college venues
        iseSeminar.setAmenities(Arrays.asList("HD Projector", "Sound System", "Air Conditioning", "Podium", "Whiteboard"));
        iseSeminar.setImageUrl("https://images.unsplash.com/photo-1577412647305-991150c7d163?w=400&h=200&fit=crop");
        basicVenues.add(venueRepository.save(iseSeminar));
        
        // 3. Main Auditorium
        Venue mainAuditorium = new Venue();
        mainAuditorium.setName("Main Auditorium");
        mainAuditorium.setDescription("University's largest auditorium for major events and conferences");
        mainAuditorium.setCapacity(800);
        mainAuditorium.setLocation("Administrative Building, Ground Floor");
        mainAuditorium.setHourlyRate(BigDecimal.ZERO); // Free for college venues
        mainAuditorium.setAmenities(Arrays.asList("4K Projector", "Professional Sound System", "Air Conditioning", "Large Stage", "Multiple Microphones", "Green Room"));
        mainAuditorium.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        basicVenues.add(venueRepository.save(mainAuditorium));
        
        return basicVenues;
    }

    // Method to fix existing database records
    public void fixExistingVenues() {
        List<Venue> allVenues = venueRepository.findAll();
        for (Venue venue : allVenues) {
            boolean needsUpdate = false;
            
            // Fix hourly rate if it's not zero
            if (venue.getHourlyRate() == null || venue.getHourlyRate().compareTo(BigDecimal.ZERO) > 0) {
                venue.setHourlyRate(BigDecimal.ZERO);
                needsUpdate = true;
            }
            
            // Fix is_active - force it to be true for all venues
            venue.setIsActive(true);
            needsUpdate = true;
            
            if (needsUpdate) {
                venueRepository.save(venue);
            }
        }
    }

    // Get venue by ID
    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }
    
    // Create new venue
    public Venue createVenue(Venue venue) {
        venue.setCreatedAt(LocalDateTime.now());
        venue.setUpdatedAt(LocalDateTime.now());
        venue.setIsActive(true);
        
        // Set default values for college venues
        if (venue.getHourlyRate() == null) {
            venue.setHourlyRate(BigDecimal.ZERO); // Free for college venues
        }
        
        return venueRepository.save(venue);
    }
    
    // Update venue
    public Venue updateVenue(Long id, Venue venueDetails) {
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        if (optionalVenue.isPresent()) {
            Venue venue = optionalVenue.get();
            venue.setName(venueDetails.getName());
            venue.setDescription(venueDetails.getDescription());
            venue.setCapacity(venueDetails.getCapacity());
            venue.setLocation(venueDetails.getLocation());
            venue.setHourlyRate(venueDetails.getHourlyRate());
            venue.setAmenities(venueDetails.getAmenities());
            venue.setImageUrl(venueDetails.getImageUrl());
            venue.setIsActive(venueDetails.getIsActive());
            venue.setUpdatedAt(LocalDateTime.now());
            return venueRepository.save(venue);
        }
        return null;
    }
    
    // Delete venue (soft delete)
    public boolean deleteVenue(Long id) {
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        if (optionalVenue.isPresent()) {
            Venue venue = optionalVenue.get();
            venue.setIsActive(false);
            venue.setUpdatedAt(LocalDateTime.now());
            venueRepository.save(venue);
            return true;
        }
        return false;
    }
    
    // Hard delete venue
    public boolean hardDeleteVenue(Long id) {
        if (venueRepository.existsById(id)) {
            venueRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Search venues by name
    public List<Venue> searchVenuesByName(String name) {
        return venueRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
    }
    
    // Search venues by location
    public List<Venue> searchVenuesByLocation(String location) {
        return venueRepository.findByLocationContainingIgnoreCaseAndIsActiveTrue(location);
    }
    
    // Get venues by capacity range
    public List<Venue> getVenuesByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        return venueRepository.findByCapacityBetweenAndIsActiveTrue(minCapacity, maxCapacity);
    }

    // Get venues by price range
    public List<Venue> getVenuesByPriceRange(BigDecimal minRate, BigDecimal maxRate) {
        return venueRepository.findByHourlyRateBetweenAndIsActiveTrue(minRate, maxRate);
    }
    
    // Get venues with minimum capacity
    public List<Venue> getVenuesByMinCapacity(Integer minCapacity) {
        return venueRepository.findByCapacityGreaterThanEqualAndIsActiveTrue(minCapacity);
    }

    // Count active venues
    public Long getActiveVenueCount() {
        return venueRepository.countActiveVenues();
    }

    // Check if venue exists
    public boolean venueExists(Long id) {
        return venueRepository.existsById(id);
    }

    // Check if venue name exists (for validation)
    public boolean venueNameExists(String name) {
        return venueRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name).stream()
                .anyMatch(venue -> venue.getName().equalsIgnoreCase(name));
    }

    // Check if venue name exists excluding current venue (for update validation)
    public boolean venueNameExistsExcludingCurrent(String name, Long currentId) {
        return venueRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name).stream()
                .anyMatch(venue -> venue.getName().equalsIgnoreCase(name) && !venue.getId().equals(currentId));
    }
}

