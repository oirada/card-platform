package com.oscar.cardplatform.web.dto;

public record CreateCardResponse(
        String codigo,        // 00 | 01
        String mensaje,       // Éxito | Fallido
        Integer numeroValidacion,
        String panEnmascarado,
        String identificador
) {}
