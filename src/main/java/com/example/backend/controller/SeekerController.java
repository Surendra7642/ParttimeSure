package com.example.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.backend.service.SeekerService;
import com.example.backend.dto.SeekerProfileDto;
import java.util.Map;

@RestController
@RequestMapping("/api/seeker")
@CrossOrigin(origins = "*")
@Slf4j
public class SeekerController {
    
    private final SeekerService seekerService;

    public SeekerController(SeekerService seekerService) {
        this.seekerService = seekerService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to get profile for user phone: {}", userPhone);
        try {
            return ResponseEntity.ok(seekerService.getProfile(userPhone));
        } catch (Exception e) {
            log.error("Failed to get profile for {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> saveProfile(@RequestBody SeekerProfileDto dto,
                                         @RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to save profile for user phone: {}", userPhone);
        try {
            return ResponseEntity.ok(seekerService.saveProfile(dto, userPhone));
        } catch (Exception e) {
            log.error("Failed to save profile for {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/jobs/feed")
    public ResponseEntity<?> getFeed() {
        log.info("Request to fetch job feed");
        return ResponseEntity.ok(seekerService.getFeed());
    }

    @GetMapping("/jobs/recommendations")
    public ResponseEntity<?> getRecommendations(@RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to fetch recommendations for user phone: {}", userPhone);
        try {
            return ResponseEntity.ok(seekerService.getRecommendations(userPhone));
        } catch (Exception e) {
            log.error("Failed to fetch recommendations for {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/jobs/{id}/apply")
    public ResponseEntity<?> applyJob(@PathVariable Long id,
                                      @RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to apply for job ID: {} by user phone: {}", id, userPhone);
        try {
            return ResponseEntity.ok(seekerService.applyJob(id, userPhone));
        } catch (Exception e) {
            log.error("Failed to apply for job ID: {} by {}: {}", id, userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<?> getApplications(@RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to fetch applications for user phone: {}", userPhone);
        try {
            return ResponseEntity.ok(seekerService.getApplications(userPhone));
        } catch (Exception e) {
            log.error("Failed to fetch applications for {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to fetch notifications for user phone: {}", userPhone);
        try {
            return ResponseEntity.ok(seekerService.getNotifications(userPhone));
        } catch (Exception e) {
            log.error("Failed to fetch notifications for {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/jobs/{id}/report")
    public java.util.concurrent.CompletableFuture<ResponseEntity<?>> reportJob(@PathVariable Long id) {
        log.info("Request to report job ID: {}", id);
        return seekerService.reportJob(id).thenApply(result -> {
            log.info("Successfully reported job ID: {} asynchronously", id);
            return ResponseEntity.ok(Map.of("message", "Job has been flagged to trust safety team asynchronously."));
        });
    }
}
