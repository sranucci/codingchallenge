package com.coding.challenge.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coding.challenge.api.common.dtos.response.ErrorResponse;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;

/**
 * Global exception handler for Jakarta Bean Validation / model binding errors.
 */
@Hidden
@RestControllerAdvice
public class ModelBindingExceptionMapper {

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception ex) {
        // We return a generic code — internal details are not exposed to clients
        ErrorResponse error = new ErrorResponse("ERR_BINDING");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
}
