package com.coding.challenge.domain.useCases.transaction.createTransaction;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTransactionRequest {
    private Long id;
    private String type;
    private BigDecimal amount;
    private Long parentTransactionId;
}
