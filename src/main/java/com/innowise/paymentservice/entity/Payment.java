package com.innowise.paymentservice.entity;

import com.innowise.paymentservice.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "payments")
@Getter
@Setter
public class Payment {

  @Id
  private String id;

  @Indexed
  @Field("order_id")
  private Long orderId;

  @Indexed
  @Field("user_id")
  private Long userId;

  @Indexed
  private PaymentStatus status;

  private Instant time;

  @Field("payment_amount")
  private BigDecimal paymentAmount;
}
