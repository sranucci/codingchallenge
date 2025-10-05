package com.coding.challenge.api.transactionEndpoints.dtos.responses;


import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SumResponse", description = "Sum of the amounts for a transaction graph")
public record GetTransactionSumResponse(
    @Schema(description = "Aggregate sum (root + all descendants)", example = "7500.5")
    BigDecimal sum
) {}