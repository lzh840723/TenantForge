package com.tenantforge.app.controller;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health endpoints for uptime and probes.
 */
@RestController
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    /** Liveness/readiness GET endpoint returning a small JSON payload. */
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        log.info("GET /api/health responded OK");
        return Map.of(
                "status", "UP",
                "checkedAt", Instant.now().toString());
    }

    /** Lightweight HEAD variant used by load balancers or probes. */
    @RequestMapping(method = RequestMethod.HEAD, path = "/api/health")
    public ResponseEntity<Void> healthProbe() {
        log.info("HEAD /api/health responded OK");
        return ResponseEntity.ok().build();
    }
}
