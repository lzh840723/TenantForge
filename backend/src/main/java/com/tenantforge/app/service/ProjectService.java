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

/**
 * Application service for Project domain operations.
 *
 * Responsibilities:
 * - Query projects with optional fuzzy name filter and pagination.
 * - Create new projects for the current tenant.
 * - Read, update and soft-delete by id (ignoring already deleted records).
 */
@Service
public class ProjectService {

    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) { this.projects = projects; }

    /**
     * List projects for the current tenant.
     *
     * @param q optional case-insensitive name contains filter
     * @param page zero-based page number
     * @param size page size
     * @param sort sort configuration
     * @return a page of non-deleted projects
     *
     * <pre>Example:
     * service.list("demo", 0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
     * </pre>
     */
    public Page<Project> list(String q, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        if (q != null && !q.isBlank()) {
            return projects.findAllByDeletedAtIsNullAndNameContainingIgnoreCase(q, pageable);
        }
        return projects.findAllByDeletedAtIsNull(pageable);
    }

    /**
     * Create a new project under the current tenant.
     *
     * @param name project name (required, validated by controller)
     * @param description optional description
     * @return persisted project entity
     */
    @Transactional
    public Project create(String name, String description) {
        UUID tenantId = TenantContextHolder.getTenantId().orElseThrow();
        return projects.save(new Project(tenantId, name, description));
    }

    /**
     * Find a project by id if not soft-deleted.
     *
     * @param id project id
     * @return optional project when present and not deleted
     */
    public Optional<Project> find(UUID id) {
        return projects.findById(id).filter(p -> !p.isDeleted());
    }

    /**
     * Update project name/description when present and not soft-deleted.
     *
     * @param id project id
     * @param name new name
     * @param description new description (nullable)
     * @return updated project when found; empty if not found or deleted
     */
    @Transactional
    public Optional<Project> update(UUID id, String name, String description) {
        return find(id).map(p -> {
            p.setName(name);
            p.setDescription(description);
            p.setUpdatedAt(Instant.now());
            return p;
        });
    }

    /**
     * Soft-delete a project by setting its deletedAt timestamp.
     *
     * @param id project id
     * @return true if deletion flag was set; false if not found or already deleted
     */
    @Transactional
    public boolean softDelete(UUID id) {
        return find(id).map(p -> { p.setDeletedAt(Instant.now()); return true; }).orElse(false);
    }
}
