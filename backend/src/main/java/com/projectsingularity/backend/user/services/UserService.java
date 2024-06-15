package com.projectsingularity.backend.user.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.user.dtos.PasswordChangeDto;
import com.projectsingularity.backend.user.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void changePassword(PasswordChangeDto passwordChangeDto, User user) {

        if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getNewPasswordConfirmation())) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);
    }
}
