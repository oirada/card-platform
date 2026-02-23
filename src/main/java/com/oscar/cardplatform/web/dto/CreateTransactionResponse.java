package com.oscar.cardplatform.web.dto;

public record CreateTransactionResponse(
        String codigo,
        String mensaje,
        String estadoTransaccion,
        String referencia
) {}

