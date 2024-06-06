package com.projectsingularity.backend.auth.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectsingularity.backend.auth.dtos.RegisterDTO;
import com.projectsingularity.backend.auth.entities.Token;
import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.auth.repositories.TokenRepository;
import com.projectsingularity.backend.auth.repositories.UserRepository;
import com.projectsingularity.backend.auth.utils.EmailTemplateName;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("No user found with email: " + email);
        }

        return user;
    }

    public User registerUser(RegisterDTO registerDTO) throws MessagingException {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("User with email " + registerDTO.getEmail() + " already exists");
        } else {
            User user = new User();
            user.setEmail(registerDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.setFirstName(registerDTO.getFirstName());
            user.setLastName(registerDTO.getLastName());
            user.setRole("ROLE_USER");
            user.setEnabled(false);
            userRepository.save(user);
            sendVerificationEmail(user);

            return user;
        }
    }

    @Transactional
    public ResponseEntity<?> verifyEmail(String token) {
        try {
            Token savedToken = tokenRepository.findByToken(token)
                    .orElseThrow(
                            () -> new RuntimeException("Invalid token :( Redirecting you to the signup page shortly"));

            if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
                sendVerificationEmail(savedToken.getUser());
                throw new RuntimeException(
                        "Verification token has expired. A new token has been sent to the same email address");
            }

            Optional<User> user = Optional.ofNullable(userRepository.findById(savedToken.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found")));

            user.get().setEnabled(true);
            userRepository.save(user.get());

            savedToken.setValidatedAt(LocalDateTime.now());
            tokenRepository.save(savedToken);

            return new ResponseEntity<>("Hurrah! Email verified successfully :) Redirecting you shortly",
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Error sending email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendVerificationEmail(User user) throws MessagingException {
        String verificationCode = saveVerificationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.VERIFY_EMAIL,
                "http://localhost:5000/verification?token=" + verificationCode,
                verificationCode,
                "Account Verification");
    }

    private String saveVerificationToken(User user) {
        String generatedToken = generateVerificationToken();

        Token token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }

    private String generateVerificationToken() {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < 6; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }
}
