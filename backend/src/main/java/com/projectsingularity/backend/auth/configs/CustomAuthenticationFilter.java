package com.projectsingularity.backend.auth.configs;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            JsonNode node = objectMapper.readTree(request.getInputStream());
            String username = node.get("email").asText();
            String password = node.get("password").asText();

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username,
                    password);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Error reading authentication request", e);
        }
    }
}