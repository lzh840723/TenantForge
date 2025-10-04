package com.tenantforge.app.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.web.ProjectController.CreateRequest;
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
            "spring.datasource.url=jdbc:h2:mem:tenantforge-proj;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
            "spring.flyway.enabled=false"
        })
class ProjectControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void createAndFetchProject() {
        var created = create("P1","desc");
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        var id = UUID.fromString(created.getBody().get("id").toString());

        var get = rest.getForEntity(url("/api/projects/"+id), java.util.Map.class);
        assertThat(get.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(get.getBody().get("name")).isEqualTo("P1");
    }

    private ResponseEntity<java.util.Map> create(String name, String desc){
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(url("/api/projects"), HttpMethod.POST, new HttpEntity<>(new CreateRequest(name, desc), h), java.util.Map.class);
    }

    private String url(String path){ return "http://localhost:"+port+path; }
}

