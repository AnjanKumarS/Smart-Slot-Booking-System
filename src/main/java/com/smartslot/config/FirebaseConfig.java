package com.smartslot.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.database-url:}")
    private String databaseUrl;

    @Value("${firebase.demo-mode:false}")
    private boolean demoMode;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Try to load service account from classpath
                try {
                    InputStream serviceAccount = 
                        new ClassPathResource("firebase-service-account.json").getInputStream();

                    FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount));

                    // Set database URL if provided
                    if (databaseUrl != null && !databaseUrl.isEmpty()) {
                        optionsBuilder.setDatabaseUrl(databaseUrl);
                    }

                    FirebaseOptions options = optionsBuilder.build();
                    FirebaseApp.initializeApp(options);

                    logger.info("✅ Firebase successfully initialized");
                    System.out.println("✅ Firebase successfully initialized");
                } catch (IOException e) {
                    if (demoMode) {
                        logger.warn("🔥 Firebase initialization failed - Demo mode enabled, continuing without Firebase");
                        System.out.println("🔥 Firebase initialization failed - Demo mode enabled, continuing without Firebase");
                    } else {
                        logger.error("❌ Failed to initialize Firebase: " + e.getMessage(), e);
                        System.err.println("❌ Failed to initialize Firebase: " + e.getMessage());
                        throw new RuntimeException("Failed to initialize Firebase", e);
                    }
                }
            } else {
                logger.info("Firebase already initialized");
            }
        } catch (Exception e) {
            if (demoMode) {
                logger.warn("🔥 Firebase initialization failed - Demo mode enabled, continuing without Firebase: " + e.getMessage());
                System.out.println("🔥 Firebase initialization failed - Demo mode enabled, continuing without Firebase: " + e.getMessage());
            } else {
                logger.error("❌ Unexpected error initializing Firebase: " + e.getMessage(), e);
                System.err.println("❌ Unexpected error initializing Firebase: " + e.getMessage());
                throw new RuntimeException("Failed to initialize Firebase", e);
            }
        }
    }
}

