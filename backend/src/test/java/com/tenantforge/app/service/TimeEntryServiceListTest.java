package com.tenantforge.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.domain.TimeEntry;
import com.tenantforge.app.repository.TimeEntryRepository;
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
@Import(TimeEntryService.class)
class TimeEntryServiceListTest {

    @Autowired
    TimeEntryRepository entries;

    @Autowired
    TimeEntryService service;

    private UUID tenantId;
    private UUID taskA;
    private UUID taskB;
    private UUID user1;
    private UUID user2;

    @BeforeEach
    void setup(){
        tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
        taskA = UUID.randomUUID();
        taskB = UUID.randomUUID();
        user1 = UUID.randomUUID();
        user2 = UUID.randomUUID();

        Instant s1 = Instant.parse("2024-01-01T10:00:00Z");
        Instant e1 = Instant.parse("2024-01-01T11:00:00Z");
        Instant s2 = Instant.parse("2024-01-02T10:00:00Z");
        Instant e2 = Instant.parse("2024-01-02T12:00:00Z");

        entries.save(new TimeEntry(tenantId, taskA, user1, s1, e1, "a1"));
        entries.save(new TimeEntry(tenantId, taskA, user2, s2, e2, "a2"));
        entries.save(new TimeEntry(tenantId, taskB, user1, s2, e2, "b1"));
    }

    @AfterEach
    void clean(){ TenantContextHolder.clear(); }

    @Test
    void filter_by_range_task_and_user() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-02T23:59:59Z");
        var page = service.list(start, end, taskA, user1, 0, 20, Sort.by(Sort.Direction.DESC, "startedAt"));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getNotes()).isEqualTo("a1");
    }
}

