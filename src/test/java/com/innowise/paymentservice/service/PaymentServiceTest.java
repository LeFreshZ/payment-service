package com.innowise.paymentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.paymentservice.dao.PaymentDao;
import com.innowise.paymentservice.dto.CreatePaymentRequest;
import com.innowise.paymentservice.dto.PaymentResponse;
import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.enums.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentNotFoundException;
import com.innowise.paymentservice.kafka.PaymentEventProducer;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.service.implementation.PaymentServiceImpl;
import com.innowise.paymentservice.service.implementation.RandomNumberClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private PaymentDao dao;

  @Mock
  private RandomNumberClient randomNumberClient;

  @Mock
  private PaymentEventProducer eventProducer;

  private PaymentService service;

  private Payment payment;

  @BeforeEach
  void setup() {
    PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);
    service = new PaymentServiceImpl(dao, mapper, randomNumberClient, eventProducer);

    payment = new Payment();
    payment.setId("payment-id-1");
    payment.setOrderId(1L);
    payment.setUserId(1L);
    payment.setStatus(PaymentStatus.SUCCESS);
    payment.setTime(Instant.now());
    payment.setPaymentAmount(BigDecimal.valueOf(100.00));
  }

  @Test
  void shouldCreatePaymentWithSuccessStatus() {
    CreatePaymentRequest request = new CreatePaymentRequest();
    request.setOrderId(1L);
    request.setPaymentAmount(BigDecimal.valueOf(100.00));

    when(dao.save(any(Payment.class))).thenReturn(payment);
    when(randomNumberClient.getRandomNumber()).thenReturn(42);

    PaymentResponse response = service.createPayment(request, 1L);

    assertEquals("payment-id-1", response.getId());
    assertEquals(PaymentStatus.SUCCESS, response.getStatus());

    verify(dao, times(2)).save(any(Payment.class));
    verify(eventProducer).sendPaymentEvent(any());
  }

  @Test
  void shouldCreatePaymentWithFailedStatus() {
    CreatePaymentRequest request = new CreatePaymentRequest();
    request.setOrderId(1L);
    request.setPaymentAmount(BigDecimal.valueOf(100.00));

    Payment failedPayment = new Payment();
    payment.setId("payment-id-1");
    payment.setOrderId(1L);
    payment.setUserId(1L);
    payment.setStatus(PaymentStatus.FAILED);
    payment.setTime(Instant.now());
    payment.setPaymentAmount(BigDecimal.valueOf(100.00));

    when(dao.save(any(Payment.class))).thenReturn(failedPayment);
    when(randomNumberClient.getRandomNumber()).thenReturn(43);

    PaymentResponse response = service.createPayment(request, 1L);

    assertEquals(PaymentStatus.FAILED, response.getStatus());

    verify(eventProducer).sendPaymentEvent(any());
  }

  @Test
  void shouldGetPaymentByIdAsOwner() {
    when(dao.findById("payment-id-1")).thenReturn(Optional.of(payment));

    PaymentResponse response = service.getPaymentById("payment-id-1", 1L, false);

    assertEquals("payment-id-1", response.getId());
    assertEquals(1L, response.getUserId());
  }

  @Test
  void shouldGetPaymentByIdAsAdmin() {
    when(dao.findById("payment-id-1")).thenReturn(Optional.of(payment));


    PaymentResponse response = service.getPaymentById("payment-id-1", 99L, true);

    assertEquals("payment-id-1", response.getId());
  }

  @Test
  void shouldThrowWhenAnotherUser() {
    when(dao.findById("payment-id-1")).thenReturn(Optional.of(payment));

    assertThrows(AccessDeniedException.class, () -> service.getPaymentById("payment-id-1", 99L,
        false));
  }

  @Test
  void shouldThrowWhenPaymentNotFound() {
    when(dao.findById("wrong-id")).thenReturn(Optional.empty());

    assertThrows(PaymentNotFoundException.class, () -> service.getPaymentById("wrong-id", 1L,
        false));
  }

  @Test
  void shouldGetPaymentsByFilters() {
    when(dao.findByFilters(1L, null, null)).thenReturn(List.of(payment));

    List<PaymentResponse> responses = service.getPayments(1L, null, null);

    assertEquals(1, responses.size());
    assertEquals("payment-id-1", responses.get(0).getId());
  }

  @Test
  void shouldGetUserPaymentSummary() {
    Instant from = Instant.now().minusSeconds(3600);
    Instant to = Instant.now();

    when(dao.sumSuccessfulPaymentsForUser(1L, from, to)).thenReturn(BigDecimal.valueOf(500.00));

    PaymentSummaryResponse response = service.getUserPaymentSummary(1L, from, to);

    assertEquals(0, BigDecimal.valueOf(500.0).compareTo(response.getTotalAmount()));
  }

  @Test
  void shouldGetAllUsersPaymentSummary() {
    Instant from = Instant.now().minusSeconds(3600);
    Instant to = Instant.now();

    when(dao.sumSuccessfulPaymentsForAll(from, to)).thenReturn(BigDecimal.valueOf(1000.00));

    PaymentSummaryResponse response = service.getAllUsersPaymentSummary(from, to);

    assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(response.getTotalAmount()));
  }
}
