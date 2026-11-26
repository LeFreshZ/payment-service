package com.innowise.paymentservice.dto;

import com.innowise.paymentservice.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponse {

  private String id;
  private Long orderId;
  private Long userId;
  private PaymentStatus status;
  private Instant time;
  private BigDecimal paymentAmount;
}
