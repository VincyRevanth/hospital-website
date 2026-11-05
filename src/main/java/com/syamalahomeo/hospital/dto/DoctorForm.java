package com.syamalahomeo.hospital.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DoctorForm {

    @NotBlank(message = "Please enter the doctor's full name.")
    @Size(max = 150, message = "The name must be 150 characters or fewer.")
    private String fullName;

    @NotBlank(message = "Please enter the qualification.")
    @Size(max = 200, message = "The qualification must be 200 characters or fewer.")
    private String qualification;

    @Size(max = 4000, message = "The profile must be 4000 characters or fewer.")
    private String bio;

    @Size(max = 100, message = "The registration number must be 100 characters or fewer.")
    private String registrationNumber;

    @NotNull(message = "Please enter a display order.")
    @Min(value = 0, message = "Display order must be 0 or more.")
    @Max(value = 1000, message = "Display order must be 1000 or less.")
    private Integer displayOrder = 0;

    private boolean active = true;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}