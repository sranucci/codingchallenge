package com.coding.challenge.domain.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransactionRepository {

    Transaction saveTransaction(Transaction transaction);
    Set<Long> findTransactionIdsForType(String type);
    BigDecimal calculateChildSum(long transactionId);
    
}
