package com.tenantforge.app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Email must be valid") String email,
        @NotBlank(message = "Password is required") String password) {}
