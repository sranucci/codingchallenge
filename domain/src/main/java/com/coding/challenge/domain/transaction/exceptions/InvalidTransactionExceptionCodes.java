package com.coding.challenge.domain.transaction.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;



public enum InvalidTransactionExceptionCodes {
    ERR_REPEATED_TRANSACTION,
    ERR_NONEXISTENT_PARENT_TRANSACTION,
    ERR_NONEXISTENT_TYPE,
}
