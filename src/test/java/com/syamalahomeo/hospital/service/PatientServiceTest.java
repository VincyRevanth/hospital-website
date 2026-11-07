package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.PatientDetail;
import com.syamalahomeo.hospital.dto.PatientForm;
import com.syamalahomeo.hospital.dto.SettingsForm;
import com.syamalahomeo.hospital.entity.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PatientServiceTest {

    @Autowired private PatientService service;
    @Autowired private SettingsService settings;

    private PatientForm form(String name, String phone) {
        PatientForm f = new PatientForm();
        f.setFullName(name);
        f.setAgeYears(34);
        f.setGender(Gender.FEMALE);
        f.setPhone(phone);
        return f;
    }

    @Test
    void registrationCreatesCodeAndValidConsultation() {
        Long id = service.register(form("Test Patient", "9876543210"), "tester");
        PatientDetail detail = service.detail(id);
        assertTrue(detail.patient().code().startsWith("SHH-"));
        assertTrue(detail.latest().valid());
        assertEquals(15, detail.latest().daysLeft());
    }

    @Test
    void searchFindsByPhoneNameAndCode() {
        Long id = service.register(form("Searchable Person", "9123456789"), "tester");
        String code = service.detail(id).patient().code();
        assertEquals(1, service.search("9123456789").size());
        assertEquals(1, service.search("searchable").size());
        assertEquals(1, service.search(code).size());
    }

    @Test
    void changingValidityAppliesToNewConsultations() {
        SettingsForm sf = settings.form();
        sf.setConsultationValidityDays(7);
        settings.update(sf);

        Long id = service.register(form("Short Validity", "9000000001"), "tester");
        assertEquals(7, service.detail(id).latest().daysLeft());
    }

    @Test
    void duplicateIsDetected() {
        service.register(form("Same Person", "9000000002"), "tester");
        assertFalse(service.findPossibleDuplicates(form("same person", "9000000002")).isEmpty());
    }

    @Test
    void deleteRemovesPatientAndConsultations() {
        Long id = service.register(form("To Delete", "9000000003"), "tester");
        service.delete(id);
        assertTrue(service.search("9000000003").isEmpty());
    }
}