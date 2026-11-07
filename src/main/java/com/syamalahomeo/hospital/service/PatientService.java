package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.ConsultationView;
import com.syamalahomeo.hospital.dto.PatientDetail;
import com.syamalahomeo.hospital.dto.PatientForm;
import com.syamalahomeo.hospital.dto.PatientView;
import com.syamalahomeo.hospital.dto.PrintData;
import com.syamalahomeo.hospital.entity.Consultation;
import com.syamalahomeo.hospital.entity.Doctor;
import com.syamalahomeo.hospital.entity.Patient;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.repository.ConsultationRepository;
import com.syamalahomeo.hospital.repository.DoctorRepository;
import com.syamalahomeo.hospital.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final PatientRepository patients;
    private final ConsultationRepository consultations;
    private final DoctorRepository doctors;
    private final SettingsService settings;
    private final Clock clock;
    private final String prefix;

    public PatientService(PatientRepository patients, ConsultationRepository consultations,
                          DoctorRepository doctors, SettingsService settings, Clock clock,
                          @Value("${patient.id-prefix:SHH}") String prefix) {
        this.patients = patients;
        this.consultations = consultations;
        this.doctors = doctors;
        this.settings = settings;
        this.clock = clock;
        this.prefix = prefix;
    }

    @Transactional
    public Long register(PatientForm form, String username) {
        LocalDate today = LocalDate.now(clock);
        Patient patient = new Patient(form.getFullName().trim(), form.getAgeYears(), today,
                form.getGender(), normalizePhone(form.getPhone()), blankToNull(form.getAddress()),
                blankToNull(form.getGuardianName()), username);
        patients.save(patient);
        patient.setPatientCode(prefix + "-" + String.format("%06d", patient.getId()));
        recordConsultation(patient, form.getDoctorId(), username, today);
        log.info("Patient {} registered by {}", patient.getPatientCode(), username);
        return patient.getId();
    }

    @Transactional
    public void startConsultation(Long patientId, Long doctorId, String username) {
        Patient patient = find(patientId);
        recordConsultation(patient, doctorId, username, LocalDate.now(clock));
        log.info("New consultation for patient {} by {}", patient.getPatientCode(), username);
    }

    @Transactional(readOnly = true)
    public PatientDetail detail(Long id) {
        LocalDate today = LocalDate.now(clock);
        Patient patient = find(id);
        List<ConsultationView> history = consultations
                .findTop10ByPatientIdOrderByConsultationDateDescIdDesc(id).stream()
                .map(c -> toView(c, today)).toList();
        return new PatientDetail(toView(patient, today),
                history.isEmpty() ? null : history.get(0), history, settings.consultationValidityDays());
    }

    @Transactional(readOnly = true)
    public PrintData printData(Long id) {
        LocalDate today = LocalDate.now(clock);
        Patient patient = find(id);
        List<Consultation> latest = consultations.findTop10ByPatientIdOrderByConsultationDateDescIdDesc(id);
        if (latest.isEmpty()) {
            throw new BusinessRuleException("No consultation has been recorded for this patient.");
        }
        return new PrintData(toView(patient, today), toView(latest.get(0), today), DATE.format(today));
    }

    @Transactional(readOnly = true)
    public List<PatientView> search(String q) {
        LocalDate today = LocalDate.now(clock);
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            return patients.findTop20ByOrderByCreatedAtDesc().stream().map(p -> toView(p, today)).toList();
        }
        String safe = query.replace("%", "").replace("_", "").replace("\\", "").trim();
        if (safe.isEmpty()) {
            return List.of();
        }
        String digits = safe.replaceAll("\\D", "");
        String code = safe.matches("\\d{1,9}")
                ? prefix + "-" + String.format("%06d", Long.parseLong(safe))
                : safe.toUpperCase(Locale.ROOT);
        return patients.search("%" + safe.toLowerCase(Locale.ROOT) + "%",
                        digits.isEmpty() ? "#" : "%" + digits + "%", code, PageRequest.of(0, 50))
                .stream().map(p -> toView(p, today)).toList();
    }

    @Transactional(readOnly = true)
    public List<PatientView> findPossibleDuplicates(PatientForm form) {
        LocalDate today = LocalDate.now(clock);
        return patients.findByPhoneAndFullNameIgnoreCase(
                        normalizePhone(form.getPhone()), form.getFullName().trim())
                .stream().map(p -> toView(p, today)).toList();
    }

    @Transactional(readOnly = true)
    public PatientForm form(Long id) {
        Patient p = find(id);
        PatientForm form = new PatientForm();
        form.setFullName(p.getFullName());
        form.setAgeYears(p.ageOn(LocalDate.now(clock)));
        form.setGender(p.getGender());
        form.setPhone(p.getPhone());
        form.setAddress(p.getAddress());
        form.setGuardianName(p.getGuardianName());
        return form;
    }

    @Transactional
    public void update(Long id, PatientForm form) {
        Patient p = find(id);
        p.setFullName(form.getFullName().trim());
        p.updateAge(form.getAgeYears(), LocalDate.now(clock));
        p.setGender(form.getGender());
        p.setPhone(normalizePhone(form.getPhone()));
        p.setAddress(blankToNull(form.getAddress()));
        p.setGuardianName(blankToNull(form.getGuardianName()));
        log.info("Patient {} details updated", p.getPatientCode());
    }

    @Transactional
    public void delete(Long id) {
        Patient p = find(id);
        consultations.deleteByPatientId(id);
        patients.delete(p);
        log.info("Patient {} deleted", p.getPatientCode());
    }

    private void recordConsultation(Patient patient, Long doctorId, String username, LocalDate today) {
        Doctor doctor = null;
        if (doctorId != null) {
            doctor = doctors.findById(doctorId).filter(Doctor::isActive)
                    .orElseThrow(() -> new BusinessRuleException("The selected doctor is not available."));
        }
        consultations.save(new Consultation(patient, doctor, today,
                settings.consultationValidityDays(), username));
    }

    private Patient find(Long id) {
        return patients.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private PatientView toView(Patient p, LocalDate today) {
        return new PatientView(p.getId(), p.getPatientCode(), p.getFullName(), p.ageOn(today),
                p.getGender().getLabel(), p.getPhone(), p.getAddress(), p.getGuardianName(),
                DATE.format(p.getCreatedAt().atZone(clock.getZone())));
    }

    private ConsultationView toView(Consultation c, LocalDate today) {
        boolean valid = c.isValidOn(today);
        long daysLeft = valid ? ChronoUnit.DAYS.between(today, c.getValidUntil()) : 0;
        return new ConsultationView(c.getId(), DATE.format(c.getConsultationDate()),
                DATE.format(c.getValidUntil()),
                c.getDoctor() == null ? null : c.getDoctor().getFullName(),
                c.getCreatedBy(), valid, daysLeft);
    }

    private static String normalizePhone(String raw) {
        String digits = raw.replaceAll("\\D", "");
        return digits.substring(digits.length() - 10);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}