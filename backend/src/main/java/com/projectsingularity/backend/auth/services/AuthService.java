package com.projectsingularity.backend.auth.services;

import com.projectsingularity.backend.auth.dtos.RegisterDTO;
import com.projectsingularity.backend.auth.entities.Token;
import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.auth.repositories.TokenRepository;
import com.projectsingularity.backend.auth.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

//    @Value("${mailing.frontend.verificationUrl}")
//    private String verificationUrl;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        return user;
    }

    // Register
    public ResponseEntity<?> register(RegisterDTO registerDTO) throws MessagingException {
        if (userRepository.findByEmail(registerDTO.getEmail()) != null) {
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .accountLocked(false)
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);

        return new ResponseEntity<>("Registered successfully! Please check your email to complete signing up.", HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> verifyEmail(String token) {
        try {
            Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token :( Redirecting you to the signup page shortly"));
    
            if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
                sendVerificationEmail(savedToken.getUser());
                throw new RuntimeException("Verification token has expired. A new token has been sent to the same email address");
            }
    
            Optional<User> user = Optional.ofNullable(userRepository.findById(savedToken.getUser().getId()).orElseThrow(() -> new RuntimeException("User not found")));
    
            user.get().setEnabled(true);
            userRepository.save(user.get());
    
            savedToken.setValidatedAt(LocalDateTime.now());
            tokenRepository.save(savedToken);
    
            return new ResponseEntity<>("Hurrah! Email verified successfully :) Redirecting you shortly", HttpStatus.OK);
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
                "http://localhost:5173/verification?token=" + verificationCode,
                verificationCode,
                "Account Verification"
        );
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
