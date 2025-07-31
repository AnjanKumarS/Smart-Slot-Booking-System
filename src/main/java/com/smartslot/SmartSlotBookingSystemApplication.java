package com.smartslot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.smartslot.model")
@EnableJpaRepositories("com.smartslot.repository")
@EnableScheduling
public class SmartSlotBookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSlotBookingSystemApplication.class, args);
    }

}

