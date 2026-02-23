package com.oscar.cardplatform.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Solicitud para crear una nueva tarjeta")
public record CreateCardRequest(
        @NotBlank @Size(min = 16, max = 19)
        @Schema(description = "Número de la tarjeta (PAN)", example = "4111111111111111")
        String pan,

        @NotBlank
        @Schema(description = "Titular de la tarjeta", example = "John Doe")
        String titular,

        @NotBlank @Size(min = 10, max = 15)
        @Schema(description = "Cédula del titular", example = "1234567890")
        String cedula,

        @NotBlank @Pattern(regexp = "(?i)^(Credito|Debito)$")
        @Schema(description = "Tipo de tarjeta", example = "Credito", allowableValues = {"Credito", "Debito"})
        String tipo,

        @NotBlank @Size(min = 10, max = 10)
        @Schema(description = "Teléfono de contacto (10 dígitos)", example = "0987654321")
        String telefono
) {}
