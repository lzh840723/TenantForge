package com.tenantforge.app.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.tenantforge.app.domain.Task;
import com.tenantforge.app.domain.TimeEntry;
import com.tenantforge.app.repository.TaskRepository;
import com.tenantforge.app.repository.TimeEntryRepository;
import com.tenantforge.app.repository.TimeReportRow;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@EnabledIfEnvironmentVariable(named = "RUN_PG_IT", matches = "(?i)true|1|yes")
class ReportRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void pgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    TaskRepository tasks;

    @Autowired
    TimeEntryRepository entries;

    static UUID TENANT = UUID.randomUUID();
    static UUID PROJECT = UUID.randomUUID();
    static UUID USER = UUID.randomUUID();

    @BeforeAll
    static void check() {
        assumeTrue(true);
    }

    @Test
    void aggregate_week_returns_rows() {
        Task task = tasks.save(new Task(TENANT, PROJECT, "Demo Task"));
        entries.save(new TimeEntry(TENANT, task.getId(), USER,
                Instant.parse("2024-01-01T10:00:00Z"), Instant.parse("2024-01-01T12:00:00Z"), "work"));
        entries.save(new TimeEntry(TENANT, task.getId(), USER,
                Instant.parse("2024-01-02T10:00:00Z"), Instant.parse("2024-01-02T11:00:00Z"), "work"));

        List<TimeReportRow> rows = entries.aggregate("week", null, null);
        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0).getHours()).isGreaterThan(0.0);
    }
}

