package com.tenantforge.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TenantForgeApplication {

    private static final Logger log = LoggerFactory.getLogger(TenantForgeApplication.class);

    public static void main(String[] args) {
        log.info("Detected PORT env: {}", System.getenv("PORT"));
        log.info("Effective server.port property: {}", System.getProperty("server.port"));
        SpringApplication.run(TenantForgeApplication.class, args);
    }
}
