package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.ContactRequest;
import com.syamalahomeo.hospital.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactPageController {

    private final ContactService contactService;

    public ContactPageController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/contact")
    public String form(Model model) {
        model.addAttribute("contactRequest", new ContactRequest());
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("contactRequest") ContactRequest request,
                         BindingResult result,
                         @RequestParam(name = "website", required = false) String honeypot) {
        if (honeypot != null && !honeypot.isBlank()) {
            return "redirect:/contact?sent"; // looks like success to a bot; nothing saved
        }
        if (!result.hasErrors() && isBlank(request.getPhone()) && isBlank(request.getEmail())) {
            result.reject("contact.reachable",
                    "Please give a phone number or an email address so we can reply.");
        }
        if (result.hasErrors()) {
            return "contact";
        }
        contactService.save(request);
        return "redirect:/contact?sent";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}