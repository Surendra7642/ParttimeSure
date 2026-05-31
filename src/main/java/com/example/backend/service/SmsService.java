package com.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    public void sendSms(String phoneNumber, String message) {
        log.info("--------------------------------------------------");
        log.info("SIMULATING SMS SENDING to: {}", phoneNumber);
        log.info("MESSAGE CONTENT: {}", message);
        log.info("--------------------------------------------------");
        
        // In a real scenario, you'd integrate with Twilio, AWS SNS, etc.
        // Example: twilioClient.messages.create(...)
    }

    public void sendOtp(String phoneNumber, String otp) {
        String message = "Your Stick-On verification code is: " + otp + ". Valid for 10 minutes.";
        sendSms(phoneNumber, message);
    }
}
