package com.projectsingularity.backend.auth.controllers;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.projectsingularity.backend.auth.dtos.RegisterDTO;

import com.projectsingularity.backend.auth.services.AuthService;
import com.projectsingularity.backend.globalutils.ResponseHandler;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        try {
            authService.registerUser(registerDTO);
            return ResponseHandler.createResponse("Registered Successfully! Check Email", HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseHandler.createResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("verify")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseHandler.createResponse("Email Verified Successfully!", HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("test")
    public String test() {
        return "Test";
    }

}
