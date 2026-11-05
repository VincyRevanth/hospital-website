package com.syamalahomeo.hospital.dto;

import java.time.Instant;
import java.util.Map;

public record ApiError(int status, String message, Map<String, String> fieldErrors, Instant timestamp) {

    public ApiError(int status, String message, Map<String, String> fieldErrors) {
        this(status, message, fieldErrors, Instant.now());
    }
}