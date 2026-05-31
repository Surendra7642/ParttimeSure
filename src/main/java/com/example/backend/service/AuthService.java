package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public AuthService(UserRepository userRepository, SmsService smsService) {
        this.userRepository = userRepository;
        this.smsService = smsService;
    }

    public String sendOtp(String phoneNumber, String flowType) {
        log.info("Generating OTP for phone: {} (Flow: {})", phoneNumber, flowType);
        
        // Clean phone number for database lookup (extract digits only)
        String cleanedPhone = phoneNumber.replaceAll("\\D", "");
        // If it starts with 91 and length is 12 (India), strip the 91
        if (cleanedPhone.length() == 12 && cleanedPhone.startsWith("91")) {
            cleanedPhone = cleanedPhone.substring(2);
        } else if (cleanedPhone.length() == 11 && cleanedPhone.startsWith("1")) {
            cleanedPhone = cleanedPhone.substring(1);
        }

        // If flow is login, check if user exists in the database
        if ("login".equalsIgnoreCase(flowType)) {
            Optional<User> user = userRepository.findByPhoneNumber(cleanedPhone);
            if (user.isEmpty()) {
                log.warn("Login attempt for unregistered phone: {}", cleanedPhone);
                throw new RuntimeException("Declined: Mobile number is not registered. Please sign up first.");
            }
        }

        // Generate a random 4-digit OTP code (between 1000 and 9999)
        String otp = String.valueOf(1000 + random.nextInt(9000));
        
        // Store in-memory
        otpStorage.put(cleanedPhone, otp);
        
        // Trigger "real" SMS
        smsService.sendOtp(phoneNumber, otp);
        log.info("OTP sent successfully to phone: {}", phoneNumber);

        return otp;
    }

    public AuthResponse verifyOtp(AuthRequest request) {
        String cleanedPhone = request.getPhoneNumber().replaceAll("\\D", "");
        if (cleanedPhone.length() == 12 && cleanedPhone.startsWith("91")) {
            cleanedPhone = cleanedPhone.substring(2);
        } else if (cleanedPhone.length() == 11 && cleanedPhone.startsWith("1")) {
            cleanedPhone = cleanedPhone.substring(1);
        }

        String storedOtp = otpStorage.get(cleanedPhone);
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            log.error("Invalid OTP verification attempt for phone: {}", cleanedPhone);
            throw new RuntimeException("Invalid OTP");
        }

        log.info("OTP verified for phone: {}", cleanedPhone);
        // Remove OTP from storage after successful verification
        otpStorage.remove(cleanedPhone);

        // Find or create user
        Optional<User> existingUser = userRepository.findByPhoneNumber(cleanedPhone);
        User user;

        if (existingUser.isEmpty()) {
            if ("login".equalsIgnoreCase(request.getFlowType())) {
                log.warn("Unregistered user verified OTP but flow was login: {}", cleanedPhone);
                throw new RuntimeException("Declined: Mobile number is not registered. Please sign up first.");
            }
            log.info("Creating new user for phone: {}", cleanedPhone);
            user = new User();
            user.setPhoneNumber(cleanedPhone);
            
            // Set role based on request or default to SEEKER
            User.Role userRole = User.Role.SEEKER;
            if (request.getRole() != null) {
                try {
                    String roleStr = request.getRole().toUpperCase();
                    if ("WORK".equals(roleStr)) {
                        userRole = User.Role.SEEKER;
                    } else if ("SHOP".equals(roleStr)) {
                        userRole = User.Role.MERCHANT;
                    } else {
                        userRole = User.Role.valueOf(roleStr);
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role provided: {}. Defaulting to SEEKER.", request.getRole());
                }
            }
            user.setRole(userRole);
            user = userRepository.save(user);
            log.info("User created with ID: {} and Role: {}", user.getId(), user.getRole());
        } else {
            user = existingUser.get();
            log.info("Existing user found with ID: {}", user.getId());
        }

        return new AuthResponse("JWT_MOCK_TOKEN_" + user.getPhoneNumber(), user.getRole().name(), "VERIFIED");
    }
}
