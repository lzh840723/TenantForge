package com.tenantforge.app.web.dto;

public record AuthResponse(
        String accessToken,
        long accessExpiresIn,
        String refreshToken,
        long refreshExpiresIn) {}
