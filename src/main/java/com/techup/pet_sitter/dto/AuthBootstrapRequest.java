package com.techup.pet_sitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthBootstrapRequest(
        @NotBlank String name,
        @NotBlank String phone,
        @Pattern(regexp = "owner|sitter") String role
) {
}
