package com.syamalahomeo.hospital.security;

import com.syamalahomeo.hospital.dto.AppointmentRequest;
import com.syamalahomeo.hospital.entity.AdminUser;
import com.syamalahomeo.hospital.entity.AppointmentStatus;
import com.syamalahomeo.hospital.repository.AdminUserRepository;
import com.syamalahomeo.hospital.repository.AppointmentRepository;
import com.syamalahomeo.hospital.repository.DoctorRepository;
import com.syamalahomeo.hospital.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminSecurityTest {

    private static final String PASSWORD = "Correct-Horse-42";

    @Autowired private WebApplicationContext context;
    @Autowired private AdminUserRepository admins;
    @Autowired private PasswordEncoder encoder;
    @Autowired private LoginAttemptService attempts;
    @Autowired private AppointmentService appointmentService;
    @Autowired private AppointmentRepository appointments;
    @Autowired private DoctorRepository doctors;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        attempts.reset("127.0.0.1", "tester");
        if (admins.findByUsername("tester").isEmpty()) {
            admins.save(new AdminUser("tester", encoder.encode(PASSWORD)));
        }
    }

    private Long newRequest(String phone, String time) {
        AppointmentRequest r = new AppointmentRequest();
        r.setPatientName("Test Patient");
        r.setPhone(phone);
        r.setDoctorId(doctors.findBySlugAndActiveTrue("abburi-aruna").orElseThrow().getId());
        r.setPreferredDate(appointmentService.minDate().plusDays(2));
        r.setPreferredTime(time);
        return appointmentService.submit(r).reference();
    }

    @Test
    void anonymousAdminPageRedirectsToLogin() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void anonymousAdminApiGets401() throws Exception {
        mvc.perform(get("/api/admin/appointments")).andExpect(status().isUnauthorized());
    }

    @Test
    void publicPagesStayPublic() throws Exception {
        mvc.perform(get("/doctors")).andExpect(status().isOk());
        mvc.perform(get("/appointments/new")).andExpect(status().isOk());
    }

    @Test
    void adminCanOpenDashboardAndList() throws Exception {
        mvc.perform(get("/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/appointments").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void nonAdminRoleIsForbidden() throws Exception {
        mvc.perform(get("/admin").with(user("clerk").roles("USER"))).andExpect(status().isForbidden());
    }

    @Test
    void postWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/admin/appointments/1/confirm").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConfirmsThenCancels() throws Exception {
        Long id = newRequest("9876543210", "10:00");

        mvc.perform(post("/admin/appointments/{id}/confirm", id)
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/appointments"));
        assertEquals(AppointmentStatus.CONFIRMED, appointments.findById(id).orElseThrow().getStatus());

        mvc.perform(post("/admin/appointments/{id}/cancel", id)
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(AppointmentStatus.CANCELLED, appointments.findById(id).orElseThrow().getStatus());
    }

    @Test
    void secondConfirmForSameSlotIsRefused() throws Exception {
        Long first = newRequest("9876543210", "10:00");
        Long second = newRequest("9123456789", "10:00");

        mvc.perform(post("/admin/appointments/{id}/confirm", first)
                .with(user("admin").roles("ADMIN")).with(csrf()));
        mvc.perform(post("/admin/appointments/{id}/confirm", second)
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(flash().attributeExists("error"));

        assertEquals(AppointmentStatus.PENDING, appointments.findById(second).orElseThrow().getStatus());
    }

    @Test
    void loginSucceedsWithCorrectPassword() throws Exception {
        mvc.perform(formLogin("/admin/login").user("tester").password(PASSWORD))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void wrongPasswordFails() throws Exception {
        mvc.perform(formLogin("/admin/login").user("tester").password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test
    void logoutWorks() throws Exception {
        mvc.perform(post("/admin/logout").with(user("tester").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?logout"));
    }

    @Test
    void repeatedFailuresLockLogin() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(formLogin("/admin/login").user("tester").password("wrong-" + i));
        }
        mvc.perform(formLogin("/admin/login").user("tester").password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/admin/login?locked"));
    }

    @Test
    void adminApiReturnsList() throws Exception {
        newRequest("9876543210", "10:00");
        mvc.perform(get("/api/admin/appointments").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}