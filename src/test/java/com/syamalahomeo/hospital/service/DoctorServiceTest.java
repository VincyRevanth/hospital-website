package com.syamalahomeo.hospital.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoctorServiceTest {

    private final DoctorService service = new DoctorService();

    @Test
    void returnsBothDoctors() {
        assertEquals(2, service.findAll().size());
    }

    @Test
    void findsDoctorBySlug() {
        assertTrue(service.findBySlug("abburi-aruna").isPresent());
    }

    @Test
    void unknownSlugReturnsEmpty() {
        assertTrue(service.findBySlug("nobody").isEmpty());
    }
}