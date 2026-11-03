package com.syamalahomeo.hospital.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DoctorServiceTest {

    @Autowired
    private DoctorService service;

    @Test
    void returnsBothSeededDoctors() {
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