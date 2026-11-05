package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByActiveTrueOrderByDisplayOrderAscFullNameAsc();

    Optional<Doctor> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);
}