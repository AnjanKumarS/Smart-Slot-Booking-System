package com.smartslot.service;

import com.smartslot.model.Venue;
import com.smartslot.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class VenueService {
    
    @Autowired
    private VenueRepository venueRepository;
    
    /**
     * Get all active venues
     * @return List of venues
     */
    public List<Venue> getAllActiveVenues() {
        return venueRepository.findByIsActiveTrue();
    }
    
    /**
     * Get venue by ID
     * @param id Venue ID
     * @return Optional<Venue>
     */
    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }
    
    /**
     * Create new venue
     * @param venue Venue to create
     * @return Created venue
     */
    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }
    
    /**
     * Update existing venue
     * @param id Venue ID
     * @param venueDetails Updated venue details
     * @return Updated venue or null if not found
     */
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
            return venueRepository.save(venue);
        }
        return null;
    }
    
    /**
     * Delete venue (soft delete)
     * @param id Venue ID
     * @return boolean success status
     */
    public boolean deleteVenue(Long id) {
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        if (optionalVenue.isPresent()) {
            Venue venue = optionalVenue.get();
            venue.setIsActive(false);
            venueRepository.save(venue);
            return true;
        }
        return false;
    }
    
    /**
     * Search venues by name
     * @param name Search term
     * @return List of matching venues
     */
    public List<Venue> searchVenuesByName(String name) {
        return venueRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
    }
    
    /**
     * Filter venues by capacity
     * @param minCapacity Minimum capacity
     * @return List of venues with capacity >= minCapacity
     */
    public List<Venue> getVenuesByMinCapacity(Integer minCapacity) {
        return venueRepository.findByCapacityGreaterThanEqualAndIsActiveTrue(minCapacity);
    }
    
    /**
     * Filter venues by location
     * @param location Location search term
     * @return List of venues in matching location
     */
    public List<Venue> getVenuesByLocation(String location) {
        return venueRepository.findByLocationContainingIgnoreCaseAndIsActiveTrue(location);
    }
    
    /**
     * Filter venues by price range
     * @param minRate Minimum hourly rate
     * @param maxRate Maximum hourly rate
     * @return List of venues within price range
     */
    public List<Venue> getVenuesByPriceRange(BigDecimal minRate, BigDecimal maxRate) {
        return venueRepository.findByHourlyRateBetweenAndIsActiveTrue(minRate, maxRate);
    }
    
    /**
     * Get venues with demo data
     * @return List of demo venues
     */
    public List<Venue> getVenuesWithDemoData() {
        List<Venue> venues = getAllActiveVenues();
        System.out.println("Current active venues in database: " + venues.size());
        
        // If less than 15 venues exist, create demo data
        if (venues.size() < 15) {
            System.out.println("Found " + venues.size() + " venues, need at least 15. Creating demo venues...");
            venues = createDemoVenues();
            System.out.println("Created " + venues.size() + " demo venues");
        } else {
            System.out.println("Found " + venues.size() + " existing venues");
        }
        
        return venues;
    }
    
    /**
     * Create demo venues for testing
     * @return List of created demo venues
     */
    private List<Venue> createDemoVenues() {
        // Clear existing venues first to avoid duplicates
        List<Venue> existingVenues = venueRepository.findAll();
        if (!existingVenues.isEmpty()) {
            System.out.println("Clearing " + existingVenues.size() + " existing venues...");
            venueRepository.deleteAll();
        }
        
        List<Venue> demoVenues = new ArrayList<>();
        
        // 1. CS Auditorium
        Venue csAuditorium = new Venue();
        csAuditorium.setName("CS Auditorium");
        csAuditorium.setDescription("Computer Science Department's main auditorium with advanced AV systems");
        csAuditorium.setCapacity(300);
        csAuditorium.setLocation("CS Building, Ground Floor");
        csAuditorium.setHourlyRate(new BigDecimal("120.00"));
        csAuditorium.setAmenities(Arrays.asList("4K Projector", "Dolby Sound System", "Air Conditioning", "Stage", "Wireless Microphones", "Video Recording"));
        csAuditorium.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(csAuditorium));
        
        // 2. ISE Seminar Hall
        Venue iseSeminar = new Venue();
        iseSeminar.setName("ISE Seminar Hall");
        iseSeminar.setDescription("Information Science Engineering seminar hall with modern presentation facilities");
        iseSeminar.setCapacity(150);
        iseSeminar.setLocation("ISE Building, 1st Floor");
        iseSeminar.setHourlyRate(new BigDecimal("100.00"));
        iseSeminar.setAmenities(Arrays.asList("HD Projector", "Sound System", "Air Conditioning", "Podium", "Whiteboard"));
        iseSeminar.setImageUrl("https://images.unsplash.com/photo-1577412647305-991150c7d163?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(iseSeminar));
        
        // 3. EC Conference Room
        Venue ecConference = new Venue();
        ecConference.setName("EC Conference Room");
        ecConference.setDescription("Electronics & Communication department's conference room for technical meetings");
        ecConference.setCapacity(50);
        ecConference.setLocation("EC Building, 2nd Floor");
        ecConference.setHourlyRate(new BigDecimal("80.00"));
        ecConference.setAmenities(Arrays.asList("Projector", "Video Conferencing", "Air Conditioning", "Whiteboard", "Coffee Service"));
        ecConference.setImageUrl("https://images.unsplash.com/photo-1517502884422-41eaead166d4?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(ecConference));
        
        // 4. Main Auditorium
        Venue mainAuditorium = new Venue();
        mainAuditorium.setName("Main Auditorium");
        mainAuditorium.setDescription("University's largest auditorium for major events and conferences");
        mainAuditorium.setCapacity(800);
        mainAuditorium.setLocation("Administrative Building, Ground Floor");
        mainAuditorium.setHourlyRate(new BigDecimal("200.00"));
        mainAuditorium.setAmenities(Arrays.asList("4K Projector", "Professional Sound System", "Air Conditioning", "Large Stage", "Multiple Microphones", "Green Room"));
        mainAuditorium.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(mainAuditorium));
        
        // 5. ME Lab Hall
        Venue meLabHall = new Venue();
        meLabHall.setName("ME Lab Hall");
        meLabHall.setDescription("Mechanical Engineering laboratory hall for technical demonstrations");
        meLabHall.setCapacity(80);
        meLabHall.setLocation("ME Building, Ground Floor");
        meLabHall.setHourlyRate(new BigDecimal("90.00"));
        meLabHall.setAmenities(Arrays.asList("Projector", "Sound System", "Air Conditioning", "Lab Equipment", "Safety Equipment"));
        meLabHall.setImageUrl("https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(meLabHall));
        
        // 6. Civil Seminar Hall
        Venue civilSeminar = new Venue();
        civilSeminar.setName("Civil Seminar Hall");
        civilSeminar.setDescription("Civil Engineering department seminar hall for presentations and discussions");
        civilSeminar.setCapacity(120);
        civilSeminar.setLocation("Civil Building, 1st Floor");
        civilSeminar.setHourlyRate(new BigDecimal("85.00"));
        civilSeminar.setAmenities(Arrays.asList("Projector", "Sound System", "Air Conditioning", "Podium", "Display Models"));
        civilSeminar.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(civilSeminar));
        
        // 7. Library Conference Room
        Venue libraryConference = new Venue();
        libraryConference.setName("Library Conference Room");
        libraryConference.setDescription("Quiet conference room in the library building for academic meetings");
        libraryConference.setCapacity(30);
        libraryConference.setLocation("Library Building, 2nd Floor");
        libraryConference.setHourlyRate(new BigDecimal("60.00"));
        libraryConference.setAmenities(Arrays.asList("Projector", "Whiteboard", "Air Conditioning", "Quiet Environment"));
        libraryConference.setImageUrl("https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(libraryConference));
        
        // 8. Biotechnology Lab
        Venue biotechLab = new Venue();
        biotechLab.setName("Biotechnology Lab");
        biotechLab.setDescription("Biotechnology laboratory for research presentations and demonstrations");
        biotechLab.setCapacity(60);
        biotechLab.setLocation("Biotech Building, Ground Floor");
        biotechLab.setHourlyRate(new BigDecimal("95.00"));
        biotechLab.setAmenities(Arrays.asList("Projector", "Lab Equipment", "Air Conditioning", "Safety Equipment", "Microscopes"));
        biotechLab.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(biotechLab));
        
        // 9. Architecture Studio
        Venue archStudio = new Venue();
        archStudio.setName("Architecture Studio");
        archStudio.setDescription("Architecture department studio for design presentations and critiques");
        archStudio.setCapacity(40);
        archStudio.setLocation("Architecture Building, 1st Floor");
        archStudio.setHourlyRate(new BigDecimal("70.00"));
        archStudio.setAmenities(Arrays.asList("Projector", "Drawing Boards", "Air Conditioning", "Natural Light", "Display Space"));
        archStudio.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(archStudio));
        
        // 10. Chemistry Seminar Hall
        Venue chemSeminar = new Venue();
        chemSeminar.setName("Chemistry Seminar Hall");
        chemSeminar.setDescription("Chemistry department seminar hall for scientific presentations");
        chemSeminar.setCapacity(100);
        chemSeminar.setLocation("Chemistry Building, 2nd Floor");
        chemSeminar.setHourlyRate(new BigDecimal("75.00"));
        chemSeminar.setAmenities(Arrays.asList("Projector", "Sound System", "Air Conditioning", "Fume Hood", "Safety Equipment"));
        chemSeminar.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(chemSeminar));
        
        // 11. Physics Lecture Hall
        Venue physicsHall = new Venue();
        physicsHall.setName("Physics Lecture Hall");
        physicsHall.setDescription("Physics department lecture hall with demonstration equipment");
        physicsHall.setCapacity(120);
        physicsHall.setLocation("Physics Building, Ground Floor");
        physicsHall.setHourlyRate(new BigDecimal("80.00"));
        physicsHall.setAmenities(Arrays.asList("Projector", "Sound System", "Air Conditioning", "Demonstration Equipment", "Blackboard"));
        physicsHall.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(physicsHall));
        
        // 12. Mathematics Conference Room
        Venue mathConference = new Venue();
        mathConference.setName("Mathematics Conference Room");
        mathConference.setDescription("Mathematics department conference room for academic discussions");
        mathConference.setCapacity(25);
        mathConference.setLocation("Mathematics Building, 1st Floor");
        mathConference.setHourlyRate(new BigDecimal("55.00"));
        mathConference.setAmenities(Arrays.asList("Projector", "Whiteboard", "Air Conditioning", "Quiet Environment"));
        mathConference.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(mathConference));
        
        // 13. Computer Lab A
        Venue compLabA = new Venue();
        compLabA.setName("Computer Lab A");
        compLabA.setDescription("Computer laboratory with 50 workstations for training sessions");
        compLabA.setCapacity(50);
        compLabA.setLocation("CS Building, 1st Floor");
        compLabA.setHourlyRate(new BigDecimal("85.00"));
        compLabA.setAmenities(Arrays.asList("50 Computers", "Projector", "Air Conditioning", "Network Access", "Software Licenses"));
        compLabA.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(compLabA));
        
        // 14. Innovation Center
        Venue innovationCenter = new Venue();
        innovationCenter.setName("Innovation Center");
        innovationCenter.setDescription("Modern innovation center for startup meetings and product launches");
        innovationCenter.setCapacity(200);
        innovationCenter.setLocation("Innovation Building, Ground Floor");
        innovationCenter.setHourlyRate(new BigDecimal("150.00"));
        innovationCenter.setAmenities(Arrays.asList("4K Projector", "Sound System", "Air Conditioning", "Stage", "Video Conferencing", "Coffee Bar"));
        innovationCenter.setImageUrl("https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(innovationCenter));
        
        // 15. Sports Complex Hall
        Venue sportsHall = new Venue();
        sportsHall.setName("Sports Complex Hall");
        sportsHall.setDescription("Multi-purpose hall in sports complex for events and presentations");
        sportsHall.setCapacity(400);
        sportsHall.setLocation("Sports Complex, Ground Floor");
        sportsHall.setHourlyRate(new BigDecimal("120.00"));
        sportsHall.setAmenities(Arrays.asList("Projector", "Sound System", "Air Conditioning", "Large Space", "Parking Available"));
        sportsHall.setImageUrl("https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=200&fit=crop");
        demoVenues.add(venueRepository.save(sportsHall));
        
        return demoVenues;
    }
    
    /**
     * Get venue statistics
     * @return Map containing venue statistics
     */
    public Map<String, Object> getVenueStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVenues", venueRepository.countActiveVenues());
        stats.put("venues", getAllActiveVenues());
        return stats;
    }
    
    /**
     * Reset and recreate all demo venues
     * @return List of created demo venues
     */
    public List<Venue> resetAndCreateDemoVenues() {
        System.out.println("Resetting and recreating all demo venues...");
        
        // Clear all existing venues
        List<Venue> existingVenues = venueRepository.findAll();
        if (!existingVenues.isEmpty()) {
            System.out.println("Deleting " + existingVenues.size() + " existing venues...");
            venueRepository.deleteAll();
        }
        
        // Create new demo venues
        List<Venue> demoVenues = createDemoVenues();
        System.out.println("Successfully created " + demoVenues.size() + " demo venues");
        
        return demoVenues;
    }
}

