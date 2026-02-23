package com.oscar.cardplatform.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para enrolar una tarjeta")
public record EnrolCardRequest(
        @NotBlank
        @Schema(description = "Identificador único de la tarjeta", example = "abc123def456")
        String identificador,

        @NotNull
        @Schema(description = "Número de validación generado al crear la tarjeta (1-100)", example = "42")
        Integer numeroValidacion
) {}

