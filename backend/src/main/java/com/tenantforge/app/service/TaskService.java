package com.tenantforge.app.service;

import com.tenantforge.app.domain.Task;
import com.tenantforge.app.repository.TaskRepository;
import com.tenantforge.app.tenant.TenantContextHolder;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository tasks;

    public TaskService(TaskRepository tasks) { this.tasks = tasks; }

    public Page<Task> list(String q, int page, int size) {
        var pageable = PageRequest.of(page, size);
        if (q != null && !q.isBlank()) return tasks.findAllByDeletedAtIsNullAndNameContainingIgnoreCase(q, pageable);
        return tasks.findAllByDeletedAtIsNull(pageable);
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

