package com.oscar.cardplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotBlank
        String identificador,

        @NotBlank @Pattern(regexp = "\\d{6}")
        String referencia,

        @NotNull @Positive
        BigDecimal total,

        @NotBlank
        String direccion
) {}
