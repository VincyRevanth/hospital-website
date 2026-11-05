package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.repository.DoctorRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminCatalogTest {

    @Autowired private WebApplicationContext context;
    @Autowired private DoctorRepository doctors;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymousCannotOpenAdminCatalogPages() throws Exception {
        mvc.perform(get("/admin/doctors")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/services")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/settings")).andExpect(status().is3xxRedirection());
    }

    @Test
    void nonAdminRoleIsForbidden() throws Exception {
        mvc.perform(get("/admin/settings").with(user("clerk").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesDoctor() throws Exception {
        mvc.perform(post("/admin/doctors").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("fullName", "Dr Test Person")
                        .param("qualification", "B.H.M.S")
                        .param("displayOrder", "5")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/doctors"));
        assertTrue(doctors.findBySlugAndActiveTrue("test-person").isPresent());
    }

    @Test
    void invalidDoctorFormShowsErrors() throws Exception {
        mvc.perform(post("/admin/doctors").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("fullName", "").param("qualification", "").param("displayOrder", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/doctor-form"))
                .andExpect(model().attributeHasFieldErrors("form", "fullName", "qualification"));
    }

    @Test
    void deactivatedDoctorDisappearsFromPublicList() throws Exception {
        Long id = doctors.findBySlugAndActiveTrue("abburi-aruna").orElseThrow().getId();
        mvc.perform(post("/admin/doctors/{id}", id).with(user("admin").roles("ADMIN")).with(csrf())
                        .param("fullName", "Dr Abburi Aruna")
                        .param("qualification", "B.H.M.S")
                        .param("displayOrder", "1")
                        .param("_active", "on"))
                .andExpect(status().is3xxRedirection());
        assertTrue(doctors.findBySlugAndActiveTrue("abburi-aruna").isEmpty());
    }

    @Test
    void adminCreatesServiceAndDuplicateNameIsRefused() throws Exception {
        mvc.perform(post("/admin/services").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("name", "Consultation").param("displayOrder", "1").param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        mvc.perform(post("/admin/services").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("name", "consultation").param("displayOrder", "2").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/service-form"))
                .andExpect(model().attributeHasFieldErrors("form", "name"));
    }

    @Test
    void settingsUpdateShowsOnContactPage() throws Exception {
        mvc.perform(post("/admin/settings").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("phone", "+91 79811 01666")
                        .param("email", "info@example.com")
                        .param("address", "Test address line")
                        .param("openingHours", "Mon-Sat test hours")
                        .param("mapUrl", ""))
                .andExpect(status().is3xxRedirection());

        String html = mvc.perform(get("/contact")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Test address line"));
        assertTrue(html.contains("Mon-Sat test hours"));
    }

    @Test
    void unsafeMapLinkIsRejected() throws Exception {
        mvc.perform(post("/admin/settings").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("mapUrl", "javascript:alert(1)"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/settings"))
                .andExpect(model().attributeHasFieldErrors("form", "mapUrl"));
    }
}