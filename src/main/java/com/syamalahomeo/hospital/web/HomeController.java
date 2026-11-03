package com.syamalahomeo.hospital.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("hospitalName", "SYAMALA HOMEO HOSPITAL");
        return "home";
    }
}