package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.StaffView;
import com.syamalahomeo.hospital.entity.AdminUser;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminAccountService {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountService.class);
    private static final String STAFF_ROLE = "ROLE_STAFF";

    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    private final DateTimeFormatter format;

    public AdminAccountService(AdminUserRepository users, PasswordEncoder encoder, Clock clock) {
        this.users = users;
        this.encoder = encoder;
        this.format = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(clock.getZone());
    }

    @Transactional(readOnly = true)
    public List<StaffView> listStaff() {
        return users.findByRoleOrderByUsername(STAFF_ROLE).stream()
                .map(u -> new StaffView(u.getId(), u.getUsername(), u.isEnabled(),
                        u.isMustChangePassword(), format.format(u.getCreatedAt())))
                .toList();
    }

    @Transactional(readOnly = true)
    public String staffUsername(Long id) {
        return findStaff(id).getUsername();
    }

    @Transactional
    public void createStaff(String username, String temporaryPassword) {
        String name = username.trim();
        if (users.findByUsername(name).isPresent()) {
            throw new BusinessRuleException("That username is already taken.");
        }
        users.save(AdminUser.staff(name, encoder.encode(temporaryPassword)));
        log.info("Staff account {} created", name);
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        AdminUser user = findStaff(id);
        user.setEnabled(enabled);
        log.info("Staff account {} {}", user.getUsername(), enabled ? "enabled" : "disabled");
    }

    @Transactional
    public void resetPassword(Long id, String temporaryPassword) {
        AdminUser user = findStaff(id);
        user.resetPassword(encoder.encode(temporaryPassword));
        log.info("Password reset for staff account {}", user.getUsername());
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        AdminUser user = users.findByUsername(username)
                .orElseThrow(() -> new BusinessRuleException("Account not found."));
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessRuleException("Your current password is not correct.");
        }
        if (newPassword.equals(currentPassword)) {
            throw new BusinessRuleException("The new password must be different from the current one.");
        }
        if (newPassword.equalsIgnoreCase(username)) {
            throw new BusinessRuleException("The password must not be the same as your username.");
        }
        user.changePassword(encoder.encode(newPassword));
        log.info("Password changed for account {}", username);
    }

    private AdminUser findStaff(Long id) {
        return users.findById(id)
                .filter(u -> STAFF_ROLE.equals(u.getRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}