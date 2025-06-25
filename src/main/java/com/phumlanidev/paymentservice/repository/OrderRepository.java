package com.phumlanidev.paymentservice.repository;

import com.phumlanidev.paymentservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment: this is the placeholder for documentation.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * Comment: this is the placeholder for documentation.
   */
  List<Order> findByUserId(String userId);

}
