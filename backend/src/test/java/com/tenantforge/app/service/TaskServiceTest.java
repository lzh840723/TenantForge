package com.tenantforge.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.repository.TaskRepository;
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
@Import(TaskService.class)
class TaskServiceTest {

    @Autowired
    TaskRepository tasks;

    @Autowired
    TaskService service;

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
        UUID projectId = UUID.randomUUID();
        var t = service.create(projectId, "T");
        assertThat(t.getTenantId()).isEqualTo(tenantId);
        assertThat(t.getProjectId()).isEqualTo(projectId);

        var upd = service.update(t.getId(), "T2", "NEW").orElseThrow();
        assertThat(upd.getName()).isEqualTo("T2");

        boolean deleted = service.softDelete(t.getId());
        assertThat(deleted).isTrue();
        assertThat(service.find(t.getId())).isEmpty();
    }
}

