package com.coding.challenge.application.useCases.transaction;

import org.springframework.stereotype.Component;

import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionRequest;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionUseCase;



@Component
public class CreateTransactionUseCaseImpl implements CreateTransactionUseCase {

    @Override
    public Transaction createTransaction(CreateTransactionRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTransaction'");
    }
    
}
