package com.example.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.backend.service.MerchantService;
import com.example.backend.dto.MerchantProfileDto;
import com.example.backend.dto.JobDto;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@CrossOrigin(origins = "*")
@Slf4j
public class MerchantController {
    
    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyShop(@RequestBody MerchantProfileDto dto,
                                        @RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to verify shop for merchant phone: {}", userPhone);
        try {
            return ResponseEntity.ok(merchantService.verifyShop(dto, userPhone));
        } catch (Exception e) {
            log.error("Failed to verify shop for merchant {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/jobs")
    public ResponseEntity<?> postJob(@RequestBody JobDto dto,
                                     @RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to post job: {} for merchant phone: {}", dto.getRoleTitle(), userPhone);
        try {
            return ResponseEntity.ok(merchantService.postJob(dto, userPhone));
        } catch (Exception e) {
            log.error("Failed to post job for merchant {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> fetchDashboard(@RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to fetch dashboard for merchant phone: {}", userPhone);
        try {
            return ResponseEntity.ok(merchantService.fetchDashboard(userPhone));
        } catch (Exception e) {
            log.error("Failed to fetch dashboard for merchant {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/applicants")
    public ResponseEntity<?> fetchApplicants(@RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        log.info("Request to fetch applicants for merchant phone: {}", userPhone);
        try {
            return ResponseEntity.ok(merchantService.fetchApplicants(userPhone));
        } catch (Exception e) {
            log.error("Failed to fetch applicants for merchant {}: {}", userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/applications/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> payload,
                                                     @RequestHeader(value = "X-User-Phone", required = false) String userPhone) {
        String status = payload.get("status");
        log.info("Request to update application ID: {} to status: {} by merchant phone: {}", id, status, userPhone);
        try {
            return ResponseEntity.ok(merchantService.updateApplicationStatus(id, status, userPhone));
        } catch (Exception e) {
            log.error("Failed to update application ID: {} by merchant {}: {}", id, userPhone, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
