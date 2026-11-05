package com.syamalahomeo.hospital.entity;

import com.syamalahomeo.hospital.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentStatusTransitionTest {

    private Appointment newAppointment() {
        ServiceOffering service = new ServiceOffering("Consultation", "test");
        return new Appointment("Test Patient", "9876543210", null, null, service,
                LocalDate.of(2030, 1, 1), LocalTime.of(10, 0), null);
    }

    @Test
    void newAppointmentIsPending() {
        assertEquals(AppointmentStatus.PENDING, newAppointment().getStatus());
    }

    @Test
    void pendingCanBeConfirmed() {
        Appointment a = newAppointment();
        a.confirm();
        assertEquals(AppointmentStatus.CONFIRMED, a.getStatus());
    }

    @Test
    void confirmedCannotBeRejected() {
        Appointment a = newAppointment();
        a.confirm();
        assertThrows(InvalidStatusTransitionException.class, a::reject);
    }

    @Test
    void rejectedCannotBeConfirmed() {
        Appointment a = newAppointment();
        a.reject();
        assertThrows(InvalidStatusTransitionException.class, a::confirm);
    }

    @Test
    void confirmedCanBeCancelled() {
        Appointment a = newAppointment();
        a.confirm();
        a.cancel();
        assertEquals(AppointmentStatus.CANCELLED, a.getStatus());
    }
}