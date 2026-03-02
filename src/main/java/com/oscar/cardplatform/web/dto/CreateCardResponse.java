package com.oscar.cardplatform.web.dto;

public record CreateCardResponse(
        String codigo,

        String mensaje,

        Integer numeroValidacion,

        String panEnmascarado,

        String identificador
) {}
