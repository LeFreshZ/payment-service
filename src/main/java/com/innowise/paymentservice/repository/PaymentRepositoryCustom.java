package com.innowise.paymentservice.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentRepositoryCustom {

  BigDecimal sumSuccessfulPaymentsForUser(Long userId, Instant from, Instant to);

  BigDecimal sumSuccessfulPaymentsForAllUsers(Instant from, Instant to);
}
