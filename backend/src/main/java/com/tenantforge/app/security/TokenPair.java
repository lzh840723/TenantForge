package com.tenantforge.app.security;

public record TokenPair(String accessToken, String refreshToken, long accessExpiresIn, long refreshExpiresIn) {}
