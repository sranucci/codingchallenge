package com.coding.challenge.application.useCases.transaction.support;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.coding.challenge.domain.transaction.Transaction;
import com.coding.challenge.domain.useCases.transaction.createTransaction.CreateTransactionRequest;


public interface TransactionMapper {

    public static final TransactionMapper MAPPER = Mappers.getMapper(TransactionMapper.class);

    Transaction toTransaction(CreateTransactionRequest request);
}