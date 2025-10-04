package com.tenantforge.app.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.web.ProjectController.CreateRequest;
import com.tenantforge.app.web.dto.AuthResponse;
import com.tenantforge.app.web.dto.RegistrationRequest;
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
            "spring.datasource.url=jdbc:h2:mem:tenantforge-proj;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
            "spring.flyway.enabled=false"
        })
@Disabled("Controller IT requires full JWT flow; enable explicitly when needed")
class ProjectControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void createAndFetchProject() {
        String token = registerAndGetToken();
        var created = create(token, "P1","desc");
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        var id = UUID.fromString(created.getBody().get("id").toString());

        HttpHeaders h = authHeaders(token);
        var get = rest.exchange(url("/api/projects/"+id), HttpMethod.GET, new HttpEntity<>(h), java.util.Map.class);
        assertThat(get.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(get.getBody().get("name")).isEqualTo("P1");
    }

    private ResponseEntity<java.util.Map> create(String token, String name, String desc){
        HttpHeaders h = authHeaders(token);
        return rest.exchange(url("/api/projects"), HttpMethod.POST, new HttpEntity<>(new CreateRequest(name, desc), h), java.util.Map.class);
    }

    private String registerAndGetToken(){
        RegistrationRequest req = new RegistrationRequest("t-"+java.util.UUID.randomUUID(),
                "u-"+java.util.UUID.randomUUID()+"@tf.dev", "Password123!", "Owner");
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<AuthResponse> resp = rest.exchange(url("/api/auth/register"), HttpMethod.POST, new HttpEntity<>(req, h), AuthResponse.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        return resp.getBody().accessToken();
    }

    private HttpHeaders authHeaders(String token){
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(token);
        return h;
    }

    private String url(String path){ return "http://localhost:"+port+path; }
}
