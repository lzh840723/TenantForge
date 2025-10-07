package com.tenantforge.app.service;

import com.tenantforge.app.domain.AppUser;
import com.tenantforge.app.domain.Tenant;
import com.tenantforge.app.security.AppUserDetailsService;
import com.tenantforge.app.security.JwtService;
import com.tenantforge.app.security.TokenPair;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Authentication flows: tenant registration, login and token refresh.
 *
 * Notes:
 * - Registration creates a new tenant and a TENANT_ADMIN user, then issues a token pair.
 * - Login authenticates via Spring Security and issues a new token pair.
 * - Refresh validates the refresh token (token_type=refresh) and issues a new pair.
 */
@Service
public class AuthService {

    private final TenantService tenantService;
    private final AppUserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;

    public AuthService(
            TenantService tenantService,
            AppUserService userService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            AppUserDetailsService userDetailsService) {
        this.tenantService = tenantService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Register a new tenant and its primary owner.
     *
     * @param tenantName tenant display name
     * @param email owner email (must be unique)
     * @param password raw password (encoded before persist)
     * @param displayName owner display name
     * @return issued access/refresh token pair for the created owner
     * @throws IllegalArgumentException when the email is already registered
     */
    @Transactional
    public TokenPair registerTenant(String tenantName, String email, String password, String displayName) {
        Optional<AppUser> existing = userService.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        Tenant tenant = tenantService.createTenant(tenantName);
        AppUser user = userService.createAdmin(tenant, email, password, displayName);
        return jwtService.generateTokens(user);
    }

    /**
     * Authenticate and issue a token pair.
     *
     * @param email email (username)
     * @param password raw password
     * @return access/refresh token pair
     */
    public TokenPair login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        AppUserDetailsService.AppUserPrincipal principal =
                (AppUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        return jwtService.generateTokens(principal.user());
    }

    /**
     * Refresh tokens using a refresh token.
     *
     * @param refreshToken JWT refresh token
     * @return new access/refresh token pair
     * @throws IllegalArgumentException if token_type is not "refresh"
     */
    public TokenPair refresh(String refreshToken) {
        var claims = jwtService.parse(refreshToken);
        if (!"refresh".equals(jwtService.tokenType(claims))) {
            throw new IllegalArgumentException("Invalid token");
        }
        String email = claims.get("email", String.class);
        AppUserDetailsService.AppUserPrincipal principal =
                (AppUserDetailsService.AppUserPrincipal) userDetailsService.loadUserByUsername(email);
        return jwtService.generateTokens(principal.user());
    }
}
