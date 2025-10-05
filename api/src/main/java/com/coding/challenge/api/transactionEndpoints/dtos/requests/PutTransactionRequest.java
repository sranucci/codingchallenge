package com.coding.challenge.api.transactionEndpoints.dtos.requests;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "TransactionUpsertRequest", description = "Payload to insert or update a transaction")
public record PutTransactionRequest(

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount must be > 0")
    @Schema(description = "Transaction amount", example = "5000.75")
    BigDecimal amount,

    @NotBlank(message = "type is required")
    @Size(max = 64, message = "type max length is 64")
    @Schema(description = "Business type/category of the transaction", example = "cars")
    String type,

    @Positive(message = "parent_id must be > 0")
    @Schema(description = "Optional parent transaction id", example = "100", nullable = true)
    @JsonProperty("parent_id")
    Long parentId
) {}
