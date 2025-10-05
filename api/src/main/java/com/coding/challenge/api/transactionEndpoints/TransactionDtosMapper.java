package com.coding.challenge.api.transactionEndpoints;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.coding.challenge.api.transactionEndpoints.dtos.requests.PutTransactionRequest;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionRequest;

@Mapper
public interface TransactionDtosMapper {

    public static final TransactionDtosMapper MAPPER = Mappers.getMapper(TransactionDtosMapper.class);

    
    @Mapping(source = "request.parentId", target = "parentTransactionId")
    CreateTransactionRequest toCreateTransactionRequest(PutTransactionRequest request, long id);

}
