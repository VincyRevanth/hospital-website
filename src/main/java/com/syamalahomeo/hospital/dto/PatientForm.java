package com.syamalahomeo.hospital.dto;

import com.syamalahomeo.hospital.entity.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PatientForm {

    @NotBlank(message = "Please enter the patient's full name.")
    @Size(max = 100, message = "The name must be 100 characters or fewer.")
    private String fullName;

    @NotNull(message = "Please enter the age in years.")
    @Min(value = 0, message = "Age cannot be negative.")
    @Max(value = 120, message = "Please enter a valid age.")
    private Integer ageYears;

    @NotNull(message = "Please choose a gender.")
    private Gender gender;

    @NotBlank(message = "Please enter a phone number.")
    @Pattern(regexp = "^(\\+91[\\- ]?|0)?[6-9]\\d{9}$",
             message = "Please enter a valid 10-digit Indian mobile number.")
    private String phone;

    @Size(max = 300, message = "The address must be 300 characters or fewer.")
    private String address;

    @Size(max = 100, message = "The guardian name must be 100 characters or fewer.")
    private String guardianName;

    private Long doctorId;

    private boolean createAnyway;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Integer getAgeYears() { return ageYears; }
    public void setAgeYears(Integer ageYears) { this.ageYears = ageYears; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public boolean isCreateAnyway() { return createAnyway; }
    public void setCreateAnyway(boolean createAnyway) { this.createAnyway = createAnyway; }
}