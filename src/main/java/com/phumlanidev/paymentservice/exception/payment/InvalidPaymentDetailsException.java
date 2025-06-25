package com.phumlanidev.paymentservice.exception.payment;


import com.phumlanidev.paymentservice.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Comment: this is the placeholder for documentation.
 */
public class InvalidPaymentDetailsException extends BaseException {

  /**
   * Comment: this is the placeholder for documentation.
   */
  public InvalidPaymentDetailsException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
