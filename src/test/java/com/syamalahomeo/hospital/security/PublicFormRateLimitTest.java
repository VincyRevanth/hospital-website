package com.syamalahomeo.hospital.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "ratelimit.public-form.max-requests=3")
@ActiveProfiles("test")
class PublicFormRateLimitTest {

    @Autowired private WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static RequestPostProcessor fromIp(String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }

    @Test
    void fourthPostFromSameIpIsBlocked() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/contact").with(csrf()).with(fromIp("10.1.1.1")).param("name", ""))
                    .andExpect(status().isOk());
        }
        mvc.perform(post("/contact").with(csrf()).with(fromIp("10.1.1.1")).param("name", ""))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void otherIpIsNotAffected() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/contact").with(csrf()).with(fromIp("10.2.2.2")).param("name", ""));
        }
        mvc.perform(post("/contact").with(csrf()).with(fromIp("10.3.3.3")).param("name", ""))
                .andExpect(status().isOk());
    }
}