package com.syamalahomeo.hospital.dto;

public class HospitalInfo {

    private final String name;
    private final String phoneDisplay;
    private final String phoneLink;
    private final String email;
    private final String address;
    private final String openingHours;
    private final String establishedYear;
    private final String mapUrl;

    public HospitalInfo(String name, String phoneDisplay, String phoneLink, String email,
                        String address, String openingHours, String establishedYear, String mapUrl) {
        this.name = name;
        this.phoneDisplay = phoneDisplay;
        this.phoneLink = phoneLink;
        this.email = email;
        this.address = address;
        this.openingHours = openingHours;
        this.establishedYear = establishedYear;
        this.mapUrl = mapUrl;
    }

    public String getName() { return name; }
    public String getPhoneDisplay() { return phoneDisplay; }
    public String getPhoneLink() { return phoneLink; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getOpeningHours() { return openingHours; }
    public String getEstablishedYear() { return establishedYear; }
    public String getMapUrl() { return mapUrl; }
}