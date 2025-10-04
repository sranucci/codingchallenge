package com.coding.challenge.domain.transaction;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface TransactionRepository {

    Optional<Transaction> findById(long transactionId);
    Transaction saveTransaction(Transaction transaction);
    Set<Long> findTransactionIdsForType(String type);
    Optional<BigDecimal> calculateChildSum(long transactionId);
    
}
