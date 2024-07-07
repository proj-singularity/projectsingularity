package com.projectsingularity.backend.auth.configs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectsingularity.backend.auth.entities.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OnboardingCheckFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            if (!user.isOnboardingComplete() && !request.getRequestURI().startsWith("/api/onboarding")) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("message", "Onboarding required");
                responseBody.put("redirectUrl", "/onboarding");
                response.getWriter().write(objectMapper.writeValueAsString(responseBody));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}