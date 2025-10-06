package com.tenantforge.app.web;

import com.tenantforge.app.domain.Project;
import com.tenantforge.app.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Project CRUD endpoints with pagination and filtering.
 */
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects")
@Validated
public class ProjectController {

    private final ProjectService service;
    public ProjectController(ProjectService service){ this.service=service; }

    @GetMapping
    @Operation(summary = "List projects with pagination and filtering")
    public Page<Project> list(@Parameter(description = "Name contains filter")
                              @RequestParam(value = "q", required = false) String q,
                              @Parameter(description = "Page number (0-based)")
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @Parameter(description = "Page size")
                              @RequestParam(value = "size", defaultValue = "20") int size,
                              @Parameter(description = "Sort field")
                              @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
                              @Parameter(description = "Sort order asc|desc")
                              @RequestParam(value = "order", defaultValue = "desc") String order){
        Sort.Direction dir = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return service.list(q, page, size, Sort.by(dir, sort));
    }

    @PostMapping
    @Operation(summary = "Create a project")
    public Project create(@RequestBody @Valid CreateRequest req){
        return service.create(req.name, req.description);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by id")
    public ResponseEntity<Project> get(@PathVariable("id") UUID id){
        return service.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<Project> update(@PathVariable("id") UUID id, @RequestBody @Valid UpdateRequest req){
        return service.update(id, req.name, req.description)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete project")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        return service.softDelete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    public record CreateRequest(@NotBlank String name, String description){}
    public record UpdateRequest(@NotBlank String name, String description){}
}
