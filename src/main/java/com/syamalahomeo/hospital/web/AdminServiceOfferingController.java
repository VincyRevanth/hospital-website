package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.ServiceForm;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.service.AdminServiceOfferingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceOfferingController {

    private final AdminServiceOfferingService service;

    public AdminServiceOfferingController(AdminServiceOfferingService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("services", service.list());
        return "admin/services";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ServiceForm());
        prepare(model, "Add service", "/admin/services");
        return "admin/service-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ServiceForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.create(form);
                redirect.addFlashAttribute("message", "Service added.");
                return "redirect:/admin/services";
            } catch (BusinessRuleException ex) {
                result.rejectValue("name", "duplicate", ex.getMessage());
            }
        }
        prepare(model, "Add service", "/admin/services");
        return "admin/service-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.form(id));
        prepare(model, "Edit service", "/admin/services/" + id);
        return "admin/service-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ServiceForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.update(id, form);
                redirect.addFlashAttribute("message", "Service updated.");
                return "redirect:/admin/services";
            } catch (BusinessRuleException ex) {
                result.rejectValue("name", "duplicate", ex.getMessage());
            }
        }
        prepare(model, "Edit service", "/admin/services/" + id);
        return "admin/service-form";
    }

    private void prepare(Model model, String title, String action) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
    }
}