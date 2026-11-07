package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.PatientForm;
import com.syamalahomeo.hospital.dto.PatientView;
import com.syamalahomeo.hospital.entity.Gender;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.service.DoctorService;
import com.syamalahomeo.hospital.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/patients")
public class AdminPatientController {

    private static final Logger log = LoggerFactory.getLogger(AdminPatientController.class);

    private final PatientService service;
    private final DoctorService doctorService;

    public AdminPatientController(PatientService service, DoctorService doctorService) {
        this.service = service;
        this.doctorService = doctorService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("patients", service.search(q));
        model.addAttribute("q", q == null ? "" : q);
        return "admin/patients";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new PatientForm());
        prepareForm(model, "Register patient", "/admin/patients", false);
        return "admin/patient-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PatientForm form, BindingResult result,
                         Model model, Principal principal, RedirectAttributes redirect) {
        checkGuardian(form, result);
        if (!result.hasErrors() && !form.isCreateAnyway()) {
            List<PatientView> duplicates = service.findPossibleDuplicates(form);
            if (!duplicates.isEmpty()) {
                model.addAttribute("duplicates", duplicates);
                prepareForm(model, "Register patient", "/admin/patients", false);
                return "admin/patient-form";
            }
        }
        if (!result.hasErrors()) {
            try {
                Long id = service.register(form, principal.getName());
                redirect.addFlashAttribute("message",
                        "Patient registered. A consultation was recorded for today.");
                return "redirect:/admin/patients/" + id;
            } catch (BusinessRuleException ex) {
                result.reject("patient.rule", ex.getMessage());
            }
        }
        prepareForm(model, "Register patient", "/admin/patients", false);
        return "admin/patient-form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("detail", service.detail(id));
        model.addAttribute("doctors", doctorService.findAll());
        return "admin/patient-detail";
    }

    @PostMapping("/{id}/consultations")
    public String newConsultation(@PathVariable Long id,
                                  @RequestParam(required = false) Long doctorId,
                                  Principal principal, RedirectAttributes redirect) {
        try {
            service.startConsultation(id, doctorId, principal.getName());
            redirect.addFlashAttribute("message", "New consultation recorded for today.");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/patients/" + id;
    }

    @GetMapping("/{id}/print")
    public String print(@PathVariable Long id, Model model, Principal principal,
                        RedirectAttributes redirect) {
        try {
            model.addAttribute("sheet", service.printData(id));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/patients/" + id;
        }
        log.info("OP form opened for patient id {} by {}", id, principal.getName());
        return "admin/patient-print";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.form(id));
        prepareForm(model, "Edit patient", "/admin/patients/" + id, true);
        return "admin/patient-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") PatientForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        checkGuardian(form, result);
        if (result.hasErrors()) {
            prepareForm(model, "Edit patient", "/admin/patients/" + id, true);
            return "admin/patient-form";
        }
        service.update(id, form);
        redirect.addFlashAttribute("message", "Patient details updated.");
        return "redirect:/admin/patients/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("message", "Patient record deleted.");
        return "redirect:/admin/patients";
    }

    private void checkGuardian(PatientForm form, BindingResult result) {
        boolean minor = form.getAgeYears() != null && form.getAgeYears() < 18;
        if (minor && (form.getGuardianName() == null || form.getGuardianName().isBlank())) {
            result.rejectValue("guardianName", "guardian.required",
                    "For patients under 18, please enter the parent or guardian's name.");
        }
    }

    private void prepareForm(Model model, String title, String action, boolean editing) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("editing", editing);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("doctors", doctorService.findAll());
    }
}