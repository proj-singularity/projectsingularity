package com.projectsingularity.backend.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectsingularity.backend.auth.entities.User;
import com.projectsingularity.backend.globalutils.ResponseHandler;
import com.projectsingularity.backend.user.dtos.PasswordChangeDto;

import com.projectsingularity.backend.user.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/changepassword")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDto passwordChangeDto,
            @AuthenticationPrincipal User principal, HttpServletRequest request, HttpServletResponse response) {
        try {
            userService.changePassword(passwordChangeDto, principal);

            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
            return ResponseHandler.createResponse("Change Success!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.createResponse(e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

}
