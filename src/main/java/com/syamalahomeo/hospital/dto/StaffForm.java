package com.syamalahomeo.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StaffForm {

    @NotBlank(message = "Please enter a username.")
    @Pattern(regexp = "^[a-z0-9._-]{3,50}$",
             message = "Username must be 3 to 50 characters: lowercase letters, digits, . _ - only.")
    private String username;

    @NotBlank(message = "Please enter a temporary password.")
    @Size(min = 12, max = 72, message = "The password must be 12 to 72 characters.")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}