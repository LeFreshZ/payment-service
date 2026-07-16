package com.innowise.paymentservice.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.paymentservice.dto.CreatePaymentRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8081)
@Testcontainers
public abstract class IntegrationTest {

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", TestContainersConfig.MONGO_DB_CONTAINER::getConnectionString);
    registry.add("spring.data.mongodb.database", () -> "payment_db_test");
    registry.add("spring.kafka.bootstrap-servers", TestContainersConfig.KAFKA_CONTAINER::getBootstrapServers);
    registry.add("random-api.url", () -> "http://localhost:8081");
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () ->
        "http://localhost:8081/.well-known/jwks.json");
  }

  @Autowired
  protected MockMvc mvc;

  @Autowired
  protected ObjectMapper mapper;

  @Autowired
  protected MongoTemplate template;

  @BeforeEach
  void setup() {
    WireMock.reset();
    template.getDb().drop();
    stubJwks();
  }

  protected void stubJwks() {
    stubFor(get(urlEqualTo("/.well-known/jwks.json"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"keys\":[]}")));
  }

  protected void stubRandomApiEven() {
    stubFor(get(urlPathMatching("/.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("42\n")));
  }

  protected void stubRandomApiOdd() {
    stubFor(get(urlPathMatching("/.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("43\n")));
  }

  protected void stubRandomApiUnavailable() {
    stubFor(get(urlPathMatching("/.*"))
        .willReturn(aResponse()
            .withStatus(500)));
  }

  protected String createPaymentRequest(Long orderId, BigDecimal amount)
      throws JsonProcessingException {
    CreatePaymentRequest request = new CreatePaymentRequest();

    request.setOrderId(orderId);
    request.setPaymentAmount(amount);

    return mapper.writeValueAsString(request);
  }
}
