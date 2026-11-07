package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.StaffForm;
import com.syamalahomeo.hospital.dto.StaffPasswordForm;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.service.AdminAccountService;
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
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminAccountService service;

    public AdminUserController(AdminAccountService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("staff", service.listStaff());
        return "admin/users";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new StaffForm());
        return "admin/user-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") StaffForm form, BindingResult result,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.createStaff(form.getUsername(), form.getPassword());
                redirect.addFlashAttribute("message",
                        "Staff account created. Give the temporary password to the staff member privately. "
                        + "They must change it at first sign-in.");
                return "redirect:/admin/users";
            } catch (BusinessRuleException ex) {
                result.rejectValue("username", "duplicate", ex.getMessage());
            }
        }
        return "admin/user-form";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Long id, RedirectAttributes redirect) {
        service.setEnabled(id, false);
        redirect.addFlashAttribute("message", "Account disabled.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Long id, RedirectAttributes redirect) {
        service.setEnabled(id, true);
        redirect.addFlashAttribute("message", "Account enabled.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/reset")
    public String resetForm(@PathVariable Long id, Model model) {
        model.addAttribute("username", service.staffUsername(id));
        model.addAttribute("form", new StaffPasswordForm());
        model.addAttribute("userId", id);
        return "admin/user-reset";
    }

    @PostMapping("/{id}/reset")
    public String reset(@PathVariable Long id, @Valid @ModelAttribute("form") StaffPasswordForm form,
                        BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("username", service.staffUsername(id));
            model.addAttribute("userId", id);
            return "admin/user-reset";
        }
        service.resetPassword(id, form.getPassword());
        redirect.addFlashAttribute("message",
                "Password reset. Give the temporary password to the staff member privately.");
        return "redirect:/admin/users";
    }
}