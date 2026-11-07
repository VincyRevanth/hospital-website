package com.syamalahomeo.hospital.security;

import com.syamalahomeo.hospital.entity.AdminUser;
import com.syamalahomeo.hospital.repository.AdminUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

public class AdminLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final LoginAttemptService attempts;
    private final AdminUserRepository users;

    public AdminLoginSuccessHandler(LoginAttemptService attempts, AdminUserRepository users) {
        this.attempts = attempts;
        this.users = users;
        setDefaultTargetUrl("/admin");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        attempts.reset(request.getRemoteAddr(), authentication.getName());

        boolean mustChange = users.findByUsername(authentication.getName())
                .map(AdminUser::isMustChangePassword).orElse(false);
        if (mustChange) {
            request.getSession().setAttribute(PasswordChangeRequiredFilter.ATTRIBUTE, Boolean.TRUE);
            getRedirectStrategy().sendRedirect(request, response, "/admin/password");
            return;
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}