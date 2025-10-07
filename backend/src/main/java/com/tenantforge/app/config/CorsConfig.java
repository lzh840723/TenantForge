package com.tenantforge.app.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Basic CORS configuration allowing frontend to call the backend APIs.
 * Allowed origins are configurable via property 'app.cors.allowed-origins'.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // Allow any header/method by default; restrict via property when needed
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.addAllowedHeader("*");
        cfg.setAllowCredentials(false);
        if (allowedOrigins == null || allowedOrigins.isEmpty() || (allowedOrigins.size() == 1 && "*".equals(allowedOrigins.get(0)))) {
            cfg.addAllowedOriginPattern("*");
        } else {
            cfg.setAllowedOrigins(allowedOrigins);
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
