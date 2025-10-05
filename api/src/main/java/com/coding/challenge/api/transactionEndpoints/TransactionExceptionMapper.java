package com.coding.challenge.api.transactionEndpoints;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coding.challenge.api.common.dtos.response.ErrorResponse;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionException;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
public class TransactionExceptionMapper {

    /**
     * Handles domain-level transaction validation errors.
     */
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransaction(InvalidTransactionException ex) {
        // Return the error code name in a simple payload
        ErrorResponse error = new ErrorResponse(ex.getCode().name());

        HttpStatusCode statusCode;
        switch (ex.getCode()) {
            case ERR_NONEXISTENT_PARENT_TRANSACTION:
                statusCode = HttpStatus.CONFLICT;
                break;
            case ERR_NONEXISTENT_TYPE:
                statusCode = HttpStatus.BAD_REQUEST;
                break;
            // nunca pasa...
            default:
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(statusCode).body(error);
    }

    /**
     * Handles any other unexpected runtime errors gracefully.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
