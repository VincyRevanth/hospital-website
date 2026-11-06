package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.service.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/messages")
public class AdminMessageController {

    private final ContactService contactService;

    public AdminMessageController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("messages", contactService.latest());
        return "admin/messages";
    }
}