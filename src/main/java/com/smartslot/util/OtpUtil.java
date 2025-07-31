package com.smartslot.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Component
public class OtpUtil {
    
    private static final String NUMBERS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 10;
    
    private final SecureRandom random = new SecureRandom();
    
    /**
     * Generate a random 6-digit OTP
     * @return String OTP
     */
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        return otp.toString();
    }
    
    /**
     * Get OTP expiry time (current time + validity minutes)
     * @return LocalDateTime expiry time
     */
    public LocalDateTime getOtpExpiryTime() {
        return LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
    }
    
    /**
     * Check if OTP is valid (not expired)
     * @param expiryTime OTP expiry time
     * @return boolean true if valid, false if expired
     */
    public boolean isOtpValid(LocalDateTime expiryTime) {
        return LocalDateTime.now().isBefore(expiryTime);
    }
    
    /**
     * Validate OTP code and expiry time
     * @param providedOtp OTP provided by user
     * @param actualOtp Actual OTP stored in database
     * @param expiryTime OTP expiry time
     * @return boolean true if OTP is valid and not expired
     */
    public boolean validateOtp(String providedOtp, String actualOtp, LocalDateTime expiryTime) {
        if (providedOtp == null || actualOtp == null || expiryTime == null) {
            return false;
        }
        
        return providedOtp.equals(actualOtp) && isOtpValid(expiryTime);
    }
    
    /**
     * Get remaining validity time in minutes
     * @param expiryTime OTP expiry time
     * @return long remaining minutes (0 if expired)
     */
    public long getRemainingValidityMinutes(LocalDateTime expiryTime) {
        if (expiryTime == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiryTime)) {
            return 0;
        }
        
        return java.time.Duration.between(now, expiryTime).toMinutes();
    }
}

