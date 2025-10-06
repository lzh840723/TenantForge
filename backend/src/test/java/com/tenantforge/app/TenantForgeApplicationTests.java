package com.tenantforge.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.tenantforge.app.security.TokenPair;
import com.tenantforge.app.service.AuthService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class TenantForgeApplicationTests {

    private static final boolean RUN_REMOTE_IT =
            Optional.ofNullable(System.getenv("RUN_REMOTE_IT"))
                    .map(String::toLowerCase)
                    .map(v -> v.equals("1") || v.equals("true") || v.equals("yes"))
                    .orElse(false);

    private static final Optional<RemoteDbConfig> REMOTE_DB = RUN_REMOTE_IT ? RemoteDbConfig.fromEnv() : Optional.empty();

    @BeforeAll
    static void checkRemoteConfig() {
        assumeTrue(RUN_REMOTE_IT, () -> "Skip remote integration tests (set RUN_REMOTE_IT=true to enable)");
        assumeTrue(REMOTE_DB.isPresent(), () -> "Set DIRECT_CONNECTION or SPRING_DATASOURCE_* env to enable remote IT");
    }

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        RemoteDbConfig dbConfig = REMOTE_DB.orElseThrow();
        registry.add("spring.datasource.url", dbConfig::jdbcUrl);
        registry.add("spring.datasource.username", dbConfig::username);
        registry.add("spring.datasource.password", dbConfig::password);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Test
    void contextLoads(@Autowired Environment environment) {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("tenantforge-backend");
    }

    @Test
    void registerAndLoginRoundtrip(@Autowired AuthService authService) {
        String tenantName = "Tenant-" + UUID.randomUUID();
        String email = "owner-" + UUID.randomUUID() + "@tenantforge.dev";
        String password = "Password123!";

        TokenPair registerTokens = authService.registerTenant(tenantName, email, password, "Primary Owner");
        assertThat(registerTokens.accessToken()).isNotBlank();
        assertThat(registerTokens.refreshToken()).isNotBlank();

        TokenPair loginTokens = authService.login(email, password);
        assertThat(loginTokens.accessToken()).isNotBlank();
        assertThat(loginTokens.refreshToken()).isNotBlank();
    }

    private static final class RemoteDbConfig {
        private final String jdbcUrl;
        private final String username;
        private final String password;

        private RemoteDbConfig(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }

        static Optional<RemoteDbConfig> fromEnv() {
            String jdbcUrl = System.getenv("SPRING_DATASOURCE_URL");
            String username = System.getenv("SPRING_DATASOURCE_USERNAME");
            String password = System.getenv("SPRING_DATASOURCE_PASSWORD");

            if (hasText(jdbcUrl) && hasText(username) && hasText(password)) {
                return Optional.of(new RemoteDbConfig(jdbcUrl, username, password));
            }

            String connection = System.getenv("DIRECT_CONNECTION");
            if (!hasText(connection)) {
                return Optional.empty();
            }
            try {
                URI uri = new URI(connection);
                String userInfo = Optional.ofNullable(uri.getUserInfo()).orElse("");
                int index = userInfo.indexOf(':');
                if (index < 0) {
                    throw new IllegalArgumentException("DIRECT_CONNECTION must include username and password");
                }
                String user = userInfo.substring(0, index);
                String pass = userInfo.substring(index + 1);
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                        .append(uri.getHost())
                        .append(":")
                        .append(port)
                        .append(uri.getPath());
                String query = uri.getQuery();
                if (query == null || query.isBlank()) {
                    jdbc.append("?sslmode=require");
                } else if (query.contains("sslmode")) {
                    jdbc.append("?").append(query);
                } else {
                    jdbc.append("?").append(query).append("&sslmode=require");
                }
                return Optional.of(new RemoteDbConfig(jdbc.toString(), user, pass));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("DIRECT_CONNECTION has invalid format", e);
            }
        }

        String jdbcUrl() {
            return jdbcUrl;
        }

        String username() {
            return username;
        }

        String password() {
            return password;
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
