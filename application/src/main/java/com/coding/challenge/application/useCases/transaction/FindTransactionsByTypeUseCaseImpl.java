package com.coding.challenge.application.useCases.transaction;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.coding.challenge.domain.useCases.transaction.findTransactionsByType.FindTransactionIdsByTypeUseCase;


@Component
public class FindTransactionsByTypeUseCaseImpl implements FindTransactionIdsByTypeUseCase{

    @Override
    public Set<Long> findIdsByType(String type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findIdsByType'");
    }
    
}
