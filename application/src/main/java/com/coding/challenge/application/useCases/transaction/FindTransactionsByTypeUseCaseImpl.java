package com.coding.challenge.application.useCases.transaction;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.coding.challenge.domain.transaction.TransactionRepository;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionCodes;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionException;
import com.coding.challenge.domain.transaction.providers.ValidTransactionTypeProvider;
import com.coding.challenge.domain.useCases.transaction.findTransactionsByType.FindTransactionIdsByTypeUseCase;

import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class FindTransactionsByTypeUseCaseImpl implements FindTransactionIdsByTypeUseCase{


    private final TransactionRepository transactionRepository;
    private final ValidTransactionTypeProvider validTransactionTypesService;

    @Override
    public Set<Long> findIdsByType(String type) {
        if (!validTransactionTypesService.getValidTransactionTypes().contains(type)){
            throw new InvalidTransactionException(InvalidTransactionCodes.ERR_NONEXISTENT_TYPE);
        }
        return transactionRepository.findTransactionIdsForType(type);
    }
    
}
