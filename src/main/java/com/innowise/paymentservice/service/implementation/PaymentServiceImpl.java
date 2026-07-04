package com.innowise.paymentservice.service.implementation;

import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.dto.CreatePaymentRequest;
import com.innowise.paymentservice.dto.PaymentCompletedEvent;
import com.innowise.paymentservice.dto.PaymentResponse;
import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.enums.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentNotFoundException;
import com.innowise.paymentservice.kafka.PaymentEventProducer;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.service.PaymentService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentDao dao;
  private final PaymentMapper mapper;
  private final RandomNumberClient randomNumberClient;
  private final PaymentEventProducer eventProducer;

  @Override
  public PaymentResponse createPayment(CreatePaymentRequest request, Long userId) {
    Payment payment = mapper.toEntity(request);
    payment.setUserId(userId);
    payment.setStatus(PaymentStatus.PENDING);
    payment.setTime(Instant.now());

    Payment savedPayment = dao.save(payment);

    int randomNumber = randomNumberClient.getRandomNumber();

    PaymentStatus finalStatus = randomNumber % 2 == 0
        ? PaymentStatus.SUCCESS
        : PaymentStatus.FAILED;

    savedPayment.setStatus(finalStatus);
    Payment updatedPayment = dao.save(savedPayment);

    eventProducer.sendPaymentEvent(
        new PaymentCompletedEvent(updatedPayment.getOrderId(), finalStatus));

    return mapper.toResponse(updatedPayment);
  }

  @Override
  public PaymentResponse getPaymentById(String id, Long userId, boolean isAdmin) {
    Optional<Payment> optionalPayment = dao.findById(id);

    if (optionalPayment.isEmpty()) {
      throw new PaymentNotFoundException("Payment not found with id: " + id);
    }

    Payment payment = optionalPayment.get();

    if (!isAdmin && !payment.getUserId().equals(userId)) {
      throw new AccessDeniedException("Access denied");
    }

    return mapper.toResponse(payment);
  }

  @Override
  public List<PaymentResponse> getPayments(Long userId, Long orderId, PaymentStatus status) {
    return dao.findByFilters(userId, orderId, status)
        .stream()
        .map(mapper::toResponse)
        .toList();
  }

  @Override
  public PaymentSummaryResponse getUserPaymentSummary(Long userId, Instant from, Instant to) {
    return new PaymentSummaryResponse(dao.sumSuccessfulPaymentsForUser(userId, from, to));
  }

  @Override
  public PaymentSummaryResponse getAllUsersPaymentSummary(Instant from, Instant to) {
    return new PaymentSummaryResponse(dao.sumSuccessfulPaymentsForAll(from, to));
  }
}
