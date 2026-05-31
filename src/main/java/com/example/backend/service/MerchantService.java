package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import com.example.backend.dto.MerchantProfileDto;
import com.example.backend.dto.JobDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@Slf4j
public class MerchantService {

    private final MerchantProfileRepository merchantProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    public MerchantService(MerchantProfileRepository merchantProfileRepository, 
                         JobRepository jobRepository, 
                         ApplicationRepository applicationRepository,
                         UserRepository userRepository,
                         SeekerProfileRepository seekerProfileRepository,
                         EmailService emailService,
                         SmsService smsService) {
        this.merchantProfileRepository = merchantProfileRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.seekerProfileRepository = seekerProfileRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    private User getAuthenticatedUser(String userPhone) {
        if (userPhone == null || userPhone.trim().isEmpty()) {
            throw new RuntimeException("Authentication header X-User-Phone is missing");
        }
        String cleaned = userPhone.replaceAll("\\D", "");
        if (cleaned.length() == 12 && cleaned.startsWith("91")) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.length() == 11 && cleaned.startsWith("1")) {
            cleaned = cleaned.substring(1);
        }
        final String searchPhone = cleaned;
        return userRepository.findByPhoneNumber(searchPhone)
                .orElseThrow(() -> new RuntimeException("User not found for phone: " + searchPhone));
    }

    public MerchantProfile verifyShop(MerchantProfileDto dto, String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        MerchantProfile profile = merchantProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            profile = new MerchantProfile();
            profile.setUser(user);
            profile.setTrustScore(90); // Default trust score
        }
        profile.setShopName(dto.getShopName());
        profile.setAddressLocation(dto.getAddressLocation());
        profile.setVerified(true); 

        return merchantProfileRepository.save(profile);
    }

    public Job postJob(JobDto dto, String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        MerchantProfile profile = merchantProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            throw new RuntimeException("Merchant profile not found. Please verify shop first.");
        }

        Job job = new Job();
        job.setMerchant(profile);
        job.setRoleTitle(dto.getRoleTitle());
        job.setDescription(dto.getDescription());
        job.setShiftTiming(dto.getShiftTiming());
        job.setSalary(dto.getSalary());
        job.setEducationRequirement(dto.getEducationRequirement());
        job.setOpenSlots(dto.getOpenSlots());
        job.setActive(true);

        return jobRepository.save(job);
    }

    public Map<String, Object> fetchDashboard(String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        MerchantProfile profile = merchantProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            return Map.of(
                "activeJobs", 0,
                "totalApplicants", 0,
                "hiredCount", 0,
                "trustScore", 90,
                "listings", List.of()
            );
        }

        List<Job> activeJobs = jobRepository.findByMerchantId(profile.getId());
        int totalApplicants = 0;
        int hiredCount = 0;
        List<Map<String, Object>> listings = new ArrayList<>();
        
        for (Job job : activeJobs) {
            List<Application> apps = applicationRepository.findByJobId(job.getId());
            totalApplicants += apps.size();
            hiredCount += apps.stream().filter(a -> a.getStatus() == Application.AppStatus.HIRED).count();
            
            listings.add(Map.of(
                "id", job.getId(),
                "roleTitle", job.getRoleTitle(),
                "applicantCount", apps.size(),
                "openSlots", job.getOpenSlots()
            ));
        }

        return Map.of(
            "activeJobs", activeJobs.size(),
            "totalApplicants", totalApplicants,
            "hiredCount", hiredCount,
            "trustScore", profile.getTrustScore(),
            "listings", listings
        );
    }

    public List<Application> fetchApplicants(String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        MerchantProfile profile = merchantProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            return List.of();
        }

        List<Job> activeJobs = jobRepository.findByMerchantId(profile.getId());
        List<Application> allApplicants = new ArrayList<>();
        
        for (Job job : activeJobs) {
            List<Application> apps = applicationRepository.findByJobId(job.getId());
            allApplicants.addAll(apps);
        }

        return allApplicants;
    }

    public Application updateApplicationStatus(Long applicationId, String status, String userPhone) {
        log.info("Updating application ID {} to status {} by merchant {}", applicationId, status, userPhone);
        User user = getAuthenticatedUser(userPhone);
        MerchantProfile profile = merchantProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            log.error("Merchant profile not found for user: {}", userPhone);
            throw new RuntimeException("Merchant profile not found");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> {
                    log.error("Application ID {} not found", applicationId);
                    return new RuntimeException("Application not found");
                });

        if (!application.getJob().getMerchant().getId().equals(profile.getId())) {
            log.warn("Unauthorized status update attempt for application {} by merchant {}", applicationId, userPhone);
            throw new RuntimeException("Unauthorized application update");
        }

        Application.AppStatus newStatus;
        try {
            newStatus = Application.AppStatus.valueOf(status.toUpperCase());
            application.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", status);
            throw new RuntimeException("Invalid status: " + status);
        }

        Application savedApp = applicationRepository.save(application);
        log.info("Application status updated to {}", newStatus);

        // If selected or hired, send notifications
        if (newStatus == Application.AppStatus.SELECTED || newStatus == Application.AppStatus.HIRED) {
            log.info("Candidate selected. Preparing notifications...");
            sendSelectionNotifications(savedApp, profile);
        }

        return savedApp;
    }

    private void sendSelectionNotifications(Application app, MerchantProfile merchant) {
        SeekerProfile seekerProfile = app.getSeeker();
        User seekerUser = seekerProfile.getUser();
        
        Map<String, String> details = Map.of(
            "roleTitle", app.getJob().getRoleTitle(),
            "shopName", merchant.getShopName(),
            "shopLocation", merchant.getAddressLocation(),
            "shopAddress", merchant.getAddressLocation(), // For now same as location
            "merchantMobile", merchant.getContactNumber() != null ? merchant.getContactNumber() : merchant.getUser().getPhoneNumber(),
            "reportingTime", app.getJob().getShiftTiming() // Use shift timing as reporting time fallback
        );

        // Send Email if available
        if (seekerProfile != null && seekerProfile.getEmail() != null) {
            emailService.sendSelectionEmail(seekerProfile.getEmail(), details);
        }

        // Send SMS to seeker's phone
        emailService.sendSelectionSms(seekerUser.getPhoneNumber(), details);
        log.info("Notifications sent to candidate: {}", seekerUser.getPhoneNumber());
    }
}
