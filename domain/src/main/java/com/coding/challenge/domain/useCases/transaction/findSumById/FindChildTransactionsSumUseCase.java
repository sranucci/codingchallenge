package com.coding.challenge.domain.useCases.transaction.findSumById;

import java.math.BigDecimal;
import java.util.Optional;

public interface FindChildTransactionsSumUseCase {
    Optional<BigDecimal> findTransitiveSum(long transactionId);
}
