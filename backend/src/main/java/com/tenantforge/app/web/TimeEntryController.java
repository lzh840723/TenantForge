package com.tenantforge.app.web;

import com.tenantforge.app.domain.TimeEntry;
import com.tenantforge.app.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/time-entries")
@Tag(name = "TimeEntries")
@Validated
public class TimeEntryController {

    private final TimeEntryService service;

    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List time entries with filters and pagination")
    public Page<TimeEntry> list(
            @Parameter(description = "Start time inclusive (ISO8601)")
            @RequestParam(value = "start", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant start,
            @Parameter(description = "End time inclusive (ISO8601)")
            @RequestParam(value = "end", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant end,
            @Parameter(description = "Filter by taskId")
            @RequestParam(value = "taskId", required = false) UUID taskId,
            @Parameter(description = "Filter by userId")
            @RequestParam(value = "userId", required = false) UUID userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(value = "size", defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(value = "sort", defaultValue = "startedAt") String sort,
            @Parameter(description = "Sort order asc|desc")
            @RequestParam(value = "order", defaultValue = "desc") String order) {
        Sort.Direction dir = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return service.list(start, end, taskId, userId, page, size, Sort.by(dir, sort));
    }

    @PostMapping
    @Operation(summary = "Create time entry")
    public TimeEntry create(@RequestBody @Valid CreateRequest req) {
        return service.create(req.taskId, req.userId, req.startedAt, req.endedAt, req.notes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get time entry by id")
    public ResponseEntity<TimeEntry> get(@PathVariable UUID id) {
        return service.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update time entry")
    public ResponseEntity<TimeEntry> update(@PathVariable UUID id, @RequestBody @Valid UpdateRequest req) {
        return service
                .update(id, req.startedAt, req.endedAt, req.notes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete time entry")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return service.softDelete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    public record CreateRequest(
            @NotNull UUID taskId,
            @NotNull UUID userId,
            @NotNull Instant startedAt,
            @NotNull Instant endedAt,
            String notes) {}

    public record UpdateRequest(@NotNull Instant startedAt, @NotNull Instant endedAt, String notes) {}
}
