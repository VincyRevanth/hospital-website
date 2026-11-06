package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.service.DoctorService;
import com.syamalahomeo.hospital.service.SeoService;
import com.syamalahomeo.hospital.service.ServiceOfferingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DoctorService doctorService;
    private final ServiceOfferingService serviceOfferingService;
    private final SeoService seoService;

    public HomeController(DoctorService doctorService, ServiceOfferingService serviceOfferingService,
                          SeoService seoService) {
        this.doctorService = doctorService;
        this.serviceOfferingService = serviceOfferingService;
        this.seoService = seoService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("services", serviceOfferingService.findActive());
        model.addAttribute("jsonLd", seoService.hospitalJsonLd());
        return "home";
    }
}