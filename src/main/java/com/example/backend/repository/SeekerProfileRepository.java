package com.example.backend.repository;

import com.example.backend.model.SeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeekerProfileRepository extends JpaRepository<SeekerProfile, Long> {
    SeekerProfile findByUserId(Long userId);
}
