package com.projectsingularity.backend.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDto {
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirmation;
}
