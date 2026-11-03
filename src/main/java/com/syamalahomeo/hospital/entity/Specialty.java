package com.syamalahomeo.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "specialty")
public class Specialty extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    protected Specialty() { }

    public Specialty(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}