package com.tenantforge.app.time;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.web.TimeEntryController.CreateRequest;
import com.tenantforge.app.web.dto.AuthResponse;
import com.tenantforge.app.web.dto.RegistrationRequest;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:tenantforge-time;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
            "spring.flyway.enabled=false"
        })
@Disabled("Controller IT requires full JWT flow; enable explicitly when needed")
class TimeEntryControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void createAndFetchTimeEntry() {
        String token = registerAndGetToken();
        UUID projectId = createProject(token, "P-time");
        UUID taskId = createTask(token, projectId, "T-time");

        CreateRequest req = new CreateRequest(
                taskId,
                UUID.randomUUID(),
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                "work notes");

        ResponseEntity<Map> created = rest.exchange(
                url("/api/time-entries"), HttpMethod.POST, new HttpEntity<>(req, authHeaders(token)), Map.class);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        UUID id = UUID.fromString(created.getBody().get("id").toString());

        ResponseEntity<Map> get = rest.exchange(url("/api/time-entries/" + id), HttpMethod.GET, new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(get.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(get.getBody().get("notes")).isEqualTo("work notes");
    }

    private UUID createProject(String token, String name) {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/projects"),
                HttpMethod.POST,
                new HttpEntity<>(new com.tenantforge.app.web.ProjectController.CreateRequest(name, "desc"), authHeaders(token)),
                Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return UUID.fromString(resp.getBody().get("id").toString());
    }

    private UUID createTask(String token, UUID projectId, String name) {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/tasks"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("projectId", projectId.toString(), "name", name), authHeaders(token)),
                Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return UUID.fromString(resp.getBody().get("id").toString());
    }

    private String url(String path) { return "http://localhost:" + port + path; }
    private HttpHeaders authHeaders(String token){ HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); h.setBearerAuth(token); return h; }

    private String registerAndGetToken(){
        RegistrationRequest req = new RegistrationRequest("t-"+java.util.UUID.randomUUID(),
                "u-"+java.util.UUID.randomUUID()+"@tf.dev", "Password123!", "Owner");
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<AuthResponse> resp = rest.exchange(url("/api/auth/register"), HttpMethod.POST, new HttpEntity<>(req, h), AuthResponse.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return resp.getBody().accessToken();
    }
}
