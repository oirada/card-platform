package com.oscar.cardplatform.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de creación de tarjeta")
public record CreateCardResponse(
        @Schema(description = "Código de respuesta (00=éxito, 01=error)", example = "00")
        String codigo,

        @Schema(description = "Mensaje de respuesta", example = "Éxito")
        String mensaje,

        @Schema(description = "Número de validación para enrolamiento (1-100)", example = "42")
        Integer numeroValidacion,

        @Schema(description = "PAN enmascarado (primeros 6 y últimos 4 dígitos visibles)", example = "411111****1111")
        String panEnmascarado,

        @Schema(description = "Identificador único de la tarjeta (SHA256 del PAN + fecha)", example = "a3d4e5f6...")
        String identificador
) {}
