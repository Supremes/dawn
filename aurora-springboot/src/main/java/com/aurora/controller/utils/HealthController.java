package com.aurora.controller.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;
    @GetMapping("/health")
    public String checkHeath() {
        if (healthEndpoint != null) {
            return "health endpoint exists";
        }
        return "no exist";
    }
}
