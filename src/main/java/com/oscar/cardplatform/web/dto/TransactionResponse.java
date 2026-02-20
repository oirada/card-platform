package com.oscar.cardplatform.web.dto;

import java.math.BigDecimal;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String status
) {}
