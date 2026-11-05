package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.AppointmentRequest;
import com.syamalahomeo.hospital.dto.AppointmentResponse;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.service.AppointmentService;
import com.syamalahomeo.hospital.service.DoctorService;
import com.syamalahomeo.hospital.service.ServiceOfferingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppointmentPageController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final ServiceOfferingService serviceOfferingService;

    public AppointmentPageController(AppointmentService appointmentService,
                                     DoctorService doctorService,
                                     ServiceOfferingService serviceOfferingService) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.serviceOfferingService = serviceOfferingService;
    }

    @GetMapping("/appointments/new")
    public String form(Model model) {
        model.addAttribute("appointmentRequest", new AppointmentRequest());
        populate(model);
        return "appointment-form";
    }

    @PostMapping("/appointments")
    public String submit(@Valid @ModelAttribute("appointmentRequest") AppointmentRequest request,
                         BindingResult bindingResult,
                         @RequestParam(name = "website", required = false) String honeypot,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (honeypot != null && !honeypot.isBlank()) {
            return "redirect:/appointments/received"; // looks like success to a bot; nothing saved
        }
        if (bindingResult.hasErrors()) {
            populate(model);
            return "appointment-form";
        }
        try {
            AppointmentResponse response = appointmentService.submit(request);
            redirectAttributes.addFlashAttribute("appointment", response);
            return "redirect:/appointments/received";
        } catch (BusinessRuleException ex) {
            bindingResult.reject("appointment.rule", ex.getMessage());
            populate(model);
            return "appointment-form";
        }
    }

    @GetMapping("/appointments/received")
    public String received() {
        return "appointment-received";
    }

    private void populate(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("services", serviceOfferingService.findActive());
        model.addAttribute("slots", appointmentService.timeSlots());
        model.addAttribute("minDate", appointmentService.minDate());
        model.addAttribute("maxDate", appointmentService.maxDate());
    }
}