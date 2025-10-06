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

@Service
public class TimeEntryService {

    private final TimeEntryRepository entries;

    public TimeEntryService(TimeEntryRepository entries) { this.entries = entries; }

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

    @Transactional
    public TimeEntry create(UUID taskId, UUID userId, Instant startedAt, Instant endedAt, String notes){
        if (endedAt.isBefore(startedAt)) throw new IllegalArgumentException("endedAt must be after startedAt");
        UUID tenantId = TenantContextHolder.getTenantId().orElseThrow();
        return entries.save(new TimeEntry(tenantId, taskId, userId, startedAt, endedAt, notes));
    }

    public Optional<TimeEntry> find(UUID id){ return entries.findById(id).filter(e -> !e.isDeleted()); }

    @Transactional
    public Optional<TimeEntry> update(UUID id, Instant startedAt, Instant endedAt, String notes){
        return find(id).map(e -> { e.setStartedAt(startedAt); e.setEndedAt(endedAt); e.setNotes(notes); e.setUpdatedAt(Instant.now()); return e;});
    }

    @Transactional
    public boolean softDelete(UUID id){ return find(id).map(e -> { e.setDeletedAt(Instant.now()); return true;}).orElse(false); }
}
