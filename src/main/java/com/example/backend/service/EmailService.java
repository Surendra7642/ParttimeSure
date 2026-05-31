package com.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    public void sendSelectionEmail(String recipientEmail, Map<String, String> details) {
        log.info("--------------------------------------------------");
        log.info("SIMULATING EMAIL SENDING to: {}", recipientEmail);
        log.info("SUBJECT: Congratulations! You've been selected for a job!");
        log.info("BODY:");
        log.info("Dear Candidate,");
        log.info("You have been selected for the role of '{}' at '{}'.", details.get("roleTitle"), details.get("shopName"));
        log.info("SHP DETAILS:");
        log.info("Location: {}", details.get("shopLocation"));
        log.info("Address: {}", details.get("shopAddress"));
        log.info("Mobile: {}", details.get("merchantMobile"));
        log.info("REPORTING DETAILS:");
        log.info("Time: {}", details.get("reportingTime"));
        log.info("--------------------------------------------------");

        // In a real scenario, use JavaMailSender or SendGrid
    }

    public void sendSelectionSms(String phoneNumber, Map<String, String> details) {
        String message = String.format(
            "Congrats! Selected for %s at %s. Address: %s. Report at %s. Call: %s",
            details.get("roleTitle"), 
            details.get("shopName"),
            details.get("shopLocation"),
            details.get("reportingTime"),
            details.get("merchantMobile")
        );
        log.info("SIMULATING NOTIFICATION SMS to: {}", phoneNumber);
        log.info("CONTENT: {}", message);
    }
}
