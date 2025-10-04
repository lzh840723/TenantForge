package com.tenantforge.app.repository;

import java.time.Instant;
import java.util.UUID;

public interface TimeReportRow {
    Instant getBucket();
    UUID getUserId();
    UUID getProjectId();
    Double getHours();
}

