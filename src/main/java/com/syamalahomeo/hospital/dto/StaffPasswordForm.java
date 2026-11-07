package com.syamalahomeo.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StaffPasswordForm {

    @NotBlank(message = "Please enter a temporary password.")
    @Size(min = 12, max = 72, message = "The password must be 12 to 72 characters.")
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}