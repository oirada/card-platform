package com.oscar.cardplatform.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Solicitud para anular una transacción de compra")
public record AnnulTransactionRequest(
        @NotBlank
        @Schema(description = "Identificador único de la tarjeta", example = "abc123def456")
        String identificador,

        @NotBlank
        @Schema(description = "Número de referencia de la transacción a anular (6 dígitos)", example = "123456")
        String referencia,

        @NotNull
        @Schema(description = "Total de la compra a anular", example = "99.99")
        BigDecimal total
) {}

