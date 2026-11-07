package com.syamalahomeo.hospital.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultationValidityTest {

    private final LocalDate start = LocalDate.of(2026, 1, 1);
    private final Consultation consultation = new Consultation(null, null, start, 15, "tester");

    @Test
    void validUntilIsDatePlusValidityDays() {
        assertEquals(LocalDate.of(2026, 1, 16), consultation.getValidUntil());
    }

    @Test
    void validOnLastDayButNotAfter() {
        assertTrue(consultation.isValidOn(LocalDate.of(2026, 1, 16)));
        assertFalse(consultation.isValidOn(LocalDate.of(2026, 1, 17)));
    }

    @Test
    void notValidBeforeConsultationDate() {
        assertFalse(consultation.isValidOn(LocalDate.of(2025, 12, 31)));
    }
}