package com.example.backend.dto;

import java.time.LocalDateTime;

public class JobDto {
    private Long id;
    private String roleTitle;
    private String description;
    private String shiftTiming;
    private String salary;
    private String educationRequirement;
    private int openSlots;
    private String shopName;
    private String addressLocation;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShiftTiming() { return shiftTiming; }
    public void setShiftTiming(String shiftTiming) { this.shiftTiming = shiftTiming; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getEducationRequirement() { return educationRequirement; }
    public void setEducationRequirement(String educationRequirement) { this.educationRequirement = educationRequirement; }

    public int getOpenSlots() { return openSlots; }
    public void setOpenSlots(int openSlots) { this.openSlots = openSlots; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getAddressLocation() { return addressLocation; }
    public void setAddressLocation(String addressLocation) { this.addressLocation = addressLocation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
