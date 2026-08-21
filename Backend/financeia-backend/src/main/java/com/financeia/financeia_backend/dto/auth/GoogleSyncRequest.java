package com.financeia.financeia_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GoogleSyncRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String name
) {
}
