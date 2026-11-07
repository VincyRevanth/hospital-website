package com.syamalahomeo.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "patient")
public class Patient extends BaseEntity {

    @Column(name = "patient_code", length = 20, unique = true)
    private String patientCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "age_years", nullable = false)
    private int ageYears;

    @Column(name = "age_recorded_on", nullable = false)
    private LocalDate ageRecordedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 300)
    private String address;

    @Column(name = "guardian_name", length = 100)
    private String guardianName;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    protected Patient() { }

    public Patient(String fullName, int ageYears, LocalDate ageRecordedOn, Gender gender,
                   String phone, String address, String guardianName, String createdBy) {
        this.fullName = fullName;
        this.ageYears = ageYears;
        this.ageRecordedOn = ageRecordedOn;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.guardianName = guardianName;
        this.createdBy = createdBy;
    }

    /** Age on a given day: the age recorded at registration plus whole years since. */
    public int ageOn(LocalDate day) {
        long years = ChronoUnit.YEARS.between(ageRecordedOn, day);
        return ageYears + (int) Math.max(0, years);
    }

    public void updateAge(int ageYears, LocalDate today) {
        this.ageYears = ageYears;
        this.ageRecordedOn = today;
    }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getCreatedBy() { return createdBy; }
}