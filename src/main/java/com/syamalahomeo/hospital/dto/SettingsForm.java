package com.syamalahomeo.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SettingsForm {

    @Pattern(regexp = "^$|^[0-9+()\\- ]{7,20}$",
             message = "Phone may contain digits, spaces, + ( ) - only (7 to 20 characters).")
    private String phone;

    @Email(message = "Please enter a valid email address.")
    @Size(max = 150, message = "Email must be 150 characters or fewer.")
    private String email;

    @Size(max = 500, message = "Address must be 500 characters or fewer.")
    private String address;

    @Size(max = 500, message = "Opening hours must be 500 characters or fewer.")
    private String openingHours;

    @Pattern(regexp = "^$|^https://[^\\s<>\"']{4,480}$",
             message = "The map link must start with https://")
    private String mapUrl;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getMapUrl() { return mapUrl; }
    public void setMapUrl(String mapUrl) { this.mapUrl = mapUrl; }
}