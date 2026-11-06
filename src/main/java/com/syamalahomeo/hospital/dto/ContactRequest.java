package com.syamalahomeo.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactRequest {

    @NotBlank(message = "Please enter your name.")
    @Size(max = 100, message = "The name must be 100 characters or fewer.")
    private String name;

    @Pattern(regexp = "^$|^(\\+91[\\- ]?|0)?[6-9]\\d{9}$",
             message = "Please enter a valid 10-digit Indian mobile number.")
    private String phone;

    @Email(message = "Please enter a valid email address.")
    @Size(max = 150, message = "The email must be 150 characters or fewer.")
    private String email;

    @NotBlank(message = "Please enter your message.")
    @Size(max = 1000, message = "The message must be 1000 characters or fewer.")
    private String message;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}