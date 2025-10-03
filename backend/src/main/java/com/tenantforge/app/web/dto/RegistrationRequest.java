package com.tenantforge.app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "Tenant name is required") String tenantName,
        @Email(message = "Email must be valid") String email,
        @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank(message = "Display name is required") String displayName) {}
