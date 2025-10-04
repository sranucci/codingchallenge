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
import org.springframework.web.bind.annotation.*;

import com.coding.challenge.api.common.dtos.response.ErrorResponse;
import com.coding.challenge.api.common.dtos.response.StatusOkResponse;
import com.coding.challenge.api.transactionEndpoints.dtos.requests.PutTransactionRequest;
import com.coding.challenge.api.transactionEndpoints.dtos.responses.GetTransactionSumResponse;

import java.util.List;


@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Operations for managing transactions")
public interface TransactionEndpointsDocumentation {

    @Operation(
        summary = "Upsert a transaction",
        description = "Creates or updates a transaction with the given ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction stored",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StatusOkResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    StatusOkResponse putTransaction(
        @Parameter(description = "Unique transaction ID", example = "1001")
        @Min(1) long transactionId,
        @Valid @RequestBody PutTransactionRequest body
    );

    @Operation(
        summary = "Get all transaction IDs by type",
        description = "Returns a list of transaction IDs for the given type."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "IDs found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = Long.class)))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    List<Long> getTransactionIdsByType(
        @Parameter(description = "Transaction type", example = "cars")
        @PathVariable("type")
        @NotBlank @Size(max = 64) String type
    );

    @Operation(
        summary = "Get the total sum of a transaction graph",
        description = "Returns the sum of amounts for the given transaction, including its children."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sum calculated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GetTransactionSumResponse.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    GetTransactionSumResponse getSum(
        @Parameter(description = "Transaction ID to calculate sum for", example = "200")
        long transactionId
    );
}
