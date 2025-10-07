package com.tenantforge.app.service;

import com.tenantforge.app.domain.Tenant;
import com.tenantforge.app.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Tenant lifecycle operations.
 */
@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /** Create and persist a new tenant. */
    @Transactional
    public Tenant createTenant(String name) {
        return tenantRepository.save(new Tenant(name));
    }
}
