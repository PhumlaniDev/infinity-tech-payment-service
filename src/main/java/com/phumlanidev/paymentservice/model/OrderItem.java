package com.phumlanidev.paymentservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Comment: this is the placeholder for documentation.
 */
@Entity
@Table
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long orderItemId;
  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private Order order; // foreign key
  @Column(name = "product_id")
  private Long productId; // foreign key
  @Column(name = "quantity")
  private Integer quantity;
  @Column(name = "price")
  private BigDecimal priceAtPurchase;


}