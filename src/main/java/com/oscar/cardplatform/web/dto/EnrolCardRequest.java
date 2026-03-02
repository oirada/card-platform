package com.oscar.cardplatform.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnrolCardRequest(
        @NotBlank
        String identificador,

        @NotNull
        Integer numeroValidacion
) {}

