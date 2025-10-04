package com.coding.challenge.infrastructure.persistance;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.coding.challenge.domain.common.exceptions.InvalidApplicationStateException;
import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.transaction.TransactionRepository;

public class InMemoryTransactionRepositoryTests {

    TransactionRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryTransactionRepository();
    }

    @Test
    void givenExistingTransaction_whenFindById_thenReturnTransaction() {
        // Arrange
        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setType("cars");
        tx.setAmount(new BigDecimal("5000"));
        repository.saveTransaction(tx);

        // Act
        Optional<Transaction> result = repository.findById(10L);

        // Assert
        assertTrue(result.isPresent(), "Expected transaction to be found");
        assertEquals(10L, result.get().getId());
        assertEquals("cars", result.get().getType());
        assertEquals(0, new BigDecimal("5000").compareTo(result.get().getAmount()));
    }

    @Test
    void givenNonExistingTransaction_whenFindById_thenReturnEmptyOptional() {
        // Act
        Optional<Transaction> result = repository.findById(999L);

        // Assert
        assertTrue(result.isEmpty(), "Expected empty optional for non-existing transaction");
    }

    @Test
    void givenNonRepeatedTransaction_whenSaveTransaction_thenSaveSuccess() {
        // Arrange
        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setType("cars");
        tx.setAmount(new BigDecimal("5000"));
        tx.setParentTransactionId(null);

        // Act
        Transaction saved = repository.saveTransaction(tx);

        // Assert (return value)
        assertNotNull(saved, "createTransaction should return the saved entity");
        assertEquals(10L, saved.getId());
        assertEquals("cars", saved.getType());
        assertEquals(0, new BigDecimal("5000").compareTo(saved.getAmount()));
        assertNull(saved.getParentTransactionId());

        // Assert (read path to confirm persistence)
        Optional<Transaction> fetched = repository.findById(10L);
        assertTrue(fetched.isPresent(), "Transaction should exist in repository");
        assertEquals("cars", fetched.get().getType());
        assertEquals(0, new BigDecimal("5000").compareTo(fetched.get().getAmount()));
        assertNull(fetched.get().getParentTransactionId());
    }

    @Test
    void givenExistingTransaction_whenSaveTransaction_thenUpdate() {
        // Arrange
        Transaction tx1 = new Transaction();
        tx1.setId(10L);
        tx1.setType("cars");
        tx1.setAmount(new BigDecimal("5000"));
        tx1.setParentTransactionId(null);
        repository.saveTransaction(tx1);

        Transaction tx2 = new Transaction();
        tx2.setId(10L);
        tx2.setType("shopping");
        tx2.setAmount(new BigDecimal("7000"));
        tx2.setParentTransactionId(null);

        // Act
        Transaction updated = repository.saveTransaction(tx2);

        // Assert (return value)
        assertNotNull(updated, "updateTransaction should return the updated entity");
        assertEquals(10L, updated.getId());
        assertEquals("shopping", updated.getType(), "Type should be updated");
        assertEquals(0, new BigDecimal("7000").compareTo(updated.getAmount()), "Amount should be updated");
        assertNull(updated.getParentTransactionId());

        // Assert (read path to confirm persistence)
        Optional<Transaction> fetched = repository.findById(10L);
        assertTrue(fetched.isPresent(), "Transaction should exist after update");
        assertEquals("shopping", fetched.get().getType());
        assertEquals(0, new BigDecimal("7000").compareTo(fetched.get().getAmount()));
        assertNull(fetched.get().getParentTransactionId());
    }

    @Test
    void givenExistingParentTransaction_whenSaveTransaction_thenLink() {
        // Arrange: parent transaction
        Transaction parent = new Transaction();
        parent.setId(100L);
        parent.setType("cars");
        parent.setAmount(new BigDecimal("5000"));
        repository.saveTransaction(parent);

        // Act: insert a brand new child already linked to parent
        Transaction child = new Transaction();
        child.setId(101L);
        child.setType("shopping");
        child.setAmount(new BigDecimal("7000"));
        child.setParentTransactionId(100L); // directly link here
        Transaction savedChild = repository.saveTransaction(child);

        // Assert return value
        assertNotNull(savedChild);
        assertEquals(101L, savedChild.getId());
        assertEquals(100L, savedChild.getParentTransactionId());

        // Assert via read path
        Optional<Transaction> fetched = repository.findById(101L);
        assertTrue(fetched.isPresent(), "Child should exist after insert");
        assertEquals(100L, fetched.get().getParentTransactionId(), "Parent link should be set on insert");
    }

    @Test
    void givenExistingParentTransaction_whenSaveTransactionUpdatesExisting_thenLink() {
        // Arrange: parent transaction
        Transaction parent = new Transaction();
        parent.setId(100L);
        parent.setType("cars");
        parent.setAmount(new BigDecimal("5000"));
        repository.saveTransaction(parent);

        // Arrange: insert child WITHOUT parent link
        Transaction child = new Transaction();
        child.setId(101L);
        child.setType("shopping");
        child.setAmount(new BigDecimal("7000"));
        child.setParentTransactionId(null);
        repository.saveTransaction(child);

        // Act: update same child (same ID) to add parent link
        Transaction childUpdate = new Transaction();
        childUpdate.setId(101L); // same ID
        childUpdate.setType("shopping");
        childUpdate.setAmount(new BigDecimal("7000"));
        childUpdate.setParentTransactionId(100L); // now link to parent
        Transaction savedUpdate = repository.saveTransaction(childUpdate);

        // Assert return value
        assertNotNull(savedUpdate, "saveTransaction should return updated entity");
        assertEquals(101L, savedUpdate.getId());
        assertEquals("shopping", savedUpdate.getType());
        assertEquals(0, new BigDecimal("7000").compareTo(savedUpdate.getAmount()));
        assertEquals(100L, savedUpdate.getParentTransactionId(), "Parent link should be set on update");

        // Assert via read path to confirm persistence
        Optional<Transaction> fetched = repository.findById(101L);
        assertTrue(fetched.isPresent(), "Child should still exist after update");
        assertEquals(100L, fetched.get().getParentTransactionId(), "Parent link should persist after update");
    }

    @Test
    void givenTransactionsOfDifferentTypes_whenFindTransactionIdsForType_thenReturnOnlyMatchingIds() {
        // Arrange
        Transaction tx1 = new Transaction();
        tx1.setId(10L);
        tx1.setType("cars");
        tx1.setAmount(new BigDecimal("5000"));
        repository.saveTransaction(tx1);

        Transaction tx2 = new Transaction();
        tx2.setId(11L);
        tx2.setType("shopping");
        tx2.setAmount(new BigDecimal("7000"));
        repository.saveTransaction(tx2);

        Transaction tx3 = new Transaction();
        tx3.setId(12L);
        tx3.setType("cars");
        tx3.setAmount(new BigDecimal("9000"));
        repository.saveTransaction(tx3);

        // Act
        Set<Long> carIds = repository.findTransactionIdsForType("cars");
        Set<Long> shoppingIds = repository.findTransactionIdsForType("shopping");
        Set<Long> foodIds = repository.findTransactionIdsForType("food"); // no match

        // Assert
        assertNotNull(carIds);
        assertTrue(carIds.contains(10L), "cars should include transaction 10");
        assertTrue(carIds.contains(12L), "cars should include transaction 12");
        assertEquals(2, carIds.size(), "cars should return exactly 2 IDs");

        assertNotNull(shoppingIds);
        assertTrue(shoppingIds.contains(11L), "shopping should include transaction 11");
        assertEquals(1, shoppingIds.size(), "shopping should return exactly 1 ID");

        assertNotNull(foodIds);
        assertTrue(foodIds.isEmpty(), "food should return no IDs");
    }

    @Test
    void givenNonExistentType_whenFindTransactionIdsForType_thenThrowInvalidApplicationStateException() {
        // Arrange: repository has only "cars" transactions
        Transaction tx1 = new Transaction();
        tx1.setId(10L);
        tx1.setType("cars");
        tx1.setAmount(new BigDecimal("5000"));
        repository.saveTransaction(tx1);

        // Act
        InvalidApplicationStateException ex = assertThrows(
                InvalidApplicationStateException.class,
                () -> repository.findTransactionIdsForType("food"),
                "Should throw InvalidApplicationStateException when no transactions found for the given type");

        // Assert exception message
        assertEquals("No transactions found for type: food", ex.getMessage());
    }

    @Test
    void givenNonExistingTransaction_whenCalculateChildSum_thenReturnEmpty() {
        // Act
        Optional<BigDecimal> result = repository.calculateChildSum(999L);

        // Assert
        assertTrue(result.isEmpty(), "Expected Optional.empty when transaction does not exist");
    }

    @Test
    void givenTransactionWithChildren_whenCalculateChildSum_thenReturnSum() {
        // Arrange: root transaction
        Transaction root = new Transaction();
        root.setId(10L);
        root.setType("cars");
        root.setAmount(new BigDecimal("5000"));
        root.setParentTransactionId(null);
        repository.saveTransaction(root);

        // Child 1
        Transaction child1 = new Transaction();
        child1.setId(11L);
        child1.setType("shopping");
        child1.setAmount(new BigDecimal("7000"));
        child1.setParentTransactionId(10L);
        repository.saveTransaction(child1);

        // Child 2
        Transaction child2 = new Transaction();
        child2.setId(12L);
        child2.setType("shopping");
        child2.setAmount(new BigDecimal("3000"));
        child2.setParentTransactionId(10L);
        repository.saveTransaction(child2);

        // Act
        Optional<BigDecimal> result = repository.calculateChildSum(10L);

        // Assert
        assertTrue(result.isPresent(), "Expected a sum to be present");
        assertEquals(
                0, new BigDecimal("15000").compareTo(result.get()),
                "Sum should be root (5000) + child1 (7000) + child2 (3000) = 15000");
    }

}
