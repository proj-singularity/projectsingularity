package com.projectsingularity.server.auth.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;

}
