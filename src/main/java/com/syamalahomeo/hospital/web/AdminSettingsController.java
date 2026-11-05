package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.SettingsForm;
import com.syamalahomeo.hospital.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    private final SettingsService service;

    public AdminSettingsController(SettingsService service) {
        this.service = service;
    }

    @GetMapping
    public String show(Model model) {
        model.addAttribute("form", service.form());
        return "admin/settings";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("form") SettingsForm form, BindingResult result,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "admin/settings";
        }
        service.update(form);
        redirect.addFlashAttribute("message", "Contact details were saved.");
        return "redirect:/admin/settings";
    }
}