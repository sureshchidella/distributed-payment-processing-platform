package com.suresh.paymentsimulator.gateway.mapper;

import com.suresh.paymentsimulator.common.entity.Transaction;
import com.suresh.paymentsimulator.gateway.request.InitiatePaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "sender.accountName", target = "senderName")
    @Mapping(source = "sender.accountNumber", target = "senderAccountNumber")
    @Mapping(source = "sender.ifscCode", target = "senderBankCode")
    @Mapping(source = "sender.address.street", target = "senderStreet")
    @Mapping(source = "sender.address.city", target = "senderCity")
    @Mapping(source = "sender.address.country", target = "senderCountry")
    @Mapping(source = "sender.address.zip", target = "senderPostalCode")
    @Mapping(source = "recipient.accountName", target = "recipientName")
    @Mapping(source = "recipient.accountNumber", target = "recipientAccountNumber")
    @Mapping(source = "recipient.ifscCode", target = "recipientBankCode")
    @Mapping(source = "recipient.address.street", target = "recipientStreet")
    @Mapping(source = "recipient.address.city", target = "recipientCity")
    @Mapping(source = "recipient.address.country", target = "recipientCountry")
    @Mapping(source = "recipient.address.zip", target = "recipientPostalCode")
    Transaction mapToEntity(InitiatePaymentRequest request);
}
