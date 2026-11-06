package com.syamalahomeo.hospital.dto;

public record DoctorResponse(Long id, String slug, String name, String qualification,
                             String bio, String registrationNumber) {
}