package com.coding.challenge.domain.transaction;

import java.math.BigDecimal;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {  
    private Long id;
    private String type;
    private BigDecimal amount;
    private Long parentTransactionId;
}
