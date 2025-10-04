package com.tenantforge.app.repository;

import com.tenantforge.app.domain.Task;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findAllByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Task> findAllByDeletedAtIsNull(Pageable pageable);
}

