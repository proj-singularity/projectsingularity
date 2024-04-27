package com.projectsingularity.backend.auth.services;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    VERIFY_EMAIL("verify_email");


    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}