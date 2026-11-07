package com.syamalahomeo.hospital.dto;

public record StaffView(Long id, String username, boolean enabled, boolean mustChange, String createdOn) {
}