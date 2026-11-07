package com.syamalahomeo.hospital.security;

import com.syamalahomeo.hospital.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.time.Duration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String CSP = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self'",
            "img-src 'self' data:",
            "font-src 'self'",
            "connect-src 'self'",
            "object-src 'none'",
            "frame-ancestors 'none'",
            "form-action 'self'",
            "base-uri 'self'");

    @Bean
    public RateLimiter publicFormRateLimiter(
            @Value("${ratelimit.public-form.max-requests:15}") int max,
            @Value("${ratelimit.public-form.window-minutes:15}") int minutes) {
        return new RateLimiter(max, Duration.ofMinutes(minutes));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, LoginAttemptService attempts,
                                           AdminUserRepository adminUsers,
                                           RateLimiter publicFormRateLimiter,
                                           @Value("${security.hsts-max-age-seconds:300}") long hstsSeconds)
            throws Exception {

        RequestMatcher publicAppointmentApi = request ->
                "POST".equalsIgnoreCase(request.getMethod())
                        && "/api/appointments".equals(request.getRequestURI());

        RequestMatcher apiRequest = request -> request.getRequestURI().startsWith("/api/");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                // Pages both roles may use
                .requestMatchers("/admin", "/admin/", "/admin/password").hasAnyRole("ADMIN", "STAFF")
                // Patient pages: staff may register, search, consult and print; only admin edits or deletes
                .requestMatchers(HttpMethod.POST, "/admin/patients/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/admin/patients/*/edit").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/patients/*/delete").hasRole("ADMIN")
                .requestMatchers("/admin/patients", "/admin/patients/**").hasAnyRole("ADMIN", "STAFF")
                // Everything else administrative is admin only
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll())
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .successHandler(new AdminLoginSuccessHandler(attempts, adminUsers))
                .failureHandler((request, response, exception) -> {
                    attempts.recordFailure(request.getRemoteAddr(), request.getParameter("username"));
                    response.sendRedirect(request.getContextPath() + "/admin/login?error");
                })
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"))
            .csrf(csrf -> csrf.ignoringRequestMatchers(publicAppointmentApi))
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiRequest)
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/admin/login"),
                    new NegatedRequestMatcher(apiRequest)))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                .referrerPolicy(ref -> ref.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .httpStrictTransportSecurity(hsts -> hsts
                        .maxAgeInSeconds(hstsSeconds)
                        .includeSubDomains(false))
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=(), payment=()"))
                .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin")))
            .addFilterBefore(new PublicFormRateLimitFilter(publicFormRateLimiter), CsrfFilter.class)
            .addFilterBefore(new LoginRateLimitFilter(attempts), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new PasswordChangeRequiredFilter(), AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}