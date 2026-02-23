package com.oscar.cardplatform.web.dto;

public record EnrolCardResponse(
        String codigo,
        String mensaje,
        String panEnmascarado
) {}

