package com.coding.challenge.api.common.dtos.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "StatusOkResponse", description = "Simple operation status response")
public record StatusOkResponse(
    @Schema(description = "Operation outcome", example = "ok")
    String status
) {
    public static StatusOkResponse ok() { return new StatusOkResponse("ok"); }
}
