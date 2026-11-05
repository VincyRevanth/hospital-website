package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.AdminAppointmentView;
import com.syamalahomeo.hospital.dto.AdminStats;
import com.syamalahomeo.hospital.entity.Appointment;
import com.syamalahomeo.hospital.entity.AppointmentStatus;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AdminAppointmentService.class);

    private final AppointmentRepository repository;
    private final Clock clock;
    private final DateTimeFormatter createdFormat;

    public AdminAppointmentService(AppointmentRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
        this.createdFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(clock.getZone());
    }

    @Transactional(readOnly = true)
    public List<AdminAppointmentView> search(AppointmentStatus status, LocalDate date) {
        List<Appointment> found;
        if (status != null && date != null) {
            found = repository.findByStatusAndPreferredDateOrderByPreferredTimeAsc(status, date);
        } else if (status != null) {
            found = repository.findByStatusOrderByPreferredDateAscPreferredTimeAsc(status);
        } else if (date != null) {
            found = repository.findByPreferredDateOrderByPreferredTimeAsc(date);
        } else {
            found = repository.findAllByOrderByPreferredDateAscPreferredTimeAsc();
        }
        return found.stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public AdminStats stats() {
        LocalDate today = LocalDate.now(clock);
        return new AdminStats(
                repository.count(),
                repository.countByStatus(AppointmentStatus.PENDING),
                repository.countByStatus(AppointmentStatus.CONFIRMED),
                repository.countByStatus(AppointmentStatus.REJECTED),
                repository.countByStatus(AppointmentStatus.CANCELLED),
                repository.countByStatusAndPreferredDateGreaterThanEqual(AppointmentStatus.CONFIRMED, today));
    }

    @Transactional
    public void changeStatus(Long id, String action) {
        Appointment appointment = repository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Appointment not found."));
        switch (action) {
            case "confirm" -> {
                if (appointment.getStatus() == AppointmentStatus.PENDING && appointment.getDoctor() != null
                        && repository.existsByConfirmedSlotKey(Appointment.slotKey(
                                appointment.getDoctor(), appointment.getPreferredDate(),
                                appointment.getPreferredTime()))) {
                    throw new BusinessRuleException(
                            "Another confirmed appointment already uses this doctor and time.");
                }
                appointment.confirm();
            }
            case "reject" -> appointment.reject();
            case "cancel" -> appointment.cancel();
            default -> throw new BusinessRuleException("Unknown action.");
        }
        repository.saveAndFlush(appointment); // flush so the DB constraint is checked now
        log.info("Appointment {} set to {}", id, appointment.getStatus());
    }

    private AdminAppointmentView toView(Appointment a) {
        return new AdminAppointmentView(
                a.getId(), a.getPatientName(), a.getPhone(), a.getEmail(),
                a.getDoctor() != null ? a.getDoctor().getFullName() : null,
                a.getService() != null ? a.getService().getName() : null,
                a.getPreferredDate(), a.getPreferredTime().toString(), a.getMessage(),
                a.getStatus().name(), createdFormat.format(a.getCreatedAt()));
    }
}