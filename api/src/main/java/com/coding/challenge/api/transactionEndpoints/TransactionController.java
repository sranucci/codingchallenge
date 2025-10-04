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

import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.coding.challenge.api.common.dtos.response.StatusOkResponse;
import com.coding.challenge.api.transactionEndpoints.dtos.requests.PutTransactionRequest;
import com.coding.challenge.api.transactionEndpoints.dtos.responses.GetTransactionSumResponse;

import java.util.List;


@RestController
public class TransactionController implements TransactionEndpointsDocumentation {




    @Override
    public ResponseEntity<List<Long>> getTransactionIdsByType(String type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionIdsByType'");
    }

    @Override
    public GetTransactionSumResponse getSum(long transactionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSum'");
    }

    @Override
    public ResponseEntity<StatusOkResponse> putTransaction(long transactionId,
            PutTransactionRequest body) {
        // TODO Auto-generated method stub
        return ResponseEntity.ok(StatusOkResponse.ok());
    }

    
    // @GetMapping("/sum/{transaction_id}")
    // @Override
    // public GetTransactionSumResponse getSum(@PathVariable("transaction_id") @Min(1) long transactionId) {
    //     // TODO Auto-generated method stub
    //     return null;
    // }

    // @GetMapping("/types/{type}")
    // @Override
    // public List<Long> getTransactionIdsByType(@NotBlank @Size(max = 64) String type) {
    //     // TODO Auto-generated method stub
    //     return null;
    // }
}
