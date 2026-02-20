package com.oscar.cardplatform.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull @Positive BigDecimal amount,
        String description
) {}
