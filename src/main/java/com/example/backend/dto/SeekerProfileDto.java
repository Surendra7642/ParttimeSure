package com.example.backend.dto;

public class SeekerProfileDto {
    private String fullName;
    private String educationLevel;
    private String languages;
    private String cityLocation;
    private String skills;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public String getCityLocation() { return cityLocation; }
    public void setCityLocation(String cityLocation) { this.cityLocation = cityLocation; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}
