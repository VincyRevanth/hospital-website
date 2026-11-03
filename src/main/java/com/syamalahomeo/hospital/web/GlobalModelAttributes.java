package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.config.HospitalProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final HospitalProperties hospital;

    public GlobalModelAttributes(HospitalProperties hospital) {
        this.hospital = hospital;
    }

    @ModelAttribute("hospital")
    public HospitalProperties hospital() {
        return hospital;
    }

    @ModelAttribute("hospitalName")
    public String hospitalName() {
        return hospital.getName();
    }
}