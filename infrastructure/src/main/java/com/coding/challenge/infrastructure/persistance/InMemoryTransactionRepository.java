package com.coding.challenge.infrastructure.persistance;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Repository;

import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.transaction.TransactionRepository;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionCodes;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionException;
import com.coding.challenge.domain.transaction.providers.ValidTransactionTypeProvider;
import com.coding.challenge.infrastructure.common.ValidTransactionTypeProps;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {


    private Set<String> allowedTypes;

    public InMemoryTransactionRepository(ValidTransactionTypeProps props) {
        for (String type : props.allowedTypes()) {
            typesToTransaction.put(type, new HashSet<>());
        }
        this.allowedTypes = props.allowedTypes();
    }

    Map<Long, Transaction> idToTransaction = new ConcurrentHashMap<>();
    Map<String, Set<Long>> typesToTransaction = new ConcurrentHashMap<>();
    Map<Long, Set<Long>> parentIdToChildId = new ConcurrentHashMap<>();

    @Override
    public Set<Long> findTransactionIdsForType(String type) {
        AtomicReference<Set<Long>> idsSnapshot = new AtomicReference<>(Set.of());
        typesToTransaction.compute(type, (k, s) -> {
            idsSnapshot.set(Set.copyOf(s));
            return s;
        });
        return idsSnapshot.get();
    }

    @Override
    public Optional<BigDecimal> calculateChildSum(long transactionId) {

        // ID nunca va a cambiar
        Transaction tx = idToTransaction.get(transactionId);

        if (tx == null) {
            return Optional.empty();
        }

        BigDecimal sum = BigDecimal.ZERO;
        Deque<Long> stack = new ArrayDeque<>();
        Set<Long> visitedTransactionIds = new HashSet<>();

        stack.push(transactionId);

        while (!stack.isEmpty()) {
            Long currentId = stack.pop();
            if (!visitedTransactionIds.add(currentId))
                continue;

            Transaction t = idToTransaction.get(currentId);
            sum = sum.add(t.getAmount());

            AtomicReference<Set<Long>> childrenIdsSnapshot = new AtomicReference<>(Set.of());

            parentIdToChildId.compute(t.getId(), (key, set) -> {
                childrenIdsSnapshot.set(Set.copyOf(set));
                return set;
            });

            childrenIdsSnapshot.get().forEach(stack::push);
        }

        return Optional.of(sum);

    }

    @Override
    public Optional<Transaction> findById(long transactionId) {
        return Optional.ofNullable(idToTransaction.get(transactionId));
    }

    private void throwInvalidTxExceptionIfParentIdNotExists(Transaction transaction) {
        if (transaction.getParentTransactionId() != null
                && !parentIdToChildId.containsKey(transaction.getParentTransactionId())) {
            throw new InvalidTransactionException(InvalidTransactionCodes.ERR_NONEXISTENT_PARENT_TRANSACTION);
        }
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {

        // Primero aseguramos existencia de transaccion padre,funciona aprovechando que
        // un ID no se puede modificar una vez creado.
        throwInvalidTxExceptionIfParentIdNotExists(transaction);

        return idToTransaction.compute(transaction.getId(), (key, oldTx) -> {
            if (oldTx == null) {
                return createNewTransaction(transaction);
            }
            return updateTransaction(transaction, oldTx);
        });

    }

    private Transaction updateTransaction(Transaction newTransaction, Transaction oldTransaction) {

        if (oldTransaction.getParentTransactionId() != null) {
            parentIdToChildId.compute(oldTransaction.getParentTransactionId(), (key, set) -> {
                set.remove(oldTransaction.getId());
                return set;
            });
        }

        if (newTransaction.getParentTransactionId() != null) {
            parentIdToChildId.compute(newTransaction.getParentTransactionId(), (key, set) -> {
                set.add(newTransaction.getId());
                return set;
            });
        }

        typesToTransaction.compute(oldTransaction.getType(), (key, set) -> {
            set.remove(oldTransaction.getId());
            return set;
        });

        typesToTransaction.compute(newTransaction.getType(), (key, set) -> {
            set.add(newTransaction.getId());
            return set;
        });

        return newTransaction;
    }

    private Transaction createNewTransaction(Transaction transaction) {
        parentIdToChildId.computeIfAbsent(transaction.getId(), k -> new HashSet<>());// No hace falta que este sea
                                                                                     // concurrente, solo 1 acceso
                                                                                     // siempre, lo proteje la key padre

        typesToTransaction.compute(transaction.getType(), (type, idSet) -> {
            idSet.add(transaction.getId());
            return idSet;
        });

        if ((transaction.getParentTransactionId() != null)) {
            parentIdToChildId.compute(transaction.getParentTransactionId(), (id, set) -> {
                set.add(transaction.getId());
                return set;
            });
        }

        return transaction;

    }

}
