package com.example.backend.repository;

import com.example.backend.model.MerchantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, Long> {
    MerchantProfile findByUserId(Long userId);
}
