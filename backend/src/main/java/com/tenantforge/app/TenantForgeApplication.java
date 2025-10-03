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
        String portEnv = System.getenv("PORT");
        String serverPortProp = System.getProperty("server.port");
        String runtimePort = portEnv != null ? portEnv : serverPortProp;
        log.info("Detected PORT env: {}", portEnv);
        log.info("Effective server.port property: {}", serverPortProp);
        log.info("Runtime listening port resolved to: {}", runtimePort);
        SpringApplication.run(TenantForgeApplication.class, args);
    }
}
