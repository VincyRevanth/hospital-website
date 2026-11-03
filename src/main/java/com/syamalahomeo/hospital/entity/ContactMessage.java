package com.syamalahomeo.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact_message")
public class ContactMessage extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 2000)
    private String message;

    protected ContactMessage() { }

    public ContactMessage(String name, String phone, String email, String message) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.message = message;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
}