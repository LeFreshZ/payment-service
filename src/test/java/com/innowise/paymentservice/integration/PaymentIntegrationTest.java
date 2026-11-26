package com.innowise.paymentservice.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.paymentservice.dto.PaymentResponse;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MvcResult;

class PaymentIntegrationTest extends IntegrationTest {

  @Test
  void shouldCreatePaymentWithSuccessStatus() throws Exception {
    stubRandomApiEven();

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.orderId").value(1L))
        .andExpect(jsonPath("$.userId").value(1L))
        .andExpect(jsonPath("$.paymentAmount").value(100.00));
  }

  @Test
  void shouldCreatePaymentWithFailedStatus() throws Exception {
    stubRandomApiOdd();

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("FAILED"));
  }

  @Test
  void shouldCreatePaymentWhenRandomApiUnavailable() throws Exception {
    stubRandomApiUnavailable();

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").exists());
  }

  @Test
  void shouldBe401WhenNoToken() throws Exception {
    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldBe400WhenInvalidRequest() throws Exception {
    String invalidRequest = "{\"orderId\": null, \"paymentAmount\": -100}";

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest)
            .with(jwt()
                .jwt(jwt -> jwt
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetPaymentByIdAsOwner() throws Exception {
    stubRandomApiEven();

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted())
        .andReturn();

    PaymentResponse payment = mapper.readValue(
        result.getResponse().getContentAsString(), PaymentResponse.class);

    mvc.perform(get("/payments/{id}", payment.getId())
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(payment.getId()));
  }

  @Test
  void shouldBe403WhenAnotherUser() throws Exception {
    stubRandomApiEven();

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted())
        .andReturn();

    PaymentResponse payment = mapper.readValue(
        result.getResponse().getContentAsString(), PaymentResponse.class);

    mvc.perform(get("/payments/{id}", payment.getId())
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("99")
                    .claim("userId", 99L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldGetPaymentByIdAsAdmin() throws Exception {
    stubRandomApiEven();

    MvcResult result = mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted())
        .andReturn();

    PaymentResponse payment = mapper.readValue(
        result.getResponse().getContentAsString(), PaymentResponse.class);

    mvc.perform(get("/payments/{id}", payment.getId())
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("99")
                    .claim("userId", 99L)
                    .claim("role", "ROLE_ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(payment.getId()));
  }

  @Test
  void shouldBe404WhenPaymentNotFound() throws Exception {
    mvc.perform(get("/payments/{id}", "nonexistent-id")
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldGetPaymentsFilteredByStatusForUser() throws Exception {
    stubRandomApiEven();

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted());

    mvc.perform(get("/payments")
            .param("status", "SUCCESS")
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("SUCCESS"))
        .andExpect(jsonPath("$[0].userId").value(1L));
  }

  @Test
  void shouldGetAllPaymentsAsAdmin() throws Exception {
    stubRandomApiEven();

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted());

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(2L, BigDecimal.valueOf(200.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("2")
                    .claim("userId", 2L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted());

    mvc.perform(get("/payments")
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("99")
                    .claim("userId", 99L)
                    .claim("role", "ROLE_ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void shouldGetUserPaymentSummary() throws Exception {
    stubRandomApiEven();

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted());

    mvc.perform(get("/payments/users/{userId}/summary", 1L)
            .param("from", Instant.now().minusSeconds(3600).toString())
            .param("to", Instant.now().plusSeconds(3600).toString())
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalAmount").value(100.00));
  }

  @Test
  void shouldGetAllUsersPaymentSummaryAsAdmin() throws Exception {
    stubRandomApiEven();

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(1L, BigDecimal.valueOf(100.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted());

    mvc.perform(post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createPaymentRequest(2L, BigDecimal.valueOf(200.00)))
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("2")
                    .claim("userId", 2L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isAccepted());

    mvc.perform(get("/payments/summary")
            .param("from", Instant.now().minusSeconds(10000).toString())
            .param("to", Instant.now().plusSeconds(10000).toString())
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("99")
                    .claim("userId", 99L)
                    .claim("role", "ROLE_ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalAmount").value(300.00));
  }

  @Test
  void shouldReturn403WhenUserAccessesSummaryEndpoint() throws Exception {
    mvc.perform(get("/payments/summary")
            .param("from", Instant.now().minusSeconds(3600).toString())
            .param("to", Instant.now().plusSeconds(3600).toString())
            .with(jwt()
                .jwt(jwt -> jwt
                    .subject("1")
                    .claim("userId", 1L)
                    .claim("role", "ROLE_USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }
}