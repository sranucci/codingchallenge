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
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.coding.challenge.api.common.dtos.response.StatusOkResponse;
import com.coding.challenge.api.transactionEndpoints.dtos.requests.PutTransactionRequest;
import com.coding.challenge.api.transactionEndpoints.dtos.responses.GetTransactionSumResponse;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@Validated
public class TransactionController implements TransactionEndpointsDocumentation {



    @PutMapping("/{transaction_id}")
    @Override
    public StatusOkResponse putTransaction( long transactionId, @Valid PutTransactionRequest body) {
        // TODO Auto-generated method stub
        return null;
    }

    
    @GetMapping("/sum/{transaction_id}")
    @Override
    public GetTransactionSumResponse getSum(@PathVariable("transaction_id") @Min(1) long transactionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @GetMapping("/types/{type}")
    @Override
    public List<Long> getTransactionIdsByType(@NotBlank @Size(max = 64) String type) {
        // TODO Auto-generated method stub
        return null;
    }
}
