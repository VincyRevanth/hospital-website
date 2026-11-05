package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.AdminDoctorView;
import com.syamalahomeo.hospital.dto.DoctorForm;
import com.syamalahomeo.hospital.entity.Doctor;
import com.syamalahomeo.hospital.repository.DoctorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class AdminDoctorService {

    private final DoctorRepository repository;

    public AdminDoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AdminDoctorView> list() {
        return repository.findAll(Sort.by("displayOrder", "fullName")).stream()
                .map(d -> new AdminDoctorView(d.getId(), d.getSlug(), d.getFullName(),
                        d.getQualification(), d.getDisplayOrder(), d.isActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorForm form(Long id) {
        Doctor d = find(id);
        DoctorForm form = new DoctorForm();
        form.setFullName(d.getFullName());
        form.setQualification(d.getQualification());
        form.setBio(d.getBio());
        form.setRegistrationNumber(d.getRegistrationNumber());
        form.setDisplayOrder(d.getDisplayOrder());
        form.setActive(d.isActive());
        return form;
    }

    @Transactional
    public void create(DoctorForm form) {
        Doctor d = new Doctor(uniqueSlug(form.getFullName()), form.getFullName().trim(),
                form.getQualification().trim());
        apply(d, form);
        repository.save(d);
    }

    @Transactional
    public void update(Long id, DoctorForm form) {
        Doctor d = find(id);
        d.setFullName(form.getFullName().trim());
        d.setQualification(form.getQualification().trim());
        apply(d, form);
        repository.save(d);
    }

    private void apply(Doctor d, DoctorForm form) {
        d.setBio(blankToNull(form.getBio()));
        d.setRegistrationNumber(blankToNull(form.getRegistrationNumber()));
        d.setDisplayOrder(form.getDisplayOrder());
        d.setActive(form.isActive());
    }

    private Doctor find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String uniqueSlug(String name) {
        String base = name.toLowerCase(Locale.ROOT)
                .replaceAll("^dr\\.?\\s+", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isEmpty()) {
            base = "doctor";
        }
        String slug = base;
        int n = 2;
        while (repository.existsBySlug(slug)) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}