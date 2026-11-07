package com.syamalahomeo.hospital.dto;

public record ConsultationView(Long id, String date, String validUntil, String doctorName,
                               String createdBy, boolean valid, long daysLeft) {
}