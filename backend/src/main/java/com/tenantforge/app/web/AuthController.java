package com.tenantforge.app.web;

import com.tenantforge.app.security.TokenPair;
import com.tenantforge.app.service.AuthService;
import com.tenantforge.app.web.dto.AuthResponse;
import com.tenantforge.app.web.dto.LoginRequest;
import com.tenantforge.app.web.dto.RefreshRequest;
import com.tenantforge.app.web.dto.RegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints: register tenant + owner, login, and refresh tokens.
 * Exposes JWT-based flow for the backend API consumers.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Register a tenant and its primary owner, returning an access/refresh token pair. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
        TokenPair tokens = authService.registerTenant(
                request.tenantName(), request.email(), request.password(), request.displayName());
        return ResponseEntity.ok(toResponse(tokens));
    }

    /** Login with email/password and retrieve a new token pair. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokens = authService.login(request.email(), request.password());
        return ResponseEntity.ok(toResponse(tokens));
    }

    /** Refresh tokens using a valid refresh token. */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenPair tokens = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(toResponse(tokens));
    }

    private AuthResponse toResponse(TokenPair tokenPair) {
        return new AuthResponse(
                tokenPair.accessToken(), tokenPair.accessExpiresIn(), tokenPair.refreshToken(), tokenPair.refreshExpiresIn());
    }
}
