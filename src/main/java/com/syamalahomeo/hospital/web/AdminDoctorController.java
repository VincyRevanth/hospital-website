package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.DoctorForm;
import com.syamalahomeo.hospital.service.AdminDoctorService;
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
@RequestMapping("/admin/doctors")
public class AdminDoctorController {

    private final AdminDoctorService service;

    public AdminDoctorController(AdminDoctorService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("doctors", service.list());
        return "admin/doctors";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new DoctorForm());
        prepare(model, "Add doctor", "/admin/doctors");
        return "admin/doctor-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") DoctorForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepare(model, "Add doctor", "/admin/doctors");
            return "admin/doctor-form";
        }
        service.create(form);
        redirect.addFlashAttribute("message", "Doctor added.");
        return "redirect:/admin/doctors";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.form(id));
        prepare(model, "Edit doctor", "/admin/doctors/" + id);
        return "admin/doctor-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") DoctorForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepare(model, "Edit doctor", "/admin/doctors/" + id);
            return "admin/doctor-form";
        }
        service.update(id, form);
        redirect.addFlashAttribute("message", "Doctor updated.");
        return "redirect:/admin/doctors";
    }

    private void prepare(Model model, String title, String action) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
    }
}