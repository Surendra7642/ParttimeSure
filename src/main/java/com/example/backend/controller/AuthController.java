package com.example.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.service.AuthService;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        String phone = payload.get("phoneNumber");
        log.info("Request to send OTP for phone: {}", phone);
        try {
            String flowType = payload.get("flowType");
            String otp = authService.sendOtp(phone, flowType);
            log.info("Successfully generated OTP for phone: {}", phone);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "otp", otp));
        } catch (RuntimeException e) {
            log.error("Failed to send OTP for phone: {}. Error: {}", phone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody AuthRequest request) {
        log.info("Request to verify OTP for phone: {}", request.getPhoneNumber());
        try {
            AuthResponse response = authService.verifyOtp(request);
            log.info("Successfully verified OTP for phone: {}", request.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to verify OTP for phone: {}. Error: {}", request.getPhoneNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
