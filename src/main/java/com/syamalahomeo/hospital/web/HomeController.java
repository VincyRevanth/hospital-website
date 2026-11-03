package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DoctorService doctorService;

    public HomeController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        return "home";
    }
}