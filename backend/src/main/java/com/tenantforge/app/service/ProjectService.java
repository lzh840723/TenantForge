package com.tenantforge.app.service;

import com.tenantforge.app.domain.Project;
import com.tenantforge.app.repository.ProjectRepository;
import com.tenantforge.app.tenant.TenantContextHolder;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) { this.projects = projects; }

    public Page<Project> list(String q, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        if (q != null && !q.isBlank()) {
            return projects.findAllByDeletedAtIsNullAndNameContainingIgnoreCase(q, pageable);
        }
        return projects.findAllByDeletedAtIsNull(pageable);
    }

    @Transactional
    public Project create(String name, String description) {
        UUID tenantId = TenantContextHolder.getTenantId().orElseThrow();
        return projects.save(new Project(tenantId, name, description));
    }

    public Optional<Project> find(UUID id) {
        return projects.findById(id).filter(p -> !p.isDeleted());
    }

    @Transactional
    public Optional<Project> update(UUID id, String name, String description) {
        return find(id).map(p -> {
            p.setName(name);
            p.setDescription(description);
            p.setUpdatedAt(Instant.now());
            return p;
        });
    }

    @Transactional
    public boolean softDelete(UUID id) {
        return find(id).map(p -> { p.setDeletedAt(Instant.now()); return true; }).orElse(false);
    }
}
