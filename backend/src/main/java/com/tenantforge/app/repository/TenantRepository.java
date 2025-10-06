package com.tenantforge.app.repository;

import com.tenantforge.app.domain.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByNameIgnoreCase(String name);
}
