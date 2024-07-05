package com.projectsingularity.backend.auth.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;

import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
@EnableRedisHttpSession
public class SecurityConfig {
        private final CustomAuthenticationSuccessHandler successHandler;
        private final CustomAuthenticationFailureHandler failureHandler;
        private final CorsConfigurationSource corsConfigurationSource;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                CustomAuthenticationFilter customUsernamePasswordAuthFilter = new CustomAuthenticationFilter();

                customUsernamePasswordAuthFilter.setFilterProcessesUrl("/api/auth/login");
                customUsernamePasswordAuthFilter.setAuthenticationManager(
                                authenticationConfiguration.getAuthenticationManager());
                customUsernamePasswordAuthFilter.setAuthenticationSuccessHandler(successHandler);
                customUsernamePasswordAuthFilter.setAuthenticationFailureHandler(failureHandler);

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                                .csrf((csrf) -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                                                .ignoringRequestMatchers("/api/user/register", "api/auth/login",
                                                                "api/user/verify", "/health"))
                                .addFilterBefore(customUsernamePasswordAuthFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(new CsrfCookieFilter(), customUsernamePasswordAuthFilter.getClass())
                                .addFilterAfter(new OnboardingCheckFilter(),
                                                customUsernamePasswordAuthFilter.getClass())
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers("/api/auth/login").permitAll()
                                                .requestMatchers("/api/user/register").permitAll()
                                                .requestMatchers("/api/user/verify").permitAll()
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
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                                .sessionManagement(s -> s
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation().migrateSession()
                                                .invalidSessionUrl("http://localhost:3000/login")
                                                .sessionAuthenticationErrorUrl("http://localhost:3000/login")
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(true)
                                                .expiredUrl("http://localhost:3000/login"));
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