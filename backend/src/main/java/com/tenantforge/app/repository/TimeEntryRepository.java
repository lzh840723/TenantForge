package com.tenantforge.app.repository;

import com.tenantforge.app.domain.TimeEntry;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    Page<TimeEntry> findAllByDeletedAtIsNullAndStartedAtBetween(Instant start, Instant end, Pageable pageable);
    Page<TimeEntry> findAllByDeletedAtIsNull(Pageable pageable);
}

