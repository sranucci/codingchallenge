package com.coding.challenge.domain.useCases.transaction.createTransaction;

import com.coding.challenge.domain.transaction.Transaction;

public interface CreateTransactionUseCase {
    boolean createTransaction(CreateTransactionRequest request);
}
