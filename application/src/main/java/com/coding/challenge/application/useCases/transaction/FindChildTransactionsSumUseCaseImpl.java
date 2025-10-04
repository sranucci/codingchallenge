package com.coding.challenge.application.useCases.transaction;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.coding.challenge.domain.useCases.transaction.findSumById.FindChildTransactionsSumUseCase;


@Component
public class FindChildTransactionsSumUseCaseImpl implements FindChildTransactionsSumUseCase{

    @Override
    public Optional<BigDecimal> findTransitiveSum(long transactionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findTransitiveSum'");
    }
    
}
