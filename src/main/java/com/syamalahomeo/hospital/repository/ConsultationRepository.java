package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.Consultation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    @EntityGraph(attributePaths = {"doctor"})
    List<Consultation> findTop10ByPatientIdOrderByConsultationDateDescIdDesc(Long patientId);

    long deleteByPatientId(Long patientId);
}