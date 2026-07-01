package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

  private final MongoTemplate template;

  @Override
  public BigDecimal sumSuccessfulPaymentsForUser(Long userId, Instant from, Instant to) {
    Criteria criteria = Criteria.where("user_id").is(userId)
        .and("status").is(PaymentStatus.SUCCESS)
        .and("time").gte(from).lte(to);

    return sumByCriteria(criteria);
  }

  @Override
  public BigDecimal sumSuccessfulPaymentsForAllUsers(Instant from, Instant to) {
    Criteria criteria = Criteria.where("status").is(PaymentStatus.SUCCESS)
        .and("time").gte(from).lte(to);

    return sumByCriteria(criteria);
  }

  private BigDecimal sumByCriteria(Criteria criteria) {
    MatchOperation match = Aggregation.match(criteria);

    GroupOperation group = Aggregation.group()
        .sum("payment_amount").as("total");

    Aggregation aggregation = Aggregation.newAggregation(match, group);

    AggregationResults<Document> results = template.aggregate(aggregation, "payments", Document.class);

    Document result = results.getUniqueMappedResult();

    if (result == null) {
      return BigDecimal.ZERO;
    }

    return new BigDecimal(result.get("total").toString());
  }
}
