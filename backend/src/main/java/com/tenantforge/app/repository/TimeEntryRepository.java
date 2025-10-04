package com.tenantforge.app.repository;

import com.tenantforge.app.domain.TimeEntry;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {
    Page<TimeEntry> findAllByDeletedAtIsNullAndStartedAtBetween(Instant start, Instant end, Pageable pageable);
    Page<TimeEntry> findAllByDeletedAtIsNull(Pageable pageable);

    @Query(value = """
            SELECT date_trunc(:period, te.started_at) AS bucket,
                   te.user_id AS user_id,
                   t.project_id AS project_id,
                   SUM(EXTRACT(EPOCH FROM (te.ended_at - te.started_at)) / 3600.0) AS hours
            FROM time_entries te
            JOIN tasks t ON t.id = te.task_id
            WHERE te.deleted_at IS NULL
              AND t.deleted_at IS NULL
              AND (:userId IS NULL OR te.user_id = :userId)
              AND (:projectId IS NULL OR t.project_id = :projectId)
            GROUP BY bucket, te.user_id, t.project_id
            ORDER BY bucket DESC
            """, nativeQuery = true)
    List<TimeReportRow> aggregate(
            @Param("period") String period,
            @Param("userId") UUID userId,
            @Param("projectId") UUID projectId);
}
