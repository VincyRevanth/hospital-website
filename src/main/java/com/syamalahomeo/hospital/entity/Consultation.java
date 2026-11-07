package com.syamalahomeo.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "consultation")
public class Consultation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    protected Consultation() { }

    public Consultation(Patient patient, Doctor doctor, LocalDate consultationDate,
                        int validityDays, String createdBy) {
        this.patient = patient;
        this.doctor = doctor;
        this.consultationDate = consultationDate;
        this.validUntil = consultationDate.plusDays(validityDays);
        this.createdBy = createdBy;
    }

    public boolean isValidOn(LocalDate day) {
        return !day.isBefore(consultationDate) && !day.isAfter(validUntil);
    }

    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getConsultationDate() { return consultationDate; }
    public LocalDate getValidUntil() { return validUntil; }
    public String getCreatedBy() { return createdBy; }
}