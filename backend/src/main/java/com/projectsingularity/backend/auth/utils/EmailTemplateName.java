package com.projectsingularity.backend.auth.utils;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    VERIFY_EMAIL("verify_email"),
    RESET_PASSWORD("reset_password");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}