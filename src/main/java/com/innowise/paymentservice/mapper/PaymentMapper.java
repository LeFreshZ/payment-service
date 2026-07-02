package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.CreatePaymentRequest;
import com.innowise.paymentservice.dto.PaymentResponse;
import com.innowise.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  Payment toEntity(CreatePaymentRequest request);

  PaymentResponse toResponse(Payment payment);
}
