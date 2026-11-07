package com.syamalahomeo.hospital.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syamalahomeo.hospital.repository.AppointmentRepository;
import com.syamalahomeo.hospital.repository.ContactMessageRepository;

@Service
public class RetentionService {

    private static final Logger log = LoggerFactory.getLogger(RetentionService.class);

    private final ContactMessageRepository messages;
    private final AppointmentRepository appointments;
    private final Clock clock;
    private final int contactDays;
    private final int appointmentDays;

    public RetentionService(ContactMessageRepository messages, AppointmentRepository appointments, Clock clock,
                            @Value("${retention.contact-message-days:0}") int contactDays,
                            @Value("${retention.appointment-days:0}") int appointmentDays) {
        this.messages = messages;
        this.appointments = appointments;
        this.clock = clock;
        this.contactDays = contactDays;
        this.appointmentDays = appointmentDays;
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Kolkata")
    public void runScheduled() {
        if (contactDays > 0) {
            long n = purgeContactMessagesCreatedBefore(Instant.now().minus(contactDays, ChronoUnit.DAYS));
            log.info("Retention: removed {} contact messages", n);
        }
        if (appointmentDays > 0) {
            long n = purgeAppointmentsForDatesBefore(LocalDate.now(clock).minusDays(appointmentDays));
            log.info("Retention: removed {} appointment records", n);
        }
    }

    @Transactional
    public long purgeContactMessagesCreatedBefore(Instant cutoff) {
        return messages.deleteByCreatedAtBefore(cutoff);
    }

    @Transactional
    public long purgeAppointmentsForDatesBefore(LocalDate cutoff) {
        return appointments.deleteByPreferredDateBefore(cutoff);
    }
}