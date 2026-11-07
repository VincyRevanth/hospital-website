package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.config.HospitalProperties;
import com.syamalahomeo.hospital.dto.HospitalInfo;
import com.syamalahomeo.hospital.dto.SettingsForm;
import com.syamalahomeo.hospital.entity.HospitalSettings;
import com.syamalahomeo.hospital.repository.HospitalSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final HospitalSettingsRepository repository;
    private final HospitalProperties props;

    public SettingsService(HospitalSettingsRepository repository, HospitalProperties props) {
        this.repository = repository;
        this.props = props;
    }

    @Transactional(readOnly = true)
    public HospitalInfo current() {
        HospitalSettings s = repository.findById(HospitalSettings.SINGLE_ID).orElse(null);
        String phone = pick(s == null ? null : s.getPhone(), props.getPhoneDisplay());
        return new HospitalInfo(
                props.getName(),
                phone,
                phoneLink(phone),
                pick(s == null ? null : s.getEmail(), props.getEmail()),
                pick(s == null ? null : s.getAddress(), props.getAddress()),
                pick(s == null ? null : s.getOpeningHours(), props.getOpeningHours()),
                props.getEstablishedYear(),
                s == null ? null : blankToNull(s.getMapUrl()));
    }

    @Transactional(readOnly = true)
    public SettingsForm form() {
        SettingsForm form = new SettingsForm();
        repository.findById(HospitalSettings.SINGLE_ID).ifPresent(s -> {
        	form.setConsultationValidityDays(s.getConsultationValidityDays());
            form.setPhone(s.getPhone());
            form.setEmail(s.getEmail());
            form.setAddress(s.getAddress());
            form.setOpeningHours(s.getOpeningHours());
            form.setMapUrl(s.getMapUrl());
        });
        return form;
    }

    @Transactional
    public void update(SettingsForm form) {
        HospitalSettings s = repository.findById(HospitalSettings.SINGLE_ID).orElseGet(HospitalSettings::new);
        s.setPhone(blankToNull(form.getPhone()));
        s.setEmail(blankToNull(form.getEmail()));
        s.setAddress(blankToNull(form.getAddress()));
        s.setOpeningHours(blankToNull(form.getOpeningHours()));
        s.setMapUrl(blankToNull(form.getMapUrl()));
        s.setConsultationValidityDays(form.getConsultationValidityDays());
        repository.save(s);
    }

    private static String phoneLink(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "+91" + digits;
        }
        return "+" + digits;
    }

    private static String pick(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
    @Transactional(readOnly = true)
    public int consultationValidityDays() {
        return repository.findById(HospitalSettings.SINGLE_ID)
                .map(HospitalSettings::getConsultationValidityDays)
                .orElse(15);
    }
}