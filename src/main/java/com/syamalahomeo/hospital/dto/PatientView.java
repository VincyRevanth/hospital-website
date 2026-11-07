package com.syamalahomeo.hospital.dto;

public record PatientView(Long id, String code, String fullName, int age, String gender,
                          String phone, String address, String guardianName, String registeredOn) {
}