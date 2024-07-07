package com.projectsingularity.backend.user.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.apache.catalina.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectsingularity.backend.auth.entities.Token;
import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.auth.repositories.TokenRepository;
import com.projectsingularity.backend.auth.services.EmailService;
import com.projectsingularity.backend.auth.utils.EmailTemplateName;
import com.projectsingularity.backend.globalutils.ApiResponse;
import com.projectsingularity.backend.user.dtos.PasswordChangeDto;
import com.projectsingularity.backend.user.dtos.RegisterDTO;
import com.projectsingularity.backend.user.dtos.UserResponse;
import com.projectsingularity.backend.user.repositories.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final EmailService emailService;

    @Autowired
    private final TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    public ApiResponse changePassword(PasswordChangeDto passwordChangeDto, User user) {
        try {

            if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Old password is incorrect");
            }
            if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getNewPasswordConfirmation())) {
                throw new IllegalArgumentException("New passwords do not match");
            }
            user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
            userRepository.save(user);
            return new ApiResponse("Password changed successfully", true, HttpStatus.OK);
        } catch (Exception e) {
            return new ApiResponse(e.getMessage(), false, HttpStatus.BAD_REQUEST);
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

    @Transactional
    public UserResponse getSession(HttpServletRequest request) {
        User user = com.projectsingularity.backend.auth.utils.SecurityUtil.getAuthenticatedUser();
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();
        return new UserResponse(user, authorities);
    }

    @Transactional
    public ResponseEntity<ApiResponse> verifyEmail(String token) {
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

            return ResponseEntity
                    .ok(new ApiResponse(
                            "Hurrah! Email verified successfully :) Please log in now",
                            true, HttpStatus.OK));
        } catch (RuntimeException e) {
            return new ResponseEntity<ApiResponse>(new ApiResponse(e.getMessage(), false, HttpStatus.BAD_REQUEST),
                    HttpStatus.BAD_REQUEST);
        } catch (MessagingException e) {
            return new ResponseEntity<ApiResponse>(new ApiResponse(e.getMessage(), false, HttpStatus.BAD_REQUEST),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // private void sendPasswordResetEmail(User user, String resetCode) throws
    // MessagingException {

    // emailService.sendEmail(
    // user.getEmail(),
    // user.getFullName(),
    // EmailTemplateName.RESET_PASSWORD,
    // "http://localhost:5173/confirm-reset?token=" + resetCode,
    // resetCode,
    // "Password Reset");
    // }
}
