package com.syamalahomeo.hospital.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServiceForm {

    @NotBlank(message = "Please enter the service name.")
    @Size(max = 150, message = "The name must be 150 characters or fewer.")
    private String name;

    @Size(max = 2000, message = "The description must be 2000 characters or fewer.")
    private String description;

    @NotNull(message = "Please enter a display order.")
    @Min(value = 0, message = "Display order must be 0 or more.")
    @Max(value = 1000, message = "Display order must be 1000 or less.")
    private Integer displayOrder = 0;

    private boolean active = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}