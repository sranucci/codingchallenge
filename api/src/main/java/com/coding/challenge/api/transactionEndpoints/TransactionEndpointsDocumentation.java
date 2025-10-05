package com.coding.challenge.api.transactionEndpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.coding.challenge.api.common.dtos.response.ErrorResponse;
import com.coding.challenge.api.common.dtos.response.StatusOkResponse;
import com.coding.challenge.api.transactionEndpoints.dtos.requests.PutTransactionRequest;
import com.coding.challenge.api.transactionEndpoints.dtos.responses.GetTransactionSumResponse;

import java.util.List;
import java.util.Set;

@Tag(name = "Transactions", description = "Operations for managing transactions")
@RequestMapping("/transactions")
@Validated
public interface TransactionEndpointsDocumentation {

    @Operation(summary = "Upsert a transaction", description = "Creates or updates a transaction with the given ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction stored", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StatusOkResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid parent transaction Id", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{transactionId:\\d+}")
    ResponseEntity<StatusOkResponse> putTransaction(
            @Parameter(description = "Unique transaction ID", example = "1001") @PathVariable @Min(1) long transactionId,
            @Valid @RequestBody PutTransactionRequest body);

    @Operation(summary = "Get all transaction IDs by type", description = "Returns a set of transaction IDs for the given transaction type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "IDs found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(type = "integer", format = "int64", description = "Transaction ID")))),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("types/{type}")
    ResponseEntity<Set<Long>> getTransactionIdsByType(
            @Parameter(description = "Transaction type", example = "cars") @PathVariable @NotBlank @Size(max = 64) String type);

    @Operation(summary = "Get the total sum of a transaction graph", description = "Returns the sum of amounts for the given transaction, including its children.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sum calculated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetTransactionSumResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/sum/{transactionId:\\d+}")
    GetTransactionSumResponse getSum(
            @Parameter(description = "Transaction ID to calculate sum for", example = "200") long transactionId);
}
