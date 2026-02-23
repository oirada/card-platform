package com.oscar.cardplatform.web.dto;

public record CardDetailResponse(
        String panEnmascarado,
        String titular,
        String cedula,
        String telefono,
        String estado
) {}

