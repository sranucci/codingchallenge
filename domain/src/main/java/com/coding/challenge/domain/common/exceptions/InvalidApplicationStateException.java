package com.coding.challenge.domain.common.exceptions;

public class InvalidApplicationStateException extends RuntimeException {

    public InvalidApplicationStateException(String message) {
        super(message);
    }
    
}
