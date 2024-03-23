package com.projectsingularity.server.auth.dtos;

import lombok.Data;

@Data
public class SignUpRequest {
    private String username;
    private String password;
    private String email;
}
