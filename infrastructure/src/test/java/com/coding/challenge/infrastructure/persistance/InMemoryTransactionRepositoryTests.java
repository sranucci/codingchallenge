package com.coding.challenge.infrastructure.persistance;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.transaction.TransactionRepository;
import com.coding.challenge.infrastructure.common.ValidTransactionTypeProps;

class InMemoryTransactionRepositoryTests {

    TransactionRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryTransactionRepository(new ValidTransactionTypeProps(Set.of("cars","shopping")));
    }

    // ---------- findById ----------
    @Nested
    class FindByIdTests {

        @Test
        void givenExistingTransaction_whenFindById_thenReturnTransaction() {
            Transaction tx = new Transaction(10L,"cars",new BigDecimal("5000"),null);
           
            repository.saveTransaction(tx);

            Optional<Transaction> result = repository.findById(10L);

            assertTrue(result.isPresent(), "Expected transaction to be found");
            assertEquals(10L, result.get().getId());
            assertEquals("cars", result.get().getType());
            assertEquals(0, new BigDecimal("5000").compareTo(result.get().getAmount()));
        }

        @Test
        void givenNonExistingTransaction_whenFindById_thenReturnEmptyOptional() {
            Optional<Transaction> result = repository.findById(999L);
            assertTrue(result.isEmpty(), "Expected empty optional for non-existing transaction");
        }
    }

    // ---------- saveTransaction ----------
    @Nested
    class SaveTransactionTests {

        @Test
        void givenNonRepeatedTransaction_whenSaveTransaction_thenSaveSuccess() {
            Transaction tx = new Transaction(10L, "cars", new BigDecimal("5000"), null);

            Transaction saved = repository.saveTransaction(tx);

            // return value
            assertNotNull(saved, "createTransaction should return the saved entity");
            assertEquals(10L, saved.getId());
            assertEquals("cars", saved.getType());
            assertEquals(0, new BigDecimal("5000").compareTo(saved.getAmount()));
            assertNull(saved.getParentTransactionId());

            // confirm persistence
            Optional<Transaction> fetched = repository.findById(10L);
            assertTrue(fetched.isPresent(), "Transaction should exist in repository");
            assertEquals("cars", fetched.get().getType());
            assertEquals(0, new BigDecimal("5000").compareTo(fetched.get().getAmount()));
            assertNull(fetched.get().getParentTransactionId());
        }

        @Test
        void givenExistingTransaction_whenSaveTransaction_thenUpdate() {
            Transaction tx1 = new Transaction(10L, "cars", new BigDecimal("5000"), null);
            repository.saveTransaction(tx1);

            Transaction tx2 = new Transaction(10L, "shopping", new BigDecimal("7000"), null);

            Transaction updated = repository.saveTransaction(tx2);

            // return value
            assertNotNull(updated, "updateTransaction should return the updated entity");
            assertEquals(10L, updated.getId());
            assertEquals("shopping", updated.getType(), "Type should be updated");
            assertEquals(0, new BigDecimal("7000").compareTo(updated.getAmount()), "Amount should be updated");
            assertNull(updated.getParentTransactionId());

            // confirm persistence
            Optional<Transaction> fetched = repository.findById(10L);
            assertTrue(fetched.isPresent(), "Transaction should exist after update");
            assertEquals("shopping", fetched.get().getType());
            assertEquals(0, new BigDecimal("7000").compareTo(fetched.get().getAmount()));
            assertNull(fetched.get().getParentTransactionId());
        }
        @Test
        void givenExistingParentTransaction_whenSaveTransaction_thenLink() {
            Transaction parent = new Transaction(100L, "cars", new BigDecimal("5000"), null);
            repository.saveTransaction(parent);

            Transaction child = new Transaction(101L, "shopping", new BigDecimal("7000"), 100L);
            Transaction savedChild = repository.saveTransaction(child);

            assertNotNull(savedChild);
            assertEquals(101L, savedChild.getId());
            assertEquals(100L, savedChild.getParentTransactionId());

            Optional<Transaction> fetched = repository.findById(101L);
            assertTrue(fetched.isPresent(), "Child should exist after insert");
            assertEquals(100L, fetched.get().getParentTransactionId(), "Parent link should be set on insert");
        }

        @Test
        void givenExistingParentTransaction_whenSaveTransactionUpdatesExisting_thenLink() {
            Transaction parent = new Transaction(100L, "cars", new BigDecimal("5000"), null);
            repository.saveTransaction(parent);

            Transaction child = new Transaction(101L, "shopping", new BigDecimal("7000"), null);
            repository.saveTransaction(child);

            Transaction childUpdate = new Transaction(101L, "shopping", new BigDecimal("7000"), 100L);
            Transaction savedUpdate = repository.saveTransaction(childUpdate);

            assertNotNull(savedUpdate, "saveTransaction should return updated entity");
            assertEquals(101L, savedUpdate.getId());
            assertEquals("shopping", savedUpdate.getType());
            assertEquals(0, new BigDecimal("7000").compareTo(savedUpdate.getAmount()));
            assertEquals(100L, savedUpdate.getParentTransactionId(), "Parent link should be set on update");

            Optional<Transaction> fetched = repository.findById(101L);
            assertTrue(fetched.isPresent(), "Child should still exist after update");
            assertEquals(100L, fetched.get().getParentTransactionId(), "Parent link should persist after update");
        }
    }
}
