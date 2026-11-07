package com.syamalahomeo.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_user")
public class AdminUser extends BaseEntity {

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String role = "ROLE_ADMIN";

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    protected AdminUser() { }

    public AdminUser(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /** A staff account starts with a temporary password that must be changed at first sign-in. */
    public static AdminUser staff(String username, String passwordHash) {
        AdminUser user = new AdminUser(username, passwordHash);
        user.role = "ROLE_STAFF";
        user.mustChangePassword = true;
        return user;
    }

    public void changePassword(String newHash) {
        this.passwordHash = newHash;
        this.mustChangePassword = false;
    }

    public void resetPassword(String temporaryHash) {
        this.passwordHash = temporaryHash;
        this.mustChangePassword = true;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}