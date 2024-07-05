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

import com.projectsingularity.backend.auth.entities.Token;
import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.auth.repositories.TokenRepository;
import com.projectsingularity.backend.auth.utils.EmailTemplateName;
import com.projectsingularity.backend.globalutils.ApiResponse;
import com.projectsingularity.backend.user.dtos.RegisterDTO;
import com.projectsingularity.backend.user.repositories.UserRepository;

import jakarta.mail.MessagingException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("No user found with email: " + email);
        }

        return user;
    }

}
