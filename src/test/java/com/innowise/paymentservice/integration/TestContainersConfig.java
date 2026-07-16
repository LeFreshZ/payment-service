package com.innowise.paymentservice.integration;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainersConfig {

  public static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

  public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  static {
    MONGO_DB_CONTAINER.start();
    KAFKA_CONTAINER.start();
  }
}
