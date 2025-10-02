package com.coding.challenge.domain.useCases.transaction.findTransactionsByType;

import java.util.Set;

public interface FindTransactionIdsByTypeUseCase {
    
    Set<Long> findIdsByType(String type);
}
