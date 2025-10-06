package com.tenantforge.app.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenantforge.app.repository.TimeEntryRepository;
import com.tenantforge.app.repository.TimeReportRow;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReportControllerUnitTest {

    private static final class Row implements TimeReportRow {
        private final Instant bucket; private final UUID userId; private final UUID projectId; private final Double hours;
        Row(Instant bucket, UUID userId, UUID projectId, Double hours){ this.bucket=bucket; this.userId=userId; this.projectId=projectId; this.hours=hours; }
        public Instant getBucket(){ return bucket; }
        public UUID getUserId(){ return userId; }
        public UUID getProjectId(){ return projectId; }
        public Double getHours(){ return hours; }
    }

    @Test
    void returns_json_and_csv() {
        TimeEntryRepository repo = org.mockito.Mockito.mock(TimeEntryRepository.class);
        var row = new Row(Instant.parse("2024-01-01T00:00:00Z"), UUID.randomUUID(), UUID.randomUUID(), 1.25);
        org.mockito.Mockito.when(repo.aggregate("week", null, null)).thenReturn(List.of(row));

        ReportController controller = new ReportController(repo);
        var jsonResp = controller.timeReport("week", null, null, null);
        assertThat(jsonResp.getStatusCode().is2xxSuccessful()).isTrue();
        java.util.List<?> jsonBody = (java.util.List<?>) jsonResp.getBody();
        assertThat(jsonBody).hasSize(1);

        var csvResp = controller.timeReport("week", null, null, "csv");
        assertThat(csvResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(csvResp.getHeaders().getFirst("Content-Type")).contains("text/csv");
        assertThat(csvResp.getBody().toString()).contains("bucket,user_id,project_id,hours");
    }
}
