package com.phumlanidev.paymentservice.exception.payment;

import com.phumlanidev.paymentservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BaseException {
  public PaymentNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}
