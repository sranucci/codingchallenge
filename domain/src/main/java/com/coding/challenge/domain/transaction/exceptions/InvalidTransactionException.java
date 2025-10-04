package com.coding.challenge.domain.transaction.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InvalidTransactionException extends RuntimeException {
    private InvalidTransactionCodes code;
}
