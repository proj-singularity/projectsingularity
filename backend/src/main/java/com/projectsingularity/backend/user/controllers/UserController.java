package com.projectsingularity.backend.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.globalutils.ApiResponse;

import com.projectsingularity.backend.user.dtos.PasswordChangeDto;
import com.projectsingularity.backend.user.dtos.RegisterDTO;
import com.projectsingularity.backend.user.dtos.UserResponse;
import com.projectsingularity.backend.user.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("register")
    public ApiResponse register(@Valid @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        try {
            userService.registerUser(registerDTO);
            return new ApiResponse("User Created", true, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ApiResponse(e.getMessage(), false, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("me")
    public ResponseEntity<UserResponse> getSession(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getSession(request));
    }

    @GetMapping("verify")
    public ResponseEntity<ApiResponse> verify(@RequestParam("token") String token) {
        return userService.verifyEmail(token);
    }

    @PostMapping("/changepassword")
    public ApiResponse changePassword(@RequestBody PasswordChangeDto passwordChangeDto,
            @AuthenticationPrincipal User principal, HttpServletRequest request, HttpServletResponse response) {
        try {
            userService.changePassword(passwordChangeDto, principal);

            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            return new ApiResponse("Password Changed", true, HttpStatus.OK);
        } catch (Exception e) {
            return new ApiResponse(e.getMessage(), false, HttpStatus.BAD_REQUEST);
        }
    }

}
