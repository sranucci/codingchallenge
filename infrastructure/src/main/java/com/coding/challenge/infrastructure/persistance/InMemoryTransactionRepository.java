package com.coding.challenge.infrastructure.persistance;


import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.transaction.TransactionRepository;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {




    @Override
    public Set<Long> findTransactionIdsForType(String type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findTransactionIdsForType'");
    }

    @Override
    public Optional<BigDecimal> calculateChildSum(long transactionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculateChildSum'");
    }

    @Override
    public Optional<Transaction> findById(long transactionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTransaction'");
    }
    
}
