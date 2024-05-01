package com.projectsingularity.backend.auth.controllers;


import com.projectsingularity.backend.auth.dtos.RegisterDTO;
import com.projectsingularity.backend.auth.services.AuthService;
import com.projectsingularity.backend.globalutils.ResponseHandler;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) throws MessagingException {
        var response = authService.register(registerDTO);
        return ResponseHandler.generateResponse((String) response.getBody(), (HttpStatus) response.getStatusCode());
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) throws MessagingException {
        var response = authService.verifyEmail(token);
        return ResponseHandler.generateResponse((String) response.getBody(), (HttpStatus) response.getStatusCode());
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
