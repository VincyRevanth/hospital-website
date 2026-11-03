package com.syamalahomeo.hospital.dto;

public class DoctorView {

    private final String slug;
    private final String name;
    private final String qualification;
    private final String bio;

    public DoctorView(String slug, String name, String qualification, String bio) {
        this.slug = slug;
        this.name = name;
        this.qualification = qualification;
        this.bio = bio;
    }

    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getQualification() { return qualification; }
    public String getBio() { return bio; }
}