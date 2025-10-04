package com.tenantforge.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(TimeEntryService.class)
class TimeEntryServiceTest {

    @Autowired
    TimeEntryRepository entries;

    @Autowired
    TimeEntryService service;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() { TenantContextHolder.clear(); }

    @Test
    void createUpdateSoftDelete_and_timeValidation() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant s = Instant.parse("2024-01-01T10:00:00Z");
        Instant e = Instant.parse("2024-01-01T11:00:00Z");

        var te = service.create(taskId, userId, s, e, "work");
        assertThat(te.getTenantId()).isEqualTo(tenantId);

        var upd = service.update(te.getId(), s, e.plusSeconds(600), "more").orElseThrow();
        assertThat(upd.getNotes()).isEqualTo("more");

        boolean deleted = service.softDelete(te.getId());
        assertThat(deleted).isTrue();
        assertThat(service.find(te.getId())).isEmpty();

        assertThatThrownBy(() -> service.create(taskId, userId, e, s, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

