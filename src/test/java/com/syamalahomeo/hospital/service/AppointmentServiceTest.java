package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.AppointmentRequest;
import com.syamalahomeo.hospital.dto.AppointmentResponse;
import com.syamalahomeo.hospital.entity.Appointment;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.repository.AppointmentRepository;
import com.syamalahomeo.hospital.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService service;
    @Autowired
    private AppointmentRepository appointments;
    @Autowired
    private DoctorRepository doctors;

    private AppointmentRequest validRequest() {
        AppointmentRequest r = new AppointmentRequest();
        r.setPatientName("Test Patient");
        r.setPhone("9876543210");
        r.setDoctorId(doctors.findBySlugAndActiveTrue("abburi-aruna").orElseThrow().getId());
        r.setPreferredDate(service.minDate().plusDays(1));
        r.setPreferredTime("10:00");
        return r;
    }

    @Test
    void validRequestIsSavedAsPending() {
        AppointmentResponse response = service.submit(validRequest());
        assertEquals("PENDING", response.status());
        assertEquals(1, appointments.count());
    }

    @Test
    void pastDateIsRejected() {
        AppointmentRequest r = validRequest();
        r.setPreferredDate(service.minDate().minusDays(1));
        assertThrows(BusinessRuleException.class, () -> service.submit(r));
    }

    @Test
    void duplicateRequestIsRejected() {
        service.submit(validRequest());
        assertThrows(BusinessRuleException.class, () -> service.submit(validRequest()));
    }

    @Test
    void doctorOrServiceIsRequired() {
        AppointmentRequest r = validRequest();
        r.setDoctorId(null);
        assertThrows(BusinessRuleException.class, () -> service.submit(r));
    }

    @Test
    void confirmedSlotBlocksOtherPatients() {
        service.submit(validRequest());
        Appointment first = appointments.findAll().get(0);
        first.confirm();
        appointments.saveAndFlush(first);

        AppointmentRequest other = validRequest();
        other.setPhone("9123456789");
        assertThrows(BusinessRuleException.class, () -> service.submit(other));
    }
}