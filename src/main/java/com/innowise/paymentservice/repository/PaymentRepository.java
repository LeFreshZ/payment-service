package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.enums.PaymentStatus;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String>, PaymentRepositoryCustom {

  @Query("""
      {
      'user_id': ?#{[0] != null ? [0] : {$exists: true}},
      'order_id': ?#{[1] != null ? [1] : {$exists: true}},
      'status': ?#{[2] != null ? [2] : {$exists: true}}
      }
      """)
  List<Payment> findByFilters(Long userId, Long orderId, PaymentStatus status);
}
