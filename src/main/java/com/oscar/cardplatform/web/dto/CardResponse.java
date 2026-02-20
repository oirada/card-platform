package com.oscar.cardplatform.web.dto;

public record CardResponse(
                           Long id,
                           String maskedPan,
                           String status
) {}
