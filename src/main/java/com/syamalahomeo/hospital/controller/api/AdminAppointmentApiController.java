package com.syamalahomeo.hospital.controller.api;

import com.syamalahomeo.hospital.dto.AdminAppointmentView;
import com.syamalahomeo.hospital.entity.AppointmentStatus;
import com.syamalahomeo.hospital.service.AdminAppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentApiController {

    private final AdminAppointmentService service;

    public AdminAppointmentApiController(AdminAppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminAppointmentView> list(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.search(status, date);
    }
}