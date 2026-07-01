package com.innowise.paymentservice.dao;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.enums.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentDao {
  private final PaymentRepository repository;

  public Payment save(Payment payment) {
    return repository.save(payment);
  }

  public Optional<Payment> findById(String id) {
    return repository.findById(id);
  }

  public List<Payment> findByFilters(Long userId, Long orderId, PaymentStatus status) {
    return repository.findByFilters(userId, orderId, status);
  }

  public BigDecimal sumSuccessfulPaymentsForUser(Long userId, Instant from, Instant to) {
    return repository.sumSuccessfulPaymentsForUser(userId, from, to);
  }

  public BigDecimal sumSuccessfulPaymentsForAll(Instant from, Instant to) {
    return repository.sumSuccessfulPaymentsForAllUsers(from, to);
  }
}
