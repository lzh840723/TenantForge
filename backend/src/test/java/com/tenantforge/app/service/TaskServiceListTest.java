package com.tenantforge.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.domain.Task;
import com.tenantforge.app.repository.TaskRepository;
import com.tenantforge.app.tenant.TenantContextHolder;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(TaskService.class)
class TaskServiceListTest {

    @Autowired
    TaskRepository tasks;

    @Autowired
    TaskService service;

    private UUID tenantId;
    private UUID projectA;
    private UUID projectB;

    @BeforeEach
    void setup(){
        tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
        projectA = UUID.randomUUID();
        projectB = UUID.randomUUID();

        Task a1 = new Task(tenantId, projectA, "Alpha One");
        a1.setStatus("NEW");
        Task a2 = new Task(tenantId, projectA, "Alpha Two");
        a2.setStatus("DONE");
        Task b1 = new Task(tenantId, projectB, "Beta One");
        b1.setStatus("NEW");
        Task deleted = new Task(tenantId, projectA, "Ghost");
        deleted.setDeletedAt(Instant.now());

        tasks.save(a1); tasks.save(a2); tasks.save(b1); tasks.save(deleted);
    }

    @AfterEach
    void clean(){ TenantContextHolder.clear(); }

    @Test
    void filter_by_project_and_status_and_query() {
        var page = service.list("Alpha", projectA, "NEW", 0, 20, Sort.by(Sort.Direction.ASC, "name"));
        assertThat(page.getContent()).extracting(Task::getName).containsExactly("Alpha One");
    }

    @Test
    void excludes_soft_deleted() {
        var page = service.list(null, projectA, null, 0, 20, Sort.by("name"));
        assertThat(page.getContent()).extracting(Task::getName).doesNotContain("Ghost");
    }
}

