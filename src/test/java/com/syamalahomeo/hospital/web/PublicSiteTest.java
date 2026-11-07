package com.syamalahomeo.hospital.web;

import com.syamalahomeo.hospital.repository.ContactMessageRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
class PublicSiteTest {

    @Autowired private WebApplicationContext context;
    @Autowired private DoctorRepository doctors;
    @Autowired private ContactMessageRepository messages;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String body(String path) throws Exception {
        return mvc.perform(get(path)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    // ---- REST API ----

    @Test
    void apiListsActiveDoctors() throws Exception {
        mvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].slug").exists());
    }

    @Test
    void apiReturnsOneDoctor() throws Exception {
        Long id = doctors.findBySlugAndActiveTrue("abburi-aruna").orElseThrow().getId();
        mvc.perform(get("/api/doctors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr Abburi Aruna"));
    }

    @Test
    void apiUnknownDoctorIs404WithJsonError() throws Exception {
        mvc.perform(get("/api/doctors/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void apiBadIdIs400() throws Exception {
        mvc.perform(get("/api/doctors/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void apiListsServices() throws Exception {
        mvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---- pages and SEO ----

    @Test
    void publicPagesLoad() throws Exception {
        for (String path : new String[]{"/", "/about", "/treatments", "/doctors", "/faq", "/privacy", "/contact"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void homePageHasCanonicalOpenGraphAndStructuredData() throws Exception {
        String html = body("/");
        assertTrue(html.contains("rel=\"canonical\""));
        assertTrue(html.contains("og:title"));
        assertTrue(html.contains("application/ld+json"));
        assertTrue(html.contains("\"@type\":\"Hospital\""));
    }

    @Test
    void robotsTxtBlocksAdminAndPointsToSitemap() throws Exception {
        String text = body("/robots.txt");
        assertTrue(text.contains("Disallow: /admin"));
        assertTrue(text.contains("Sitemap:"));
    }

    @Test
    void sitemapListsPagesAndDoctors() throws Exception {
        String xml = body("/sitemap.xml");
        assertTrue(xml.contains("<urlset"));
        assertTrue(xml.contains("/doctors/abburi-aruna"));
    }

    // ---- contact form ----

    @Test
    void contactFormSavesMessage() throws Exception {
        mvc.perform(post("/contact").with(csrf())
                        .param("name", "Test Person").param("email", "test@example.com")
                        .param("message", "Hello")
                        .param("consent", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact?sent"));
        assertEquals(1, messages.count());
    }

    @Test
    void contactFormNeedsPhoneOrEmail() throws Exception {
        mvc.perform(post("/contact").with(csrf())
                        .param("name", "Test Person").param("message", "Hello")
                        .param("consent", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeHasErrors("contactRequest"));
        assertEquals(0, messages.count());
    }

    @Test
    void contactHoneypotSavesNothing() throws Exception {
        mvc.perform(post("/contact").with(csrf())
                        .param("name", "Bot").param("email", "bot@example.com")
                        .param("message", "Spam").param("website", "http://spam.example"))
                .andExpect(status().is3xxRedirection());
        assertEquals(0, messages.count());
    }

    @Test
    void anonymousCannotReadMessages() throws Exception {
        mvc.perform(get("/admin/messages")).andExpect(status().is3xxRedirection());
    }

    @Test
    void adminCanReadMessages() throws Exception {
        mvc.perform(get("/admin/messages").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}