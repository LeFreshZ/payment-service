package com.innowise.paymentservice.service.implementation;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class RandomNumberClient {

  private final WebClient webClient;

  public RandomNumberClient(
      WebClient.Builder builder,
      @Value("${random-api.url}") String randomApiUrl
  ) {

    this.webClient = builder.baseUrl(randomApiUrl).build();
  }

  public int getRandomNumber() {
    try {
      String response = webClient.get()
          .uri(uriBuilder -> uriBuilder
              .queryParam("num", 1)
              .queryParam("min", 1)
              .queryParam("max", 1000)
              .queryParam("col", 1)
              .queryParam("base", 10)
              .queryParam("format", "plain")
              .queryParam("rnd", "new")
              .build())
          .retrieve()
          .bodyToMono(String.class)
          .block();

      return Integer.parseInt(response.trim());
    } catch (Exception ex) {
      log.warn("Random API unavailable, using fallback: {}", ex.getMessage());

      Random rnd = new Random();

      return rnd.nextInt(1, 1000);
    }
  }
}
