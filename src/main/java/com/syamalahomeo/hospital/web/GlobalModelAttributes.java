package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.config.HospitalProperties;
import com.syamalahomeo.hospital.dto.HospitalInfo;
import com.syamalahomeo.hospital.service.SettingsService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final SettingsService settingsService;
    private final HospitalProperties properties;

    public GlobalModelAttributes(SettingsService settingsService, HospitalProperties properties) {
        this.settingsService = settingsService;
        this.properties = properties;
    }

    @ModelAttribute("hospital")
    public HospitalInfo hospital() {
        return settingsService.current();
    }

    @ModelAttribute("hospitalName")
    public String hospitalName() {
        return properties.getName();
    }
}