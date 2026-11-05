package com.syamalahomeo.hospital.entity;

import com.syamalahomeo.hospital.exception.InvalidStatusTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

@Entity
@Table(name = "appointment")
public class Appointment extends BaseEntity {

    @Column(name = "patient_name", nullable = false, length = 100)
    private String patientName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceOffering service;

    @Column(name = "preferred_date", nullable = false)
    private LocalDate preferredDate;

    @Column(name = "preferred_time", nullable = false)
    private LocalTime preferredTime;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    /** Set only while CONFIRMED; the UNIQUE constraint blocks double-booking. */
    @Column(name = "confirmed_slot_key", length = 60)
    private String confirmedSlotKey;

    @Version
    private long version;

    protected Appointment() { }

    public Appointment(String patientName, String phone, String email, Doctor doctor,
                       ServiceOffering service, LocalDate preferredDate,
                       LocalTime preferredTime, String message) {
        this.patientName = patientName;
        this.phone = phone;
        this.email = email;
        this.doctor = doctor;
        this.service = service;
        this.preferredDate = preferredDate;
        this.preferredTime = preferredTime;
        this.message = message;
    }

    public static String slotKey(Doctor doctor, LocalDate date, LocalTime time) {
        return doctor.getId() + ":" + date + ":" + time;
    }

    public void confirm() {
        requireStatus("confirm", AppointmentStatus.PENDING);
        this.status = AppointmentStatus.CONFIRMED;
        if (doctor != null) {
            this.confirmedSlotKey = slotKey(doctor, preferredDate, preferredTime);
        }
    }

    public void reject() {
        requireStatus("reject", AppointmentStatus.PENDING);
        this.status = AppointmentStatus.REJECTED;
    }

    public void cancel() {
        requireStatus("cancel", AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
        this.status = AppointmentStatus.CANCELLED;
        this.confirmedSlotKey = null;
    }

    private void requireStatus(String action, AppointmentStatus... allowed) {
        if (!Arrays.asList(allowed).contains(status)) {
            throw new InvalidStatusTransitionException(
                    "Cannot " + action + " an appointment that is " + status);
        }
    }

    public String getPatientName() { return patientName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Doctor getDoctor() { return doctor; }
    public ServiceOffering getService() { return service; }
    public LocalDate getPreferredDate() { return preferredDate; }
    public LocalTime getPreferredTime() { return preferredTime; }
    public String getMessage() { return message; }
    public AppointmentStatus getStatus() { return status; }
    public long getVersion() { return version; }
}