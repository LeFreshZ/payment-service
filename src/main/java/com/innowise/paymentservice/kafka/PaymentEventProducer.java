package com.innowise.paymentservice.kafka;

import com.innowise.paymentservice.dto.PaymentCompletedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentEventProducer {

  private static final String TOPIC = "payment-events";

  private final KafkaTemplate<String, PaymentCompletedEvent> template;

  public void sendPaymentEvent(PaymentCompletedEvent event) {
    template.send(TOPIC, event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.warn("Failed to send payment event with orderId={}, {}", event.getOrderId(),
                ex.getMessage());
          }
        });
  }
}
