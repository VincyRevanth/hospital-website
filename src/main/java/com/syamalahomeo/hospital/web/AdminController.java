package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.entity.AppointmentStatus;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.exception.InvalidStatusTransitionException;
import com.syamalahomeo.hospital.service.AdminAppointmentService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminAppointmentService service;

    public AdminController(AdminAppointmentService service) {
        this.service = service;
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("adminName", principal.getName());
        model.addAttribute("stats", service.stats());
        return "admin/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        model.addAttribute("appointments", service.search(status, date));
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStatusText", status == null ? "" : status.name());
        model.addAttribute("selectedDate", date == null ? "" : date.toString());
        return "admin/appointments";
    }

    @PostMapping("/appointments/{id}/{action}")
    public String change(@PathVariable Long id, @PathVariable String action,
                         @RequestParam(required = false) String returnStatus,
                         @RequestParam(required = false) String returnDate,
                         RedirectAttributes redirect) {
        try {
            service.changeStatus(id, action);
            redirect.addFlashAttribute("message", "Appointment #" + id + " was updated.");
        } catch (BusinessRuleException | InvalidStatusTransitionException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error",
                    "That doctor and time slot is already confirmed for another patient.");
        } catch (ObjectOptimisticLockingFailureException ex) {
            redirect.addFlashAttribute("error",
                    "This appointment was changed by someone else. Please review it and try again.");
        }
        if (returnStatus != null && !returnStatus.isBlank()) {
            redirect.addAttribute("status", returnStatus);
        }
        if (returnDate != null && !returnDate.isBlank()) {
            redirect.addAttribute("date", returnDate);
        }
        return "redirect:/admin/appointments";
    }
}