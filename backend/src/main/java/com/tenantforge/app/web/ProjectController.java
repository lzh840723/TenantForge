package com.tenantforge.app.web;

import com.tenantforge.app.domain.Project;
import com.tenantforge.app.service.ProjectService;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects")
@Validated
public class ProjectController {

    private final ProjectService service;
    public ProjectController(ProjectService service){ this.service=service; }

    @GetMapping
    public Page<Project> list(@RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "20") int size,
                              @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
                              @RequestParam(value = "order", defaultValue = "desc") String order){
        Sort.Direction dir = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return service.list(q, page, size, Sort.by(dir, sort));
    }

    @PostMapping
    public Project create(@RequestBody CreateRequest req){
        return service.create(req.name, req.description);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> get(@PathVariable UUID id){
        return service.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable UUID id, @RequestBody UpdateRequest req){
        return service.update(id, req.name, req.description)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        return service.softDelete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    public record CreateRequest(@NotBlank String name, String description){}
    public record UpdateRequest(@NotBlank String name, String description){}
}
