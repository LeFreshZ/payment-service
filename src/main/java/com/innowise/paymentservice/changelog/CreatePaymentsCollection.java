package com.innowise.paymentservice.changelog;

import com.mongodb.client.MongoDatabase;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "create-payments-collection", order = "001", author = "lefreshz")
public class CreatePaymentsCollection {

  @Execution
  public void execution(MongoDatabase database) {
    database.createCollection("payments");
  }

  @RollbackExecution
  public void rollback(MongoDatabase database) {
    database.getCollection("payments").drop();
  }
}
