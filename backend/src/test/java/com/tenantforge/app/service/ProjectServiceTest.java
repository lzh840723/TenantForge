package com.tenantforge.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.repository.ProjectRepository;
import com.tenantforge.app.tenant.TenantContextHolder;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(ProjectService.class)
class ProjectServiceTest {

    @Autowired
    ProjectRepository projects;

    @Autowired
    ProjectService service;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() { TenantContextHolder.clear(); }

    @Test
    void createUpdateSoftDelete() {
        var p = service.create("P", "d");
        assertThat(p.getTenantId()).isEqualTo(tenantId);

        var updated = service.update(p.getId(), "P2", "d2").orElseThrow();
        assertThat(updated.getName()).isEqualTo("P2");

        boolean deleted = service.softDelete(p.getId());
        assertThat(deleted).isTrue();
        assertThat(service.find(p.getId())).isEmpty();
    }
}

