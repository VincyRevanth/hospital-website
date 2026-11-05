package com.syamalahomeo.hospital.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@ConfigurationProperties(prefix = "appointment")
public class AppointmentProperties {

    private LocalTime earliestTime = LocalTime.of(9, 0);
    private LocalTime latestTime = LocalTime.of(17, 30);
    private int slotMinutes = 30;
    private int maxDaysAhead = 60;
    private String timezone = "Asia/Kolkata";

    public LocalTime getEarliestTime() { return earliestTime; }
    public void setEarliestTime(LocalTime earliestTime) { this.earliestTime = earliestTime; }
    public LocalTime getLatestTime() { return latestTime; }
    public void setLatestTime(LocalTime latestTime) { this.latestTime = latestTime; }
    public int getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(int slotMinutes) { this.slotMinutes = slotMinutes; }
    public int getMaxDaysAhead() { return maxDaysAhead; }
    public void setMaxDaysAhead(int maxDaysAhead) { this.maxDaysAhead = maxDaysAhead; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}