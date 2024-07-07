package com.projectsingularity.backend.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @GetMapping("/csrf")
    public ResponseEntity<?> csrf() {
        return ResponseEntity.ok().build();
    }
}
