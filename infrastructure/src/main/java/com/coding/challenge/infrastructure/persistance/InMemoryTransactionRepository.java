package com.coding.challenge.infrastructure.persistance;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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
import com.coding.challenge.infrastructure.common.ValidTransactionTypeProps;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Set<String> validTypes;

    public InMemoryTransactionRepository(ValidTransactionTypeProps props) {
        for (String type : props.allowedTypes()) {
            typesToTransaction.put(type, new HashSet<>());
        }
        validTypes = Set.copyOf(props.allowedTypes());
    }

    Map<Long, ReentrantLock> locksPerId = new ConcurrentHashMap<>();
    Map<Long, Transaction> idToTransaction = new ConcurrentHashMap<>();
    Map<String, Set<Long>> typesToTransaction = new ConcurrentHashMap<>();
    Map<Long, Set<Long>> parentIdToChildId = new ConcurrentHashMap<>();

    @Override
    public Set<Long> findTransactionIdsForType(String type) {

        AtomicReference<Set<Long>> idsRef = new AtomicReference<>(Collections.emptySet());
        typesToTransaction.compute(type, (key, set) -> {
            idsRef.set(Set.copyOf(set));
            return set;
        });
        return idsRef.get();

    }

    @Override
    public Optional<BigDecimal> calculateChildSum(long transactionId) {

        // ID nunca va a cambiar, no hace falta lock
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

            ReentrantLock currentTxLock = takeLockFor(currentId);

            currentTxLock.lock();

            BigDecimal currentAmount;
            Set<Long> childSnapshot;
            try {
                Transaction t = idToTransaction.get(currentId);
                currentAmount = t.getAmount();
                AtomicReference<Set<Long>> childrenIdsSnapshot = new AtomicReference<>(Set.of());

                parentIdToChildId.compute(t.getId(), (key, set) -> {
                    childrenIdsSnapshot.set(Set.copyOf(set));
                    return set;
                });
                childSnapshot = childrenIdsSnapshot.get();
            } finally {
                currentTxLock.unlock();
            }

            sum = sum.add(currentAmount);
            childSnapshot.forEach(stack::push);
        }

        return Optional.of(sum);

    }

    @Override
    public Optional<Transaction> findById(long transactionId) {

        ReentrantLock lock = takeLockFor(transactionId);
        lock.lock();
        try {
            return Optional.ofNullable(idToTransaction.get(transactionId));
        } finally {
            lock.unlock();
        }
    }

    private void throwTxExceptionIfNoParentId(Transaction transaction) {
        if (transaction.getParentTransactionId() != null
                && !idToTransaction.containsKey(transaction.getParentTransactionId())) {
            throw new InvalidTransactionException(InvalidTransactionCodes.ERR_NONEXISTENT_PARENT_TRANSACTION);
        }
    }

    private void throwTxExceptionIfInvalidType(Transaction transaction) {
        // validate early
        if (!validTypes.contains(transaction.getType())) {
            throw new InvalidTransactionException(InvalidTransactionCodes.ERR_NONEXISTENT_TYPE);
        }
    }

    private ReentrantLock takeLockFor(long txId) {
        return locksPerId.computeIfAbsent(txId, k -> new ReentrantLock());
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        throwTxExceptionIfNoParentId(transaction);
        throwTxExceptionIfInvalidType(transaction);

        long currentTransactionId = transaction.getId();
        Long newParent = transaction.getParentTransactionId();

        // Se necesita tomar mutex en orden ascendente
        for (;;) {
            Long peekedOldParent = Optional.ofNullable(idToTransaction.get(transaction.getId()))
                    .map(Transaction::getParentTransactionId).orElse(null);

            List<ReentrantLock> acquiredLocks = acquireLocksInOrder(Arrays.asList(currentTransactionId, newParent, peekedOldParent));
            try {
                Long actualOldParent = Optional.ofNullable(idToTransaction.get(currentTransactionId))
                        .map(Transaction::getParentTransactionId)
                        .orElse(null);

                if (!Objects.equals(peekedOldParent, actualOldParent)) {
                    continue;
                }
                // Una vez que tenemos todos los mutex en forma ascendente podemos actualizar la
                // transaccion

                Transaction oldTransaction = idToTransaction.put(transaction.getId(), transaction);
                if (oldTransaction == null) {
                    createNewTransaction(transaction);
                } else {
                    updateTransaction(transaction, oldTransaction);
                }

                return oldTransaction;
            } finally {
                releaseLocksInReverseOrder(acquiredLocks);
            }

        }
    }

    private void updateTransaction(Transaction newTransaction, Transaction oldTransaction) {
        removeTransactionFromType(oldTransaction);
        addTransactionType(newTransaction);

        if (oldTransaction.getParentTransactionId() != null) {
            removeTransactionFromParentLink(oldTransaction);
        }

        if (newTransaction.getParentTransactionId() != null) {
            addTransactionToParentLink(newTransaction);
        }
    }

    private void initializeChildLinks(Transaction transaction){
        parentIdToChildId.putIfAbsent(transaction.getId(), new HashSet<>());
    }

    private void createNewTransaction(Transaction transaction) {
        addTransactionType(transaction);
        initializeChildLinks(transaction);
        if (transaction.getParentTransactionId() != null) {
            addTransactionToParentLink(transaction);
        }
    }

    private void removeTransactionFromType(Transaction transaction) {
        typesToTransaction.compute(transaction.getType(), (key, set) -> {
            set.remove(transaction.getId());
            return set;
        });
    }

    private void removeTransactionFromParentLink(Transaction transaction) {
        parentIdToChildId.compute(transaction.getParentTransactionId(), (key, set) -> {
            set.remove(transaction.getId());
            return set;
        });

    }

    private void addTransactionType(Transaction transaction) {
        typesToTransaction.compute(transaction.getType(), (key, set) -> {
            set.add(transaction.getId());
            return set;
        });
    }

    private void addTransactionToParentLink(Transaction transaction) {
        parentIdToChildId.compute(transaction.getParentTransactionId(), (key, set) -> {
            set.add(transaction.getId());
            return set;
        });

    }

    private List<ReentrantLock> acquireLocksInOrder(Collection<Long> ids) {
        List<Long> order = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<ReentrantLock> acquired = new ArrayList<>(order.size());
        for (Long id : order) {
            ReentrantLock l = takeLockFor(id);
            l.lock();
            acquired.add(l);
        }
        return acquired;
    }

    private void releaseLocksInReverseOrder(List<ReentrantLock> locks) {
        for (int i = locks.size() - 1; i >= 0; i--) {
            locks.get(i).unlock();
        }
    }

}
