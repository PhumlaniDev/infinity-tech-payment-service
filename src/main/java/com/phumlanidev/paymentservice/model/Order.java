package com.phumlanidev.paymentservice.model;

import com.phumlanidev.paymentservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment: this is the placeholder for documentation.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long orderId;
  @Column(name = "user_id")
  private String userId;
  @Column(name = "total_price")
  private BigDecimal totalPrice;
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private OrderStatus orderStatus;
  @Builder.Default
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderItem> items = new ArrayList<>();
  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;
  @CreatedBy
  @Column(updatable = false)
  private String createdBy;
  @LastModifiedDate
  @Column(insertable = false)
  private LocalDateTime updatedAt;
  @LastModifiedBy
  @Column(insertable = false)
  private String updatedBy;
//  @Column(name = "order_number")
//  private UUID orderNumber;
//  @Column(name = "payment_status")
//  private PaymentStatus paymentStatus;
}
