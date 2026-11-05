package com.syamalahomeo.hospital.dto;

import java.time.LocalDate;

public record AdminAppointmentView(
        Long id,
        String patientName,
        String phone,
        String email,
        String doctorName,
        String serviceName,
        LocalDate preferredDate,
        String preferredTime,
        String message,
        String status,
        String createdAt) {
}