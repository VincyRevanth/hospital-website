package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.DoctorView;
import com.syamalahomeo.hospital.entity.Doctor;
import com.syamalahomeo.hospital.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private static final String BIO_PLACEHOLDER = "[Profile to be supplied by the hospital]";

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<DoctorView> findAll() {
        return doctorRepository.findByActiveTrueOrderByDisplayOrderAscFullNameAsc()
                .stream().map(this::toView).toList();
    }

    public Optional<DoctorView> findBySlug(String slug) {
        return doctorRepository.findBySlugAndActiveTrue(slug).map(this::toView);
    }

    private DoctorView toView(Doctor d) {
        String bio = (d.getBio() == null || d.getBio().isBlank()) ? BIO_PLACEHOLDER : d.getBio();
        return new DoctorView(d.getSlug(), d.getFullName(), d.getQualification(), bio);
    }
}