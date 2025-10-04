package com.tenantforge.app.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:tenantforge-obs;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
        })
class DocsAndActuatorIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;
    @Autowired
    ApplicationContext ctx;

    @Test
    void actuatorHealth_isUp() {
        ResponseEntity<String> resp = rest.getForEntity(url("/actuator/health"), String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).contains("UP");
    }

    @Test
    void openapi_bean_present() {
        String[] beanNames = ctx.getBeanNamesForType(org.springdoc.webmvc.api.OpenApiResource.class);
        assertThat(beanNames.length > 0).isTrue();
    }

    private String url(String p){ return "http://localhost:"+port+p; }
}
