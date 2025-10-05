package com.coding.challenge.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.transaction.TransactionRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionEndpointsTests {

    @LocalServerPort
    private int port;

    private WebTestClient httpClient;

    @BeforeEach
    void setup() {
        httpClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

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
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    // Expect empty JSON array
                    .expectBody()
                    .json("[]");

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
                    .expectStatus().isNotFound()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("ERR_NONEXISTENT_TYPE");

            // Verify repository was called once for that type
            verify(repository, times(1)).findTransactionIdsForType(nonExistentType);
        }

    }

    @Nested
    class GetTransactionChildsSumEndpointTests {

        @DirtiesContext
        @ParameterizedTest(name = "{0}")
        @CsvSource({
                "Suma todo arbol total","10, 20000", // sumamos desde base del arbol
                "Suma mitad del arbol","11, 15000" // sumamos desde raiz del arbol
        })
        void givenTransactionGraph_WhenGettingSum_ThenReturnExpectedTotal(String desc,long transactionId, BigDecimal expectedSum) {
            // Arrange
            repository.saveTransaction(new Transaction(10L, "cars", BigDecimal.valueOf(5000), null));
            repository.saveTransaction(new Transaction(11L, "shopping", BigDecimal.valueOf(10000), 10L));
            repository.saveTransaction(new Transaction(12L, "shopping", BigDecimal.valueOf(5000), 11L));
            Mockito.clearInvocations(repository);

            // Act + Assert
            httpClient.get()
                    .uri("/transactions/sum/{id}", transactionId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.sum").isEqualTo(expectedSum.doubleValue());
        }
    }

}