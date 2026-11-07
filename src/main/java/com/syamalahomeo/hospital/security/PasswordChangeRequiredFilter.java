package com.syamalahomeo.hospital.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Keeps a user with a temporary password inside the change-password page until it is changed. */
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    public static final String ATTRIBUTE = "MUST_CHANGE_PASSWORD";

    private static String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = path(request);
        boolean adminArea = path.startsWith("/admin") || path.startsWith("/api/admin");
        boolean allowed = path.equals("/admin/password") || path.equals("/admin/logout")
                || path.equals("/admin/login");
        return !adminArea || allowed;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute(ATTRIBUTE))) {
            if (path(request).startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/password");
            }
            return;
        }
        chain.doFilter(request, response);
    }
}