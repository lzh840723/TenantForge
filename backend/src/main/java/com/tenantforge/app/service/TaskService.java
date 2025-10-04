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

@Service
public class TaskService {

    private final TaskRepository tasks;

    public TaskService(TaskRepository tasks) { this.tasks = tasks; }

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

    @Transactional
    public Task create(UUID projectId, String name) {
        UUID tenantId = TenantContextHolder.getTenantId().orElseThrow();
        return tasks.save(new Task(tenantId, projectId, name));
    }

    public Optional<Task> find(UUID id) { return tasks.findById(id).filter(t -> !t.isDeleted()); }

    @Transactional
    public Optional<Task> update(UUID id, String name, String status){
        return find(id).map(t -> { t.setName(name); t.setStatus(status); t.setUpdatedAt(Instant.now()); return t;});
    }

    @Transactional
    public boolean softDelete(UUID id){ return find(id).map(t -> { t.setDeletedAt(Instant.now()); return true;}).orElse(false); }
}
