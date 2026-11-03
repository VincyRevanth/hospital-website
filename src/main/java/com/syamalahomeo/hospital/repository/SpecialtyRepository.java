package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
}