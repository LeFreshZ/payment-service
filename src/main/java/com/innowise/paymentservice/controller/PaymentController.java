package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.CreatePaymentRequest;
import com.innowise.paymentservice.dto.PaymentResponse;
import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.entity.enums.PaymentStatus;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentController {

  public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

  private final PaymentService service;

  @PostMapping
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<PaymentResponse> createPayment(
      @Valid @RequestBody CreatePaymentRequest request,
      Authentication authentication
  ) {

    Long userId = Long.parseLong(authentication.getName());

    return ResponseEntity.accepted().body(service.createPayment(request, userId));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<PaymentResponse> getPaymentById(
      @PathVariable String id,
      Authentication authentication
  ) {

    Long userId = Long.parseLong(authentication.getName());
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(ADMIN_ROLE_NAME));

    return ResponseEntity.ok(service.getPaymentById(id, userId, isAdmin));
  }

  @GetMapping
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<List<PaymentResponse>> getPayments(
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Long orderId,
      @RequestParam(required = false) PaymentStatus status,
      Authentication authentication
  ) {

    long currentUserId = Long.parseLong(authentication.getName());
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(ADMIN_ROLE_NAME));

    Long effectiveUserId = isAdmin ? userId : currentUserId;

    return ResponseEntity.ok(service.getPayments(effectiveUserId, orderId, status));
  }

  @GetMapping("/users/{userId}/summary")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<PaymentSummaryResponse> getUserPaymentSummary(
      @PathVariable Long userId,
      @RequestParam Instant from,
      @RequestParam Instant to,
      Authentication authentication
  ) {

    long currentUserId = Long.parseLong(authentication.getName());
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(ADMIN_ROLE_NAME));

    Long effectiveUserId = isAdmin ? userId : currentUserId;

    return ResponseEntity.ok(service.getUserPaymentSummary(effectiveUserId, from, to));
  }

  @GetMapping("/summary")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PaymentSummaryResponse> getAllUsersPaymentSummary(
      @RequestParam Instant from,
      @RequestParam Instant to
  ) {

    return ResponseEntity.ok(service.getAllUsersPaymentSummary(from, to));
  }
}
