package com.coding.challenge.boot.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.transaction.TransactionRepository;
import com.coding.challenge.domain.transaction.exceptions.InvalidTransactionCodes;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class TransactionEndpointsTests {

    @Autowired
    private WebTestClient httpClient;

    @MockitoSpyBean
    private TransactionRepository repository;

    @BeforeEach
    void clearMockitoSpies() {
        Mockito.reset(repository);
    }

    @Nested
    class PutTransactionEndpointTests {

        @DirtiesContext
        @Test
        void whenValidTransaction_thenReturn201Created() {
            // Arrange
            long id = 10L;
            var body = Map.of(
                    "amount", 5000,
                    "type", "cars");

            // Act + Assert
            httpClient.put()
                    .uri("/transactions/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ok");
        }

        @DirtiesContext
        @Test
        void givenExistingTransaction_WhenNewTransactionWithSameId_ThenReplaceTransactionAndReturn200Ok() {
            var id = 10L;
            var amount = 5000;
            var type = "shopping";
            repository.saveTransaction(new Transaction(id, "cars", BigDecimal.valueOf(1), null));
            Mockito.clearInvocations(repository);

            var body = Map.of(
                    "amount", amount,
                    "type", type);
            // Act + Assert
            httpClient.put()
                    .uri("/transactions/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ok");

            verify(repository, times(1)).saveTransaction(Mockito.argThat(tx -> tx.getId() == id
                    &&
                    tx.getAmount().compareTo(BigDecimal.valueOf(amount)) == 0
                    &&
                    tx.getType().equals(type)));
        }

        @DirtiesContext
        @Test
        void whenNewTransactionWithInvalidParentId_ThenReturn409Conflict() {
            var id = 10L;
            var amount = 5000;
            var type = "shopping";

            var body = Map.of(
                    "amount", amount,
                    "type", type,
                    "parent_id", 12L);
            // Act + Assert
            httpClient.put()
                    .uri("/transactions/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody()
                    .jsonPath("$.errorCode")
                    .isEqualTo(InvalidTransactionCodes.ERR_NONEXISTENT_PARENT_TRANSACTION.name());

            verify(repository, times(0)).saveTransaction(any(Transaction.class));
        }

        @Test
        void whenInvalidAmount_ThenReturn400BadRequest() {
            var id = 20L;
            var body = Map.of(
                    "amount", -1000,
                    "type", "cars");
            httpClient.put()
                    .uri("/transactions/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(repository, times(0)).saveTransaction(any(Transaction.class));
        }

    }

    @Nested
    class GetTransactionIdsForTypeEndpointTests {

        @DirtiesContext
        @Test
        void givenExistingTransactionsForType_WhenValidType_ThenReturn200Ok() {
            // Arrange
            var type = "cars";
            for (int i = 1; i <= 5; i++) {
                repository.saveTransaction(new Transaction(Long.valueOf(i), type, BigDecimal.valueOf(1000 * i), null));
            }
            // Una no relacionada para chequear que el filtro ande
            repository.saveTransaction(new Transaction(99L, "shopping", BigDecimal.valueOf(9999), null));

            Mockito.clearInvocations(repository);

            // Act + Assert
            List<Long> ids = httpClient.get()
                    .uri("/transactions/types/{type}", type)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    // Expect 5 IDs in the response JSON array
                    .expectBodyList(Long.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(ids);
            assertEquals(5, ids.size());
            assertTrue(ids.containsAll(List.of(1L, 2L, 3L, 4L, 5L)));

            verify(repository, times(1)).findTransactionIdsForType(type);
        }

        @DirtiesContext
        @Test
        void givenNoTransactionsForType_WhenInvalidType_ThenReturnEmptyList204NoContent() {
            // Arrange
            var type = "cars";

            // Clear invocations
            Mockito.clearInvocations(repository);

            // Act + Assert
            httpClient.get()
                    .uri("/transactions/types/{type}", type)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

            verify(repository, times(1)).findTransactionIdsForType(type);
        }

        @DirtiesContext
        @Test
        void givenNonExistentType_WhenFetchingTransactions_ThenReturn404NotFound() {
            // Arrange
            var nonExistentType = "nonexistent-type";

            // Clear any previous spy interactions
            Mockito.clearInvocations(repository);

            // Act + Assert
            httpClient.get()
                    .uri("/transactions/types/{type}", nonExistentType)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.errorCode").isEqualTo(InvalidTransactionCodes.ERR_NONEXISTENT_TYPE.name());

            // Verify repository was called once for that type
            verify(repository, times(0)).findTransactionIdsForType(nonExistentType);
        }

    }

    @Nested
    class GetTransactionChildsSumEndpointTests {

        @Test
        void givenNonExistentTransactionId_whenGettingSum_thenReturn404NotFound() {
            var missingId = 100L;
            httpClient.get()
                    .uri("/transactions/sum/{id}", missingId)
                    .exchange()
                    .expectStatus().isNotFound();

        }

        @Test
        void givenInvalidId_whenGettingSum_thenReturn400BadRequest() {
            var invalidId = -100L;
            httpClient.get()
                    .uri("/transactions/sum/{id}", invalidId)
                    .exchange()
                    .expectStatus().isNotFound();

        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("cases")
        @DirtiesContext
        void givenLinearAndCyclicTransactionGraph_WhenGetTransactionSum_Then200Ok(
                String description,
                List<Transaction> seed,
                long startId,
                BigDecimal expectedSum) {
            // Arrange
            seed.forEach(repository::saveTransaction);

            // Act + Assert
            httpClient.get()
                    .uri("/transactions/sum/{id}", startId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.sum").isEqualTo(expectedSum.doubleValue());
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Linear chain: 10(5000) -> 11(10000) -> 12(5000)
                    arguments(
                            "linear: from root (10) -> 20000",
                            List.of(
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), null),
                                    new Transaction(11L, "shopping", BigDecimal.valueOf(10000), 10L),
                                    new Transaction(12L, "shopping", BigDecimal.valueOf(5000), 11L)),
                            10L, BigDecimal.valueOf(20000)),
                    arguments(
                            "linear: from middle (11) -> 15000",
                            List.of(
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), null),
                                    new Transaction(11L, "shopping", BigDecimal.valueOf(10000), 10L),
                                    new Transaction(12L, "shopping", BigDecimal.valueOf(5000), 11L)),
                            11L, BigDecimal.valueOf(15000)),
                    arguments(
                            "linear: from leaf (12) -> 5000",
                            List.of(
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), null),
                                    new Transaction(11L, "shopping", BigDecimal.valueOf(10000), 10L),
                                    new Transaction(12L, "shopping", BigDecimal.valueOf(5000), 11L)),
                            12L, BigDecimal.valueOf(5000)),
                    // Cycle: 10(5000)->11(10000)->12(5000)->10(...) ; count each node once
                    arguments(
                            "cycle: start at 10 -> 20000",
                            List.of(
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), null),
                                    new Transaction(11L, "shopping", BigDecimal.valueOf(10000), 10L),
                                    new Transaction(12L, "shopping", BigDecimal.valueOf(5000), 11L),
                                    // close the cycle by re-saving 10 with parent 12
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), 12L)),
                            10L, BigDecimal.valueOf(20000)),
                    arguments(
                            "cycle: start at 11 -> 20000",
                            List.of(
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), null),
                                    new Transaction(11L, "shopping", BigDecimal.valueOf(10000), 10L),
                                    new Transaction(12L, "shopping", BigDecimal.valueOf(5000), 11L),
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), 12L)),
                            11L, BigDecimal.valueOf(20000)),
                    arguments(
                            "cycle: start at 12 -> 20000",
                            List.of(
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), null),
                                    new Transaction(11L, "shopping", BigDecimal.valueOf(10000), 10L),
                                    new Transaction(12L, "shopping", BigDecimal.valueOf(5000), 11L),
                                    new Transaction(10L, "cars", BigDecimal.valueOf(5000), 12L)),
                            12L, BigDecimal.valueOf(20000)),
                    // Single node
                    arguments(
                            "single node",
                            List.of(new Transaction(99L, "cars", BigDecimal.valueOf(1234), null)),
                            99L, BigDecimal.valueOf(1234)),
                    // Disconnected graph sanity: sum only the reachable component
                    arguments(
                            "disconnected components",
                            List.of(
                                    new Transaction(1L, "cars", BigDecimal.valueOf(100), null),
                                    new Transaction(2L, "cars", BigDecimal.valueOf(200), 1L),
                                    new Transaction(7L, "cars", BigDecimal.valueOf(700), null) // separate island
                            ),
                            1L, BigDecimal.valueOf(300)));
        }
    }

}