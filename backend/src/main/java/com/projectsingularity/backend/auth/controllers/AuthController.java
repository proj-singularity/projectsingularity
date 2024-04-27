package com.projectsingularity.backend.auth.controllers;


import com.projectsingularity.backend.auth.dtos.RegisterDTO;
import com.projectsingularity.backend.auth.services.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) throws MessagingException {
        return authService.register(registerDTO);
    }

    @GetMapping("/verify")
    public void verify(@RequestParam String token) throws MessagingException {
        authService.verifyEmail(token);
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
