package com.coding.challenge.api.transactionEndpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.coding.challenge.api.common.dtos.response.StatusOkResponse;
import com.coding.challenge.api.transactionEndpoints.dtos.requests.PutTransactionRequest;
import com.coding.challenge.api.transactionEndpoints.dtos.responses.GetTransactionSumResponse;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionRequest;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionUseCase;
import com.coding.challenge.domain.useCases.transaction.findSumById.FindChildTransactionsSumUseCase;
import com.coding.challenge.domain.useCases.transaction.findTransactionsByType.FindTransactionIdsByTypeUseCase;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@RestController
public class TransactionController implements TransactionEndpointsDocumentation {

    private final FindTransactionIdsByTypeUseCase findTransactionIdsByTypeUseCase;
    private final CreateTransactionUseCase createTransactionUseCase;
    private final FindChildTransactionsSumUseCase findChildTransactionsSumUseCase;

    @Override
    public ResponseEntity<Set<Long>> getTransactionIdsByType(String type) {

        Set<Long> ids = findTransactionIdsByTypeUseCase.findIdsByType(type);

        if (ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ids);
        }

        return ResponseEntity.ok(ids);

    }

    @Override
    public ResponseEntity<GetTransactionSumResponse> getSum(long transactionId) {

        Optional<BigDecimal> maybeSum = findChildTransactionsSumUseCase.findTransitiveSum(transactionId);
        if (maybeSum.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new GetTransactionSumResponse(maybeSum.get()));
    }

    @Override
    public ResponseEntity<StatusOkResponse> putTransaction(long transactionId, PutTransactionRequest req) {
        CreateTransactionRequest saveTxReq = TransactionDtosMapper.MAPPER.toCreateTransactionRequest(req,
                transactionId);

                
        boolean isCreated = createTransactionUseCase.createTransaction(saveTxReq);
        if (isCreated) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .build()
                    .toUri();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(location)
                    .body(StatusOkResponse.ok());
        }

        return ResponseEntity.ok(StatusOkResponse.ok());

    }

}
