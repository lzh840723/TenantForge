package com.tenantforge.app.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.web.ProjectController.CreateRequest;
import com.tenantforge.app.web.TaskController.UpdateRequest;
import com.tenantforge.app.web.dto.AuthResponse;
import com.tenantforge.app.web.dto.RegistrationRequest;
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
            "spring.datasource.url=jdbc:h2:mem:tenantforge-task;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
            "spring.flyway.enabled=false"
        })
@Disabled("Controller IT requires full JWT flow; enable explicitly when needed")
class TaskControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void createUpdateTaskRoundtrip() {
        String token = registerAndGetToken();
        UUID projectId = createProject(token, "P-task");

        Map created = postJson(token, "/api/tasks", Map.of("projectId", projectId.toString(), "name", "T1"));
        assertThat(created.get("id")).isNotNull();
        UUID taskId = UUID.fromString(created.get("id").toString());

        ResponseEntity<Map> get = rest.exchange(url("/api/tasks/" + taskId), HttpMethod.GET, new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(get.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(get.getBody().get("name")).isEqualTo("T1");

        ResponseEntity<Map> updated = rest.exchange(
                url("/api/tasks/" + taskId),
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateRequest("T1-upd", "NEW"), authHeaders(token)),
                Map.class);
        assertThat(updated.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(updated.getBody().get("name")).isEqualTo("T1-upd");
    }

    private UUID createProject(String token, String name) {
        ResponseEntity<Map> resp = rest.exchange(
                url("/api/projects"),
                HttpMethod.POST,
                new HttpEntity<>(new CreateRequest(name, "desc"), authHeaders(token)),
                Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return UUID.fromString(resp.getBody().get("id").toString());
    }

    private String url(String path) { return "http://localhost:" + port + path; }
    private HttpHeaders authHeaders(String token){ HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); h.setBearerAuth(token); return h; }
    private Map postJson(String token, String path, Object body){
        ResponseEntity<Map> resp = rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, authHeaders(token)), Map.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return resp.getBody();
    }

    private String registerAndGetToken(){
        RegistrationRequest req = new RegistrationRequest("t-"+java.util.UUID.randomUUID(),
                "u-"+java.util.UUID.randomUUID()+"@tf.dev", "Password123!", "Owner");
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<AuthResponse> resp = rest.exchange(url("/api/auth/register"), HttpMethod.POST, new HttpEntity<>(req, h), AuthResponse.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return resp.getBody().accessToken();
    }
}
