package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}