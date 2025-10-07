package com.tenantforge.app.service;

import com.tenantforge.app.domain.Task;
import com.tenantforge.app.repository.TaskRepository;
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
 * Application service for Task operations.
 *
 * Provides filtered listing, creation, update and soft-delete semantics. All
 * queries ignore soft-deleted records.
 */
@Service
public class TaskService {

    private final TaskRepository tasks;

    public TaskService(TaskRepository tasks) { this.tasks = tasks; }

    /**
     * List tasks with optional filters.
     *
     * @param q optional name contains filter (case-insensitive)
     * @param projectId optional project scope
     * @param status optional status (e.g., NEW/OPEN/CLOSED)
     * @param page zero-based page number
     * @param size page size
     * @param sort sort configuration
     * @return a page of non-deleted tasks matching filters
     */
    public Page<Task> list(String q, UUID projectId, String status, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        Specification<Task> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (q != null && !q.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"));
        }
        if (projectId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("projectId"), projectId));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return tasks.findAll(spec, pageable);
    }

    /**
     * Create a task under the given project for the current tenant.
     *
     * @param projectId owning project id (required)
     * @param name task name (required)
     * @return persisted task
     */
    @Transactional
    public Task create(UUID projectId, String name) {
        UUID tenantId = TenantContextHolder.getTenantId().orElseThrow();
        return tasks.save(new Task(tenantId, projectId, name));
    }

    /** Find by id if not soft-deleted. */
    public Optional<Task> find(UUID id) { return tasks.findById(id).filter(t -> !t.isDeleted()); }

    /**
     * Update task fields when present and not soft-deleted.
     *
     * @param id task id
     * @param name new name
     * @param status new status (e.g., NEW/OPEN/CLOSED)
     * @return updated task if found; empty otherwise
     */
    @Transactional
    public Optional<Task> update(UUID id, String name, String status){
        return find(id).map(t -> { t.setName(name); t.setStatus(status); t.setUpdatedAt(Instant.now()); return t;});
    }

    /** Soft-delete by setting deletedAt; no-op if not found. */
    @Transactional
    public boolean softDelete(UUID id){ return find(id).map(t -> { t.setDeletedAt(Instant.now()); return true;}).orElse(false); }
}
