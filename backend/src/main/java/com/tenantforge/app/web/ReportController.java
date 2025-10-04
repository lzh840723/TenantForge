package com.tenantforge.app.web;

import com.tenantforge.app.repository.TimeEntryRepository;
import com.tenantforge.app.repository.TimeReportRow;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reporting endpoints for time aggregation with JSON and CSV formats.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports")
public class ReportController {

    private final TimeEntryRepository timeEntries;

    public ReportController(TimeEntryRepository timeEntries) {
        this.timeEntries = timeEntries;
    }

    @GetMapping("/time")
    public ResponseEntity<?> timeReport(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false, defaultValue = "json") String format) {
        String p = normalizePeriod(period);
        List<TimeReportRow> rows = timeEntries.aggregate(p, userId, projectId);

        if ("csv".equalsIgnoreCase(format)) {
            String csv = toCsv(rows);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + p + ".csv")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .body(csv);
        }
        return ResponseEntity.ok(rows);
    }

    private static String normalizePeriod(String period) {
        String p = (period == null ? "" : period).toLowerCase();
        if (!StringUtils.hasText(p) || !(p.equals("week") || p.equals("month"))) {
            return "week";
        }
        return p;
    }

    private static String toCsv(List<TimeReportRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("bucket,user_id,project_id,hours\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        for (TimeReportRow r : rows) {
            sb.append(fmt.format(r.getBucket()))
                    .append(',')
                    .append(r.getUserId())
                    .append(',')
                    .append(r.getProjectId())
                    .append(',')
                    .append(String.format(java.util.Locale.ROOT, "%.3f", r.getHours()))
                    .append('\n');
        }
        return sb.toString();
    }
}
