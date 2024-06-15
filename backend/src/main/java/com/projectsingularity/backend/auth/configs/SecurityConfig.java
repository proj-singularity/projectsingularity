package com.projectsingularity.backend.auth.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectsingularity.backend.auth.entities.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@EnableRedisHttpSession
public class SecurityConfig {

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
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
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                .csrf((csrf) -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                                                .ignoringRequestMatchers("/api/auth/**", "/health"))
                                .addFilterAfter(new CsrfCookieFilter(), customUsernamePasswordAuthFilter.getClass())

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
                                                .invalidateHttpSession(true)

                                                .deleteCookies("SESSION")
                                                .permitAll())
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        Authentication auth = SecurityContextHolder.getContext()
                                                                        .getAuthentication();
                                                        if (auth != null && auth.isAuthenticated()) {
                                                                User user = (User) auth.getPrincipal();

                                                                if (!user.isOnboardingComplete()) {
                                                                        response.sendRedirect("/onboarding");
                                                                        return;
                                                                }
                                                        }
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

final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
                this.delegate.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {

                if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
                        return super.resolveCsrfTokenValue(request, csrfToken);

                }

                return this.delegate.resolveCsrfTokenValue(request, csrfToken);
        }
}

final class CsrfCookieFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                        @NonNull FilterChain filterChain)
                        throws ServletException, IOException {
                CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

                csrfToken.getToken();

                filterChain.doFilter(request, response);
        }
}