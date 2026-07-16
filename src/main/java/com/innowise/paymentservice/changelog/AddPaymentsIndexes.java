package com.innowise.paymentservice.changelog;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "add_payments_indexes", order = "002", author = "lefreshz")
public class AddPaymentsIndexes {

  @Execution
  public void execution(MongoDatabase database) {
    var collection = database.getCollection("payments");

    collection.createIndex(Indexes.ascending("user_id"));
    collection.createIndex(Indexes.ascending("order_id"));
    collection.createIndex(Indexes.ascending("status"));
  }

  @RollbackExecution
  public void rollback(MongoDatabase database) {
    var collection = database.getCollection("payments");

    collection.dropIndex("user_id_1");
    collection.dropIndex("order_id_1");
    collection.dropIndex("status_1");
  }
}
