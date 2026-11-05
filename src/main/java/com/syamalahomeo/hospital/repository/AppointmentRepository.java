package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.Appointment;
import com.syamalahomeo.hospital.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByPhoneAndPreferredDateAndPreferredTimeAndStatusIn(
            String phone, LocalDate date, LocalTime time, Collection<AppointmentStatus> statuses);

    boolean existsByConfirmedSlotKey(String confirmedSlotKey);

    long countByStatus(AppointmentStatus status);

    long countByStatusAndPreferredDateGreaterThanEqual(AppointmentStatus status, LocalDate date);

    @EntityGraph(attributePaths = {"doctor", "service"})
    List<Appointment> findAllByOrderByPreferredDateAscPreferredTimeAsc();

    @EntityGraph(attributePaths = {"doctor", "service"})
    List<Appointment> findByStatusOrderByPreferredDateAscPreferredTimeAsc(AppointmentStatus status);

    @EntityGraph(attributePaths = {"doctor", "service"})
    List<Appointment> findByPreferredDateOrderByPreferredTimeAsc(LocalDate date);

    @EntityGraph(attributePaths = {"doctor", "service"})
    List<Appointment> findByStatusAndPreferredDateOrderByPreferredTimeAsc(AppointmentStatus status, LocalDate date);
}