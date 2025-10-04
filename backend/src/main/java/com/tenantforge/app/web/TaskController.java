package com.tenantforge.app.web;

import com.tenantforge.app.domain.Task;
import com.tenantforge.app.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public Page<Task> list(@RequestParam(value = "q", required = false) String q,
                           @RequestParam(value = "projectId", required = false) UUID projectId,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "20") int size,
                           @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
                           @RequestParam(value = "order", defaultValue = "desc") String order){
        Sort.Direction dir = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return service.list(q, projectId, status, page, size, Sort.by(dir, sort));
    }

    @PostMapping
    public Task create(@RequestBody CreateRequest req){
        return service.create(req.projectId, req.name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> get(@PathVariable UUID id){
        return service.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable UUID id, @RequestBody UpdateRequest req){
        return service.update(id, req.name, req.status).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        return service.softDelete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    public record CreateRequest(UUID projectId, @NotBlank String name){}
    public record UpdateRequest(@NotBlank String name, @NotBlank String status){}
}
