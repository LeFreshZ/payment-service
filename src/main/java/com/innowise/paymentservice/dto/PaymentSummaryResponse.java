package com.innowise.paymentservice.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentSummaryResponse {

  private BigDecimal totalAmount;
}
