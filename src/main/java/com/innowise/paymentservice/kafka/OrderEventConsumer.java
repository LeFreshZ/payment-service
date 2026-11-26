package com.innowise.paymentservice.kafka;

import com.innowise.paymentservice.dto.OrderCreatedEvent;
import com.innowise.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class OrderEventConsumer {

  private PaymentService service;

  @KafkaListener(
      topics = "order-events",
      groupId = "payment-service-group"
  )
  public void handleOrderCreated(OrderCreatedEvent event) {
    service.processOrderPayment(
        event.getOrderId(),
        event.getUserId(),
        event.getTotalPrice()
    );
  }
}
