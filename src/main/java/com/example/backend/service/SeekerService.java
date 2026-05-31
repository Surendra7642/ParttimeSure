package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import com.example.backend.dto.SeekerProfileDto;
import com.example.backend.dto.JobDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class SeekerService {

    private final SeekerProfileRepository seekerProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public SeekerService(SeekerProfileRepository seekerProfileRepository, 
                         JobRepository jobRepository, 
                         ApplicationRepository applicationRepository,
                         UserRepository userRepository) {
        this.seekerProfileRepository = seekerProfileRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
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

    public SeekerProfile getProfile(String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        SeekerProfile profile = seekerProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            throw new RuntimeException("Profile not setup yet");
        }
        return profile;
    }

    public SeekerProfile saveProfile(SeekerProfileDto dto, String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        SeekerProfile profile = seekerProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            profile = new SeekerProfile();
            profile.setUser(user);
        }
        profile.setFullName(dto.getFullName());
        profile.setEducationLevel(dto.getEducationLevel());
        profile.setLanguages(dto.getLanguages());
        profile.setCityLocation(dto.getCityLocation());
        profile.setSkills(dto.getSkills());

        return seekerProfileRepository.save(profile);
    }

    public List<JobDto> getFeed() {
        return jobRepository.findByIsActiveTrue().stream().map(job -> {
            JobDto dto = new JobDto();
            dto.setId(job.getId());
            dto.setRoleTitle(job.getRoleTitle());
            dto.setDescription(job.getDescription());
            dto.setShiftTiming(job.getShiftTiming());
            dto.setSalary(job.getSalary());
            dto.setEducationRequirement(job.getEducationRequirement());
            dto.setOpenSlots(job.getOpenSlots());
            dto.setCreatedAt(job.getCreatedAt());
            if (job.getMerchant() != null) {
                dto.setShopName(job.getMerchant().getShopName());
                dto.setAddressLocation(job.getMerchant().getAddressLocation());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<JobDto> getRecommendations(String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        SeekerProfile profile = seekerProfileRepository.findByUserId(user.getId());
        List<JobDto> allJobs = getFeed();
        
        if (profile == null) {
            return allJobs;
        }

        String seekerSkills = profile.getSkills() != null ? profile.getSkills().toLowerCase() : "";
        String seekerEducation = profile.getEducationLevel() != null ? profile.getEducationLevel().toLowerCase() : "";

        return allJobs.stream().sorted((j1, j2) -> {
            int score1 = 0;
            int score2 = 0;

            if (j1.getEducationRequirement() != null && seekerEducation.contains(j1.getEducationRequirement().toLowerCase())) {
                score1 += 30;
            }
            if (j2.getEducationRequirement() != null && seekerEducation.contains(j2.getEducationRequirement().toLowerCase())) {
                score2 += 30;
            }

            if (j1.getRoleTitle() != null && seekerSkills.contains(j1.getRoleTitle().toLowerCase())) {
                score1 += 50;
            }
            if (j2.getRoleTitle() != null && seekerSkills.contains(j2.getRoleTitle().toLowerCase())) {
                score2 += 50;
            }

            return Integer.compare(score2, score1);
        }).collect(Collectors.toList());
    }

    public Application applyJob(Long jobId, String userPhone) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        User user = getAuthenticatedUser(userPhone);
        SeekerProfile seekerProfile = seekerProfileRepository.findByUserId(user.getId());
        if (seekerProfile == null) {
            throw new RuntimeException("Seeker profile not found. Please create a profile first.");
        }

        Application application = new Application();
        application.setJob(job);
        application.setSeeker(seekerProfile);
        application.setStatus(Application.AppStatus.APPLIED);
        return applicationRepository.save(application);
    }

    public List<Application> getApplications(String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        SeekerProfile seekerProfile = seekerProfileRepository.findByUserId(user.getId());
        if (seekerProfile == null) {
            return List.of();
        }
        return applicationRepository.findBySeekerId(seekerProfile.getId());
    }

    public List<Map<String, String>> getNotifications(String userPhone) {
        User user = getAuthenticatedUser(userPhone);
        SeekerProfile seekerProfile = seekerProfileRepository.findByUserId(user.getId());
        List<Map<String, String>> notifications = new ArrayList<>();
        
        if (seekerProfile != null) {
            List<Application> apps = applicationRepository.findBySeekerId(seekerProfile.getId());
            for (Application app : apps) {
                if (app.getStatus() == Application.AppStatus.HIRED) {
                    notifications.add(Map.of(
                        "title", "🎉 Offer Letter Received!",
                        "message", "Congratulations! " + app.getJob().getMerchant().getShopName() + " has hired you for the " + app.getJob().getRoleTitle() + " role.",
                        "time", "Just now"
                    ));
                }
            }
        }
        
        notifications.add(Map.of(
            "title", "Welcome to Part Time Sure",
            "message", "Explore verified premium roles and get hired in hours.",
            "time", "1 day ago"
        ));
        
        return notifications;
    }

    @org.springframework.scheduling.annotation.Async
    public java.util.concurrent.CompletableFuture<Void> reportJob(Long jobId) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Job ID " + jobId + " reported to trust and safety asynchronously.");
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }
}
