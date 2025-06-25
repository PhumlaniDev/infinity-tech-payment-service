package com.phumlanidev.paymentservice.exception.order;


import com.phumlanidev.paymentservice.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
public class OrderAlreadyProcessedException extends BaseException {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public OrderAlreadyProcessedException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
