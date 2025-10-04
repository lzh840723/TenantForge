package com.tenantforge.app.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.web.dto.AuthResponse;
import com.tenantforge.app.web.dto.LoginRequest;
import com.tenantforge.app.web.dto.RegistrationRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:tenantforge-auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
            "spring.flyway.enabled=false"
        })
class AuthControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void registerThenLogin() {
        String tenantName = "t-" + UUID.randomUUID();
        String email = "u-" + UUID.randomUUID() + "@tf.dev";
        String password = "Password123!";

        RegistrationRequest reg = new RegistrationRequest(tenantName, email, password, "Owner");
        AuthResponse registered = postJson("/api/auth/register", reg, AuthResponse.class);
        assertThat(registered).isNotNull();
        assertThat(registered.accessToken()).isNotBlank();
        assertThat(registered.refreshToken()).isNotBlank();

        LoginRequest login = new LoginRequest(email, password);
        AuthResponse logged = postJson("/api/auth/login", login, AuthResponse.class);
        assertThat(logged).isNotNull();
        assertThat(logged.accessToken()).isNotBlank();
        assertThat(logged.refreshToken()).isNotBlank();
    }

    private <T> T postJson(String path, Object body, Class<T> clazz) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> req = new HttpEntity<>(body, headers);
        ResponseEntity<T> resp = rest.exchange(url(path), HttpMethod.POST, req, clazz);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return resp.getBody();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
