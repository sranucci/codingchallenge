package com.coding.challenge.domain.useCases.transaction.findTransactionsByType;

import java.util.Set;

import com.coding.challenge.domain.primitives.BaseUseCaseMarker;

public interface FindTransactionIdsByTypeUseCase extends BaseUseCaseMarker {
    
    Set<Long> findIdsByType(String type);
}
