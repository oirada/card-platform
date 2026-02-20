package com.oscar.cardplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCardRequest(
        @NotBlank @Size(min = 16, max = 19)
        String pan,

        @NotBlank
        String titular,

        @NotBlank @Size(min = 10, max = 15)
        String cedula,

        @NotBlank
        String tipo, // "Credito" | "Debito"

        @NotBlank @Size(min = 10, max = 10)
        String telefono
) {}
