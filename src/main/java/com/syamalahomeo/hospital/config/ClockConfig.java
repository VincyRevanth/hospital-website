package com.syamalahomeo.hospital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(AppointmentProperties props) {
        return Clock.system(ZoneId.of(props.getTimezone()));
    }
}