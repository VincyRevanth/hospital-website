package com.syamalahomeo.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AppointmentRequest {

    @NotBlank(message = "Please enter the patient's name.")
    @Size(max = 100, message = "The name must be 100 characters or fewer.")
    private String patientName;

    @NotBlank(message = "Please enter a phone number.")
    @Pattern(regexp = "^(\\+91[\\- ]?|0)?[6-9]\\d{9}$",
             message = "Please enter a valid 10-digit Indian mobile number.")
    private String phone;

    @Email(message = "Please enter a valid email address.")
    @Size(max = 150, message = "The email must be 150 characters or fewer.")
    private String email;

    private Long doctorId;

    private Long serviceId;

    @NotNull(message = "Please choose a preferred date.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate preferredDate;

    @NotBlank(message = "Please choose a preferred time.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Please choose a valid time.")
    private String preferredTime;

    @Size(max = 500, message = "The message must be 500 characters or fewer.")
    private String message;

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }
    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}