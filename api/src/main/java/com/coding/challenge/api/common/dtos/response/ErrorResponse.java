package com.coding.challenge.api.common.dtos.response;


import io.swagger.v3.oas.annotations.media.Schema;

/** Standard error payload for validation/domain errors */
@Schema(name = "ErrorResponse", description = "Standard error payload")
public record ErrorResponse(
    @Schema(description = "Short error code or category", example = "BAD_REQUEST")
    String error,
    @Schema(description = "Human-readable message", example = "amount: amount must be > 0")
    String message
) {}

