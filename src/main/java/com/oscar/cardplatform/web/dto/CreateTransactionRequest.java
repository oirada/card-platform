package com.oscar.cardplatform.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Solicitud para crear una transacción de compra")
public record CreateTransactionRequest(
        @NotBlank
        @Schema(description = "Identificador único de la tarjeta", example = "abc123def456")
        String identificador,

        @NotBlank @Pattern(regexp = "\\d{6}")
        @Schema(description = "Número de referencia de la transacción (6 dígitos, único)", example = "123456")
        String referencia,

        @NotNull @Positive
        @Schema(description = "Total de la compra", example = "99.99")
        BigDecimal total,

        @NotBlank
        @Schema(description = "Dirección de compra", example = "Av Principal 123, Quito")
        String direccion
) {}
