package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.dto.PasswordChangeForm;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.security.PasswordChangeRequiredFilter;
import com.syamalahomeo.hospital.service.AdminAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/password")
public class AdminAccountController {

    private final AdminAccountService service;

    public AdminAccountController(AdminAccountService service) {
        this.service = service;
    }

    @GetMapping
    public String form(Model model, HttpSession session) {
        model.addAttribute("form", new PasswordChangeForm());
        model.addAttribute("forced", forced(session));
        return "admin/password";
    }

    @PostMapping
    public String change(@Valid @ModelAttribute("form") PasswordChangeForm form, BindingResult result,
                         Principal principal, HttpSession session, HttpServletRequest request,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors() && !form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "The new passwords do not match.");
        }
        if (!result.hasErrors()) {
            try {
                service.changePassword(principal.getName(), form.getCurrentPassword(), form.getNewPassword());
                session.removeAttribute(PasswordChangeRequiredFilter.ATTRIBUTE);
                redirect.addFlashAttribute("message", "Your password was changed.");
                return request.isUserInRole("ADMIN") ? "redirect:/admin" : "redirect:/admin/patients";
            } catch (BusinessRuleException ex) {
                result.reject("password.rule", ex.getMessage());
            }
        }
        model.addAttribute("forced", forced(session));
        return "admin/password";
    }

    private static boolean forced(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(PasswordChangeRequiredFilter.ATTRIBUTE));
    }
}