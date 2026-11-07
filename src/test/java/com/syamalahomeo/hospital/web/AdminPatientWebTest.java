package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.repository.PatientRepository;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminPatientWebTest {

    @Autowired private WebApplicationContext context;
    @Autowired private PatientRepository patients;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymousIsSentToLogin() throws Exception {
        mvc.perform(get("/admin/patients")).andExpect(status().is3xxRedirection());
    }

    @Test
    void adminCanOpenPatientList() throws Exception {
        mvc.perform(get("/admin/patients").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void adminRegistersPatientAndPrintsForm() throws Exception {
        mvc.perform(post("/admin/patients").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("fullName", "Print Test Patient").param("ageYears", "40")
                        .param("gender", "MALE").param("phone", "9876501234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/patients/*"));

        Long id = patients.findAll().get(0).getId();
        String html = mvc.perform(get("/admin/patients/{id}/print", id).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Print Test Patient"));
        assertTrue(html.contains("SHH-"));
        assertTrue(html.contains("BP-"));
        assertTrue(html.contains("PR-"));
        assertTrue(html.contains("RR-"));
        assertTrue(html.contains("Temp-"));
        assertTrue(html.contains("SpO2-"));
        assertFalse(html.contains("Rx"));
    }

    @Test
    void invalidFormShowsErrors() throws Exception {
        mvc.perform(post("/admin/patients").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("fullName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/patient-form"))
                .andExpect(model().attributeHasFieldErrors("form", "fullName", "ageYears", "gender", "phone"));
    }

    @Test
    void minorNeedsGuardianName() throws Exception {
        mvc.perform(post("/admin/patients").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("fullName", "Young Patient").param("ageYears", "7")
                        .param("gender", "FEMALE").param("phone", "9876501235"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "guardianName"));
    }

    @Test
    void postWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/admin/patients").with(user("admin").roles("ADMIN"))
                        .param("fullName", "No Token"))
                .andExpect(status().isForbidden());
    }
}