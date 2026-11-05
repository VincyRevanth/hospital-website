package com.syamalahomeo.hospital.dto;

import java.time.LocalDate;

public record AppointmentResponse(
        Long reference,
        String status,
        String doctorName,
        String serviceName,
        LocalDate preferredDate,
        String preferredTime,
        String notice) {
}