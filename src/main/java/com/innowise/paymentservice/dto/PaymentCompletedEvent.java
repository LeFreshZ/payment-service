package com.innowise.paymentservice.dto;

import com.innowise.paymentservice.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompletedEvent {

  private Long orderId;
  private PaymentStatus status;
}
