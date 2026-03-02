package com.oscar.cardplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AnnulTransactionRequest(
        @NotBlank
        String identificador,

        @NotBlank
        String referencia,

        @NotNull
        BigDecimal total
) {}

