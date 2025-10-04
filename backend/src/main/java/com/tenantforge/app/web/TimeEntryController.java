package com.tenantforge.app.web;

import com.tenantforge.app.domain.TimeEntry;
import com.tenantforge.app.service.TimeEntryService;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/time-entries")
@Validated
public class TimeEntryController {

    private final TimeEntryService service;

    public TimeEntryController(TimeEntryService service) {
        this.service = service;
    }

    @GetMapping
    public Page<TimeEntry> list(
            @RequestParam(value = "start", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant start,
            @RequestParam(value = "end", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant end,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return service.list(start, end, page, size);
    }

    @PostMapping
    public TimeEntry create(@RequestBody CreateRequest req) {
        return service.create(req.taskId, req.userId, req.startedAt, req.endedAt, req.notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeEntry> get(@PathVariable UUID id) {
        return service.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeEntry> update(@PathVariable UUID id, @RequestBody UpdateRequest req) {
        return service
                .update(id, req.startedAt, req.endedAt, req.notes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
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

