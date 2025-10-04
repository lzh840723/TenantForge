package com.tenantforge.app.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.web.ProjectController.CreateRequest;
import com.tenantforge.app.web.TaskController.UpdateRequest;
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
            "spring.datasource.url=jdbc:h2:mem:tenantforge-task;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
            "spring.flyway.enabled=false"
        })
class TaskControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void createUpdateTaskRoundtrip() {
        UUID projectId = createProject("P-task");

        Map created = postJson("/api/tasks", Map.of("projectId", projectId.toString(), "name", "T1"));
        assertThat(created.get("id")).isNotNull();
        UUID taskId = UUID.fromString(created.get("id").toString());

        ResponseEntity<Map> get = rest.getForEntity(url("/api/tasks/" + taskId), Map.class);
        assertThat(get.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(get.getBody().get("name")).isEqualTo("T1");

        ResponseEntity<Map> updated = rest.exchange(
                url("/api/tasks/" + taskId),
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateRequest("T1-upd", "NEW"), jsonHeaders()),
                Map.class);
        assertThat(updated.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(updated.getBody().get("name")).isEqualTo("T1-upd");
    }

    private UUID createProject(String name) {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/projects"),
                HttpMethod.POST,
                new HttpEntity<>(new CreateRequest(name, "desc"), jsonHeaders()),
                Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return UUID.fromString(resp.getBody().get("id").toString());
    }

    private String url(String path) { return "http://localhost:" + port + path; }
    private HttpHeaders jsonHeaders(){ HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); return h; }
    private Map postJson(String path, Object body){
        ResponseEntity<Map> resp = rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return resp.getBody();
    }
}

