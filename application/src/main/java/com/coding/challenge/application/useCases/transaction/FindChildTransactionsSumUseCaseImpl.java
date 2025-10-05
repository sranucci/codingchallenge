package com.coding.challenge.application.useCases.transaction;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.coding.challenge.domain.transaction.TransactionRepository;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionCodes;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionException;
import com.coding.challenge.domain.useCases.transaction.findSumById.FindChildTransactionsSumUseCase;

import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class FindChildTransactionsSumUseCaseImpl implements FindChildTransactionsSumUseCase{


    private final TransactionRepository transactionRepository;

    @Override
    public Optional<BigDecimal> findTransitiveSum(long transactionId) {
        return transactionRepository.calculateChildSum(transactionId);
    }
    
}
