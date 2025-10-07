package com.tenantforge.app.service;

import com.tenantforge.app.domain.AppUser;
import com.tenantforge.app.domain.Tenant;
import com.tenantforge.app.domain.UserRole;
import com.tenantforge.app.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User management utilities used by authentication and admin flows.
 */
@Service
public class AppUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Find active (not deleted) user by email (case-insensitive). */
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).filter(user -> !user.isDeleted());
    }

    /** Find active (not deleted) user by id. */
    public Optional<AppUser> findById(UUID id) {
        return userRepository.findById(id).filter(user -> !user.isDeleted());
    }

    /**
     * Create a tenant admin user with encoded password.
     */
    @Transactional
    public AppUser createAdmin(Tenant tenant, String email, String rawPassword, String displayName) {
        String hashed = passwordEncoder.encode(rawPassword);
        AppUser user = new AppUser(tenant, email, hashed, displayName, UserRole.TENANT_ADMIN);
        return userRepository.save(user);
    }

    /** Update and re-encode a user's password. */
    @Transactional
    public void updatePassword(AppUser user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
    }
}
