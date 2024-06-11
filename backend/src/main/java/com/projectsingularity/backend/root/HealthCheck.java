package com.projectsingularity.backend.root;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping("/csrftest")
    public String postMethodName(@RequestBody String entity) {

        return entity;
    }
}
