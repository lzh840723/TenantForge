package com.tenantforge.app.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "checkedAt", Instant.now().toString());
    }

    @RequestMapping(method = RequestMethod.HEAD, path = "/api/health")
    public ResponseEntity<Void> healthProbe() {
        return ResponseEntity.ok().build();
    }
}
