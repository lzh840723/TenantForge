package com.tenantforge.app.web;

import com.tenantforge.app.domain.Task;
import com.tenantforge.app.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks")
@Validated
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "List tasks with filters and pagination")
    public Page<Task> list(@Parameter(description = "Name contains filter")
                           @RequestParam(value = "q", required = false) String q,
                           @Parameter(description = "Filter by projectId")
                           @RequestParam(value = "projectId", required = false) UUID projectId,
                           @Parameter(description = "Filter by status")
                           @RequestParam(value = "status", required = false) String status,
                           @Parameter(description = "Page number (0-based)")
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @Parameter(description = "Page size")
                           @RequestParam(value = "size", defaultValue = "20") int size,
                           @Parameter(description = "Sort field")
                           @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
                           @Parameter(description = "Sort order asc|desc")
                           @RequestParam(value = "order", defaultValue = "desc") String order){
        Sort.Direction dir = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return service.list(q, projectId, status, page, size, Sort.by(dir, sort));
    }

    @PostMapping
    @Operation(summary = "Create task")
    public Task create(@RequestBody @Valid CreateRequest req){
        return service.create(req.projectId, req.name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    public ResponseEntity<Task> get(@PathVariable UUID id){
        return service.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<Task> update(@PathVariable UUID id, @RequestBody @Valid UpdateRequest req){
        return service.update(id, req.name, req.status).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete task")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        return service.softDelete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    public record CreateRequest(@jakarta.validation.constraints.NotNull UUID projectId, @NotBlank String name){}
    public record UpdateRequest(@NotBlank String name, @NotBlank String status){}
}
