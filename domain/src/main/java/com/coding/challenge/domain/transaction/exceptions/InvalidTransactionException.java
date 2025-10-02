package com.coding.challenge.domain.transaction.exceptions;

public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message){
        super(message);
    }
    
}
