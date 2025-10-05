package com.coding.challenge.infrastructure.providers;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.coding.challenge.domain.transaction.providers.ValidTransactionTypeProvider;
import com.coding.challenge.infrastructure.common.ValidTransactionTypeProps;

import lombok.AllArgsConstructor;



@Service
@AllArgsConstructor
public class InMemoryValidTransactionTypeProvider implements ValidTransactionTypeProvider {


    private final ValidTransactionTypeProps props;

    @Override
    public Set<String> getValidTransactionTypes() {
        return props.allowedTypes();
    }
    
}
