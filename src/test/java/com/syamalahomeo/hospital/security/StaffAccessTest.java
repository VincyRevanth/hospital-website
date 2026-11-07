package com.syamalahomeo.hospital.security;

import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.entity.AdminUser;
import com.syamalahomeo.hospital.repository.AdminUserRepository;
import com.syamalahomeo.hospital.repository.PatientRepository;
import com.syamalahomeo.hospital.service.AdminAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
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
class StaffAccessTest {

    private static final String TEMP = "Temp-Password-1234";
    private static final String NEW_PASSWORD = "Brand-New-Pass-5678";

    @Autowired private WebApplicationContext context;
    @Autowired private AdminAccountService accounts;
    @Autowired private AdminUserRepository users;
    @Autowired private PatientRepository patients;
    @Autowired private LoginAttemptService attempts;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        attempts.reset("127.0.0.1", "staff1");
    }

    private static RequestPostProcessor staff() {
        return user("staff1").roles("STAFF");
    }

    private static RequestPostProcessor admin() {
        return user("admin").roles("ADMIN");
    }

    private Long registerPatientAsStaff() throws Exception {
        mvc.perform(post("/admin/patients").with(staff()).with(csrf())
                        .param("fullName", "Staff Registered").param("ageYears", "30")
                        .param("gender", "FEMALE").param("phone", "9876509999"))
                .andExpect(status().is3xxRedirection());
        return patients.findAll().get(0).getId();
    }

    @Test
    void staffCanRegisterPatientAndPrint() throws Exception {
        Long id = registerPatientAsStaff();
        mvc.perform(get("/admin/patients/{id}/print", id).with(staff())).andExpect(status().isOk());
        mvc.perform(get("/admin/patients").with(staff())).andExpect(status().isOk());
    }

    @Test
    void staffCannotUseAdminPages() throws Exception {
        for (String path : new String[]{"/admin/appointments", "/admin/messages", "/admin/doctors",
                "/admin/services", "/admin/settings", "/admin/users", "/api/admin/appointments"}) {
            mvc.perform(get(path).with(staff())).andExpect(status().isForbidden());
        }
    }

    @Test
    void staffCannotEditOrDeletePatients() throws Exception {
        Long id = registerPatientAsStaff();
        mvc.perform(get("/admin/patients/{id}/edit", id).with(staff())).andExpect(status().isForbidden());
        mvc.perform(post("/admin/patients/{id}", id).with(staff()).with(csrf())
                        .param("fullName", "Changed").param("ageYears", "31")
                        .param("gender", "FEMALE").param("phone", "9876509999"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/patients/{id}/delete", id).with(staff()).with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(patients.findById(id).isPresent());
    }

    @Test
    void staffDashboardRedirectsToPatients() throws Exception {
        mvc.perform(get("/admin").with(staff()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/patients"));
    }

    @Test
    void adminCreatesStaffAccount() throws Exception {
        mvc.perform(post("/admin/users").with(admin()).with(csrf())
                        .param("username", "staff1").param("password", TEMP))
                .andExpect(status().is3xxRedirection());
        AdminUser created = users.findByUsername("staff1").orElseThrow();
        assertEquals("ROLE_STAFF", created.getRole());
        assertTrue(created.isMustChangePassword());
        assertNotEquals(TEMP, created.getPasswordHash());
    }

    @Test
    void staffCannotCreateAccounts() throws Exception {
        mvc.perform(post("/admin/users").with(staff()).with(csrf())
                        .param("username", "another").param("password", TEMP))
                .andExpect(status().isForbidden());
    }

    @Test
    void weakPasswordIsRefused() throws Exception {
        mvc.perform(post("/admin/users").with(admin()).with(csrf())
                        .param("username", "staff1").param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(model().attributeHasFieldErrors("form", "password"));
    }

    @Test
    void duplicateUsernameIsRefused() throws Exception {
        accounts.createStaff("staff1", TEMP);
        mvc.perform(post("/admin/users").with(admin()).with(csrf())
                        .param("username", "staff1").param("password", TEMP))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "username"));
    }

    @Test
    void newStaffMustChangePasswordAtFirstLogin() throws Exception {
        accounts.createStaff("staff1", TEMP);
        mvc.perform(formLogin("/admin/login").user("staff1").password(TEMP))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/admin/password"));
    }

    @Test
    void staffCanLoginAfterChangingPassword() throws Exception {
        accounts.createStaff("staff1", TEMP);
        accounts.changePassword("staff1", TEMP, NEW_PASSWORD);
        mvc.perform(formLogin("/admin/login").user("staff1").password(NEW_PASSWORD))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void wrongCurrentPasswordIsRefused() {
        accounts.createStaff("staff1", TEMP);
        assertThrows(BusinessRuleException.class,
                () -> accounts.changePassword("staff1", "not-the-password", NEW_PASSWORD));
    }

    @Test
    void disabledStaffCannotLogin() throws Exception {
        accounts.createStaff("staff1", TEMP);
        Long id = users.findByUsername("staff1").orElseThrow().getId();
        accounts.setEnabled(id, false);
        mvc.perform(formLogin("/admin/login").user("staff1").password(TEMP))
                .andExpect(unauthenticated());
    }
}