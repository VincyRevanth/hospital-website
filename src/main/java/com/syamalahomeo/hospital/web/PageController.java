package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.service.DoctorService;
import com.syamalahomeo.hospital.service.ServiceOfferingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PageController {

    private final DoctorService doctorService;
    private final ServiceOfferingService serviceOfferingService;

    public PageController(DoctorService doctorService, ServiceOfferingService serviceOfferingService) {
        this.doctorService = doctorService;
        this.serviceOfferingService = serviceOfferingService;
    }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/treatments")
    public String treatments(Model model) {
        model.addAttribute("services", serviceOfferingService.findActive());
        return "treatments";
    }

    @GetMapping("/faq")
    public String faq() { return "faq"; }

    @GetMapping("/privacy")
    public String privacy() { return "privacy"; }

    @GetMapping("/doctors")
    public String doctors(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        return "doctors";
    }

    @GetMapping("/doctors/{slug}")
    public String doctor(@PathVariable String slug, Model model) {
        var doctor = doctorService.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("doctor", doctor);
        return "doctor";
    }
}