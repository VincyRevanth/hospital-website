package com.syamalahomeo.hospital.dto;

public class DoctorView {

    private final Long id;
    private final String slug;
    private final String name;
    private final String qualification;
    private final String bio;
    private final String registrationNumber;

    public DoctorView(Long id, String slug, String name, String qualification,
                      String bio, String registrationNumber) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.qualification = qualification;
        this.bio = bio;
        this.registrationNumber = registrationNumber;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getQualification() { return qualification; }
    public String getBio() { return bio; }
    public String getRegistrationNumber() { return registrationNumber; }
}