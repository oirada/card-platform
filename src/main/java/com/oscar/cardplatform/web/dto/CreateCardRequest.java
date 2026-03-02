package com.oscar.cardplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CreateCardRequest(
        @NotBlank @Size(min = 16, max = 19)
        String pan,

        @NotBlank
        String titular,

        @NotBlank @Size(min = 10, max = 15)
        String cedula,

        @NotBlank @Pattern(regexp = "(?i)^(Credito|Debito)$")
        String tipo,

        @NotBlank @Size(min = 10, max = 10)
        String telefono
) {}
