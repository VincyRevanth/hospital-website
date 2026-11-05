package com.syamalahomeo.hospital.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.syamalahomeo.hospital.repository.DoctorRepository;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentWebTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private DoctorRepository doctors;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private Long doctorId() {
        return doctors.findBySlugAndActiveTrue("abburi-aruna").orElseThrow().getId();
    }

    private String futureDate() {
        return LocalDate.now().plusDays(3).toString();
    }

    @Test
    void formPageLoads() throws Exception {
        mvc.perform(get("/appointments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointment-form"));
    }

    @Test
    void invalidFormShowsErrors() throws Exception {
        mvc.perform(post("/appointments").param("patientName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("appointment-form"))
                .andExpect(model().attributeHasFieldErrors("appointmentRequest", "patientName"));
    }

    @Test
    void validFormRedirectsToReceivedPage() throws Exception {
        mvc.perform(post("/appointments")
                        .param("patientName", "Test Patient")
                        .param("phone", "9876543210")
                        .param("doctorId", String.valueOf(doctorId()))
                        .param("preferredDate", futureDate())
                        .param("preferredTime", "11:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments/received"));
    }

    @Test
    void apiCreatesPendingAppointment() throws Exception {
        String json = """
                {"patientName":"Test Patient","phone":"9876543210","doctorId":%d,
                 "preferredDate":"%s","preferredTime":"12:00"}
                """.formatted(doctorId(), futureDate());
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void apiRejectsInvalidBody() throws Exception {
        mvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}