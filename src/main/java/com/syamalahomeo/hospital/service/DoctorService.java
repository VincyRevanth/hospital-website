package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.DoctorView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private static final String BIO_PLACEHOLDER = "[Profile to be supplied by the hospital]";

    private final List<DoctorView> doctors = List.of(
            new DoctorView("abburi-aruna", "Dr Abburi Aruna", "B.H.M.S", BIO_PLACEHOLDER),
            new DoctorView("sudeepthi-bandaru", "Dr Sudeepthi Bandaru", "B.H.M.S", BIO_PLACEHOLDER)
    );

    public List<DoctorView> findAll() {
        return doctors;
    }

    public Optional<DoctorView> findBySlug(String slug) {
        return doctors.stream().filter(d -> d.getSlug().equals(slug)).findFirst();
    }
}