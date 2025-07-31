package com.smartslot.config;

import com.smartslot.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private VenueService venueService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize venues with demo data
        System.out.println("Initializing venue data...");
        venueService.getVenuesWithDemoData();
        System.out.println("Venue data initialization completed!");
    }
} 