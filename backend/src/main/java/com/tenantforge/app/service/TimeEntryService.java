package com.tenantforge.app.service;

import com.tenantforge.app.domain.TimeEntry;
import com.tenantforge.app.repository.TimeEntryRepository;
import com.tenantforge.app.tenant.TenantContextHolder;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for time tracking entries.
 *
 * Supports filtered listing by time window, task and user, creation with
 * validation, updates and soft-deletes. Deleted records are excluded from
 * reads.
 */
@Service
public class TimeEntryService {

    private final TimeEntryRepository entries;

    public TimeEntryService(TimeEntryRepository entries) { this.entries = entries; }

    /**
     * List time entries with optional filters.
     *
     * @param start inclusive start time (nullable)
     * @param end inclusive end time (nullable)
     * @param taskId optional task id
     * @param userId optional user id
     * @param page zero-based page number
     * @param size page size
     * @param sort sort configuration
     * @return a page of non-deleted time entries
     */
    public Page<TimeEntry> list(Instant start, Instant end, UUID taskId, UUID userId, int page, int size, Sort sort){
        var pageable = PageRequest.of(page, size, sort);
        Specification<TimeEntry> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (start != null && end != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("startedAt"), start, end));
        }
        if (taskId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("taskId"), taskId));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        return entries.findAll(spec, pageable);
    }

    /**
     * Create a time entry for the given task and user.
     *
     * @param taskId task id (required)
     * @param userId user id (required)
     * @param startedAt start timestamp (required)
     * @param endedAt end timestamp (required, must be after start)
     * @param notes optional notes
     * @return persisted time entry
     * @throws IllegalArgumentException when endedAt is before startedAt
     */
    @Transactional
    public TimeEntry create(UUID taskId, UUID userId, Instant startedAt, Instant endedAt, String notes){
        if (endedAt.isBefore(startedAt)) throw new IllegalArgumentException("endedAt must be after startedAt");
        UUID tenantId = TenantContextHolder.getTenantId().orElseThrow();
        return entries.save(new TimeEntry(tenantId, taskId, userId, startedAt, endedAt, notes));
    }

    /** Find by id if not soft-deleted. */
    public Optional<TimeEntry> find(UUID id){ return entries.findById(id).filter(e -> !e.isDeleted()); }

    /**
     * Update a time entry when present and not soft-deleted.
     *
     * @param id time entry id
     * @param startedAt new start time
     * @param endedAt new end time
     * @param notes new notes (nullable)
     * @return updated entry if found; empty otherwise
     */
    @Transactional
    public Optional<TimeEntry> update(UUID id, Instant startedAt, Instant endedAt, String notes){
        return find(id).map(e -> { e.setStartedAt(startedAt); e.setEndedAt(endedAt); e.setNotes(notes); e.setUpdatedAt(Instant.now()); return e;});
    }

    /** Soft-delete by setting deletedAt; no-op if not found. */
    @Transactional
    public boolean softDelete(UUID id){ return find(id).map(e -> { e.setDeletedAt(Instant.now()); return true;}).orElse(false); }
}
