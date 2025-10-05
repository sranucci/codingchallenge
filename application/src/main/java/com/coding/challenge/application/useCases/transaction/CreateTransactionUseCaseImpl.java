package com.coding.challenge.application.useCases.transaction;

import org.springframework.stereotype.Component;

import com.coding.challenge.application.useCases.transaction.support.TransactionMapper;
import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.transaction.TransactionRepository;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionCodes;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionException;
import com.coding.challenge.domain.transaction.providers.ValidTransactionTypeProvider;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionRequest;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionUseCase;

import lombok.AllArgsConstructor;



@Component
@AllArgsConstructor
public class CreateTransactionUseCaseImpl implements CreateTransactionUseCase {

    private final ValidTransactionTypeProvider validTransactionTypesService;
    private final TransactionRepository transactionRepository;

    @Override
    public boolean createTransaction(CreateTransactionRequest request) {

        if (!validTransactionTypesService.getValidTransactionTypes().contains(request.getType())){
            throw new InvalidTransactionException(InvalidTransactionCodes.ERR_NONEXISTENT_TYPE);
        }

        if (request.getParentTransactionId() != null && transactionRepository.findById(request.getParentTransactionId()).isEmpty()){
            throw new InvalidTransactionException(InvalidTransactionCodes.ERR_NONEXISTENT_PARENT_TRANSACTION);
        }

        Transaction transaction = TransactionMapper.MAPPER.toTransaction(request);
        Transaction oldTransaction = transactionRepository.saveTransaction(transaction);

        return oldTransaction == null;
    }

    
}
