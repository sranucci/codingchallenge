package com.coding.challenge.domain.useCases.transaction.createTransaction;

import com.coding.challenge.domain.primitives.BaseUseCaseMarker;
import com.coding.challenge.domain.transaction.Transaction;

public interface CreateTransactionUseCase extends BaseUseCaseMarker{
    boolean createTransaction(CreateTransactionRequest request);
}
