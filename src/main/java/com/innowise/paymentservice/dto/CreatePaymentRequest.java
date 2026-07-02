package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

  @NotNull
  private Long orderId;

  @NotNull
  @Positive
  private BigDecimal paymentAmount;
}
