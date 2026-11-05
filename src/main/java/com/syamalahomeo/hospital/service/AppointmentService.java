package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.config.AppointmentProperties;
import com.syamalahomeo.hospital.dto.AppointmentRequest;
import com.syamalahomeo.hospital.dto.AppointmentResponse;
import com.syamalahomeo.hospital.entity.Appointment;
import com.syamalahomeo.hospital.entity.AppointmentStatus;
import com.syamalahomeo.hospital.entity.Doctor;
import com.syamalahomeo.hospital.entity.ServiceOffering;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.repository.AppointmentRepository;
import com.syamalahomeo.hospital.repository.DoctorRepository;
import com.syamalahomeo.hospital.repository.ServiceOfferingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private static final String NOTICE =
            "Your request has been received. It is NOT a confirmed appointment yet. "
            + "The hospital will contact you to confirm, change or decline it.";

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceOfferingRepository serviceRepository;
    private final AppointmentProperties props;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              ServiceOfferingRepository serviceRepository,
                              AppointmentProperties props,
                              Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.serviceRepository = serviceRepository;
        this.props = props;
        this.clock = clock;
    }

    public List<String> timeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime t = props.getEarliestTime();
        while (!t.isAfter(props.getLatestTime())) {
            slots.add(t.toString());
            LocalTime next = t.plusMinutes(props.getSlotMinutes());
            if (!next.isAfter(t)) {
                break; // passed midnight
            }
            t = next;
        }
        return slots;
    }

    public LocalDate minDate() {
        return LocalDate.now(clock).plusDays(1);
    }

    public LocalDate maxDate() {
        return LocalDate.now(clock).plusDays(props.getMaxDaysAhead());
    }

    @Transactional
    public AppointmentResponse submit(AppointmentRequest req) {
        Doctor doctor = null;
        if (req.getDoctorId() != null) {
            doctor = doctorRepository.findById(req.getDoctorId())
                    .filter(Doctor::isActive)
                    .orElseThrow(() -> new BusinessRuleException("The selected doctor is not available."));
        }
        ServiceOffering service = null;
        if (req.getServiceId() != null) {
            service = serviceRepository.findById(req.getServiceId())
                    .filter(ServiceOffering::isActive)
                    .orElseThrow(() -> new BusinessRuleException("The selected service is not available."));
        }
        if (doctor == null && service == null) {
            throw new BusinessRuleException("Please choose a doctor or a service.");
        }

        LocalDate date = req.getPreferredDate();
        if (date.isBefore(minDate()) || date.isAfter(maxDate())) {
            throw new BusinessRuleException(
                    "Please choose a date between " + minDate() + " and " + maxDate() + ".");
        }
        if (!timeSlots().contains(req.getPreferredTime())) {
            throw new BusinessRuleException("Please choose one of the available times.");
        }
        LocalTime time = LocalTime.parse(req.getPreferredTime());
        String phone = normalizePhone(req.getPhone());

        if (appointmentRepository.existsByPhoneAndPreferredDateAndPreferredTimeAndStatusIn(
                phone, date, time, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))) {
            throw new BusinessRuleException(
                    "A request for this date and time already exists for this phone number.");
        }
        if (doctor != null && appointmentRepository.existsByConfirmedSlotKey(
                Appointment.slotKey(doctor, date, time))) {
            throw new BusinessRuleException(
                    "That time is already taken for the selected doctor. Please choose another time.");
        }

        Appointment appointment = new Appointment(
                req.getPatientName().trim(), phone, blankToNull(req.getEmail()),
                doctor, service, date, time, blankToNull(req.getMessage()));
        appointmentRepository.save(appointment);
        log.info("Appointment request {} created", appointment.getId()); // no patient details in logs

        return new AppointmentResponse(
                appointment.getId(), appointment.getStatus().name(),
                doctor != null ? doctor.getFullName() : null,
                service != null ? service.getName() : null,
                date, req.getPreferredTime(), NOTICE);
    }

    private static String normalizePhone(String raw) {
        String digits = raw.replaceAll("\\D", "");
        return digits.substring(digits.length() - 10);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}	