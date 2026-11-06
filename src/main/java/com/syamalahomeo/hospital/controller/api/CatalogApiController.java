package com.syamalahomeo.hospital.controller.api;

import com.syamalahomeo.hospital.dto.DoctorResponse;
import com.syamalahomeo.hospital.dto.ServiceView;
import com.syamalahomeo.hospital.service.DoctorService;
import com.syamalahomeo.hospital.service.ServiceOfferingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogApiController {

    private final DoctorService doctorService;
    private final ServiceOfferingService serviceOfferingService;

    public CatalogApiController(DoctorService doctorService, ServiceOfferingService serviceOfferingService) {
        this.doctorService = doctorService;
        this.serviceOfferingService = serviceOfferingService;
    }

    @GetMapping("/doctors")
    public List<DoctorResponse> doctors() {
        return doctorService.apiList();
    }

    @GetMapping("/doctors/{id}")
    public DoctorResponse doctor(@PathVariable Long id) {
        return doctorService.apiFind(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    @GetMapping("/services")
    public List<ServiceView> services() {
        return serviceOfferingService.findActive();
    }
}