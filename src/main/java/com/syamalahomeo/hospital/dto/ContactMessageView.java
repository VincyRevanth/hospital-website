package com.syamalahomeo.hospital.dto;

public record ContactMessageView(Long id, String name, String phone, String email,
                                 String message, String receivedAt) {
}