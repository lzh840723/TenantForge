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

    public TokenPair login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        AppUserDetailsService.AppUserPrincipal principal =
                (AppUserDetailsService.AppUserPrincipal) authentication.getPrincipal();
        return jwtService.generateTokens(principal.user());
    }

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
