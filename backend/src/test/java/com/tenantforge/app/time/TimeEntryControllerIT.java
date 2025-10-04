package com.tenantforge.app.time;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.web.TimeEntryController.CreateRequest;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
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
class TimeEntryControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void createAndFetchTimeEntry() {
        UUID projectId = createProject("P-time");
        UUID taskId = createTask(projectId, "T-time");

        CreateRequest req = new CreateRequest(
                taskId,
                UUID.randomUUID(),
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T11:00:00Z"),
                "work notes");

        ResponseEntity<Map> created = rest.exchange(
                url("/api/time-entries"), HttpMethod.POST, new HttpEntity<>(req, jsonHeaders()), Map.class);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        UUID id = UUID.fromString(created.getBody().get("id").toString());

        ResponseEntity<Map> get = rest.getForEntity(url("/api/time-entries/" + id), Map.class);
        assertThat(get.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(get.getBody().get("notes")).isEqualTo("work notes");
    }

    private UUID createProject(String name) {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/projects"),
                HttpMethod.POST,
                new HttpEntity<>(new com.tenantforge.app.web.ProjectController.CreateRequest(name, "desc"), jsonHeaders()),
                Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return UUID.fromString(resp.getBody().get("id").toString());
    }

    private UUID createTask(UUID projectId, String name) {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/tasks"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("projectId", projectId.toString(), "name", name), jsonHeaders()),
                Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return UUID.fromString(resp.getBody().get("id").toString());
    }

    private String url(String path) { return "http://localhost:" + port + path; }
    private HttpHeaders jsonHeaders(){ HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); return h; }
}

