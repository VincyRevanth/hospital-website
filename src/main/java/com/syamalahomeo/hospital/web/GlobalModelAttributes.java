package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.config.HospitalProperties;
import com.syamalahomeo.hospital.dto.HospitalInfo;
import com.syamalahomeo.hospital.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAttributes.class);

    private final SettingsService settingsService;
    private final HospitalProperties properties;
    private final String baseUrl;

    public GlobalModelAttributes(SettingsService settingsService, HospitalProperties properties,
                                 @Value("${site.base-url:http://localhost:8080}") String baseUrl) {
        this.settingsService = settingsService;
        this.properties = properties;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @ModelAttribute("hospital")
    public HospitalInfo hospital() {
        try {
            return settingsService.current();
        } catch (RuntimeException ex) {
            // Keeps error pages working even if the database is unavailable
            log.warn("Hospital settings unavailable; using defaults");
            return new HospitalInfo(properties.getName(), properties.getPhoneDisplay(),
                    properties.getPhoneLink(), properties.getEmail(), properties.getAddress(),
                    properties.getOpeningHours(), properties.getEstablishedYear(), null);
        }
    }

    @ModelAttribute("hospitalName")
    public String hospitalName() {
        return properties.getName();
    }

    @ModelAttribute("baseUrl")
    public String baseUrl() {
        return baseUrl;
    }

    @ModelAttribute("canonicalUrl")
    public String canonicalUrl(HttpServletRequest request) {
        return baseUrl + request.getRequestURI(); // path only: query strings are never canonical
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}