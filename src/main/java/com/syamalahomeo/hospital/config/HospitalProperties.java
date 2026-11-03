package com.syamalahomeo.hospital.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hospital")
public class HospitalProperties {

    private String name;
    private String phoneDisplay;
    private String phoneLink;
    private String email;
    private String address;
    private String openingHours;
    private String establishedYear;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneDisplay() { return phoneDisplay; }
    public void setPhoneDisplay(String phoneDisplay) { this.phoneDisplay = phoneDisplay; }
    public String getPhoneLink() { return phoneLink; }
    public void setPhoneLink(String phoneLink) { this.phoneLink = phoneLink; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getEstablishedYear() { return establishedYear; }
    public void setEstablishedYear(String establishedYear) { this.establishedYear = establishedYear; }
}