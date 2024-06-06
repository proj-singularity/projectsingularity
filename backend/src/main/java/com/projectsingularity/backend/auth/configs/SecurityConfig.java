package com.projectsingularity.backend.auth.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@EnableRedisHttpSession
public class SecurityConfig {

        @Bean
        public CorsFilter corsFilter() {
                final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                final CorsConfiguration config = new CorsConfiguration();
                config.setAllowCredentials(true);
                config.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
                config.setAllowedHeaders(Arrays.asList(
                                HttpHeaders.ORIGIN,
                                HttpHeaders.CONTENT_TYPE,
                                HttpHeaders.ACCEPT,
                                HttpHeaders.AUTHORIZATION));
                config.setAllowedMethods(Arrays.asList(
                                "GET",
                                "POST",
                                "DELETE",
                                "PUT",
                                "PATCH"));
                source.registerCorsConfiguration("/**", config);
                return new CorsFilter(source);

        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                UsernamePasswordAuthenticationFilter customUsernamePasswordAuthFilter = new UsernamePasswordAuthenticationFilter() {

                        private final ObjectMapper objectMapper = new ObjectMapper();

                        @Override
                        public Authentication attemptAuthentication(HttpServletRequest request,
                                        HttpServletResponse response)
                                        throws AuthenticationException {
                                try {
                                        JsonNode node = objectMapper.readTree(request.getInputStream());
                                        String username = node.get("email").textValue();
                                        String password = node.get("password").textValue();

                                        request.setAttribute("username", username);
                                        request.setAttribute("password", password);
                                } catch (IOException e) {
                                        throw new AuthenticationServiceException("ERROR READING REQUEST", e);
                                }

                                return super.attemptAuthentication(request, response);
                        }

                        @Override
                        protected String obtainUsername(HttpServletRequest request) {
                                return (String) request.getAttribute("username");
                        }

                        @Override
                        protected String obtainPassword(HttpServletRequest request) {
                                return (String) request.getAttribute("password");
                        }
                };
                System.out.println(authenticationConfiguration.getAuthenticationManager());
                customUsernamePasswordAuthFilter.setFilterProcessesUrl("/api/auth/login");
                customUsernamePasswordAuthFilter.setAuthenticationManager(
                                authenticationConfiguration.getAuthenticationManager());

                customUsernamePasswordAuthFilter
                                .setAuthenticationSuccessHandler((request, response, authentication) -> {
                                        response.setStatus(200);
                                        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                                        response.getWriter().write("{\"success\": true}");
                                        response.getWriter().flush();
                                });

                customUsernamePasswordAuthFilter.setAuthenticationFailureHandler(
                                (request, response, exception) -> {
                                        response.setStatus(401);
                                        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                                        response.getWriter().write("{\"success\": false}");
                                        response.getWriter().flush();
                                });
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/health").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(formLogin -> formLogin.disable())
                                .securityContext(context -> context.requireExplicitSave(false))
                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        response.setStatus(HttpServletResponse.SC_OK);
                                                        response.setHeader(HttpHeaders.CONTENT_TYPE,
                                                                        "application/json");
                                                        response.getWriter().write("{\"success\": true}");
                                                        response.getWriter().flush();
                                                })
                                                .invalidateHttpSession(true) // specifies that the HttpSession should be
                                                                             // invalidated when the user logs out
                                                .deleteCookies("SESSION")
                                                .permitAll())
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"message\": \"You need to log in first.\"}");
                                                        response.getWriter().flush();
                                                }))
                                .sessionManagement(s -> s
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation().migrateSession()
                                                .invalidSessionUrl("http://localhost:5173/login")
                                                .sessionAuthenticationErrorUrl("http://localhost:5173/login")
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(true)
                                                .expiredUrl("http://localhost:5173/login"));

                http.addFilterBefore(customUsernamePasswordAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
