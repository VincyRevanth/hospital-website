package com.syamalahomeo.hospital.dto;

public record AdminDoctorView(Long id, String slug, String fullName, String qualification,
                              int displayOrder, boolean active) {
}