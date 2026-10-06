package com.suresh.paymentsimulator.gateway.mapper;

import com.suresh.paymentsimulator.common.entity.Transaction;
import com.suresh.paymentsimulator.gateway.response.InitiatePaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InitiateTransactionResponseMapper {

    @Mapping(target = "amount",
            expression = "java(transaction.getAmount() != null ? transaction.getAmount().toPlainString() : null)")
    @Mapping(target = "id", source = "id")
    InitiatePaymentResponse map(Transaction transaction);
}