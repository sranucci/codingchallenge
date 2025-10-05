package com.coding.challenge.domain.useCases.transaction.findSumById;

import java.math.BigDecimal;
import java.util.Optional;

import com.coding.challenge.domain.primitives.BaseUseCaseMarker;

public interface FindChildTransactionsSumUseCase extends BaseUseCaseMarker{
    Optional<BigDecimal> findTransitiveSum(long transactionId);
}
