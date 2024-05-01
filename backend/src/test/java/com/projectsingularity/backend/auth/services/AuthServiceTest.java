package com.projectsingularity.backend.auth.services;

import com.projectsingularity.backend.auth.dtos.RegisterDTO;
import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.auth.entities.Token;
import com.projectsingularity.backend.auth.repositories.TokenRepository;
import com.projectsingularity.backend.auth.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() throws Exception {
        try (AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
        }
    }

    @Test
    @DisplayName("Load user by email.")
    public void testLoadUserByUsername_UserFound() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        assertEquals(user, authService.loadUserByUsername("test@example.com"));
    }

    @Test
    @DisplayName("Throw exception when email does not exist in db.")
    public void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> authService.loadUserByUsername("test@example.com"));
    }

    @Test
    @DisplayName("Throws exception when user already exists.")
    public void testRegister_UserAlreadyExists() throws MessagingException {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(new User());

        ResponseEntity<?> responseEntity = authService.register(registerDTO);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals("User already exists", responseEntity.getBody());
    }

    @Test
    @DisplayName("Register user.")
    public void testRegister_SuccessfulRegistration() throws MessagingException {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("new@example.com");
        registerDTO.setPassword("password");
        registerDTO.setFirstName("John");
        registerDTO.setLastName("Doe");

        when(userRepository.findByEmail("new@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> responseEntity = authService.register(registerDTO);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals("User registered successfully. Please check your email to complete signing up.", responseEntity.getBody());
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    public void testVerifyEmail_ValidToken() throws MessagingException {
        Token token = new Token();
        token.setUser(new User());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(any())).thenReturn(Optional.of(token.getUser()));

        assertDoesNotThrow(() -> authService.verifyEmail("valid_token"));
        assertTrue(token.getUser().isEnabled());
        assertNotNull(token.getValidatedAt());
        verify(tokenRepository, times(1)).save(token);
    }

    @Test
    public void testVerifyEmail_InvalidToken() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.verifyEmail("invalid_token"));
        verifyNoInteractions(userRepository);
    }
}