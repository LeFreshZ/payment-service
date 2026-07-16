package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.CreatePaymentRequest;
import com.innowise.paymentservice.dto.PaymentResponse;
import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Service interface for managing payments.
 *
 * <p>Provides operations for initiating payments, retrieving payment data,
 * and calculating payment summaries using MongoDB aggregation. Integrates with an external random
 * number API to simulate payment gateway logic.
 */
public interface PaymentService {

  /**
   * Initiates a new payment via REST API for the authenticated user.
   *
   * @param request DTO containing order_id and payment_amount
   * @param userId  ID of the authenticated user extracted from JWT
   * @return created payment as {@link PaymentResponse}
   */
  PaymentResponse createPayment(CreatePaymentRequest request, Long userId);

  /**
   * Processes a payment triggered by an order creation event from Kafka.
   *
   * <p>Creates a payment for the given order, calls external random number API
   * to determine final status, saves the result and publishes PAYMENT_COMPLETED event.
   *
   * @param orderId    ID of the order from CREATE_ORDER event
   * @param userId     ID of the user who created the order
   * @param totalPrice total amount to charge
   */
  void processOrderPayment(Long orderId, Long userId, BigDecimal totalPrice);

  /**
   * Retrieves a payment by its unique identifier.
   *
   * @param id      unique identifier of the payment
   * @param userId  ID of the authenticated user extracted from JWT
   * @param isAdmin true if the requesting user has ADMIN role
   * @return found payment as {@link PaymentResponse}
   * @throws com.innowise.paymentservice.exception.PaymentNotFoundException     if not found
   */
  PaymentResponse getPaymentById(String id, Long userId, boolean isAdmin);

  /**
   * Retrieves payments filtered dynamically by userId, orderId, or status.
   *
   * <p>For USER role, userId filter is always overridden with the authenticated user's ID.
   *
   * @param userId  optional filter by user_id
   * @param orderId optional filter by order_id
   * @param status  optional filter by payment status
   * @return list of matching payments as {@link PaymentResponse}
   */
  List<PaymentResponse> getPayments(Long userId, Long orderId, PaymentStatus status);

  /**
   * Calculates total sum of successful payments for a specific user in a date range.
   *
   * @param userId ID of the user
   * @param from   start of the date range (inclusive)
   * @param to     end of the date range (inclusive)
   * @return summary containing total successful payment amount
   */
  PaymentSummaryResponse getUserPaymentSummary(Long userId, Instant from, Instant to);

  /**
   * Calculates total sum of successful payments across all users in a date range.
   *
   * <p>Accessible by ADMIN only.
   *
   * @param from start of the date range (inclusive)
   * @param to   end of the date range (inclusive)
   * @return summary containing total successful payment amount for all users
   */
  PaymentSummaryResponse getAllUsersPaymentSummary(Instant from, Instant to);
}
