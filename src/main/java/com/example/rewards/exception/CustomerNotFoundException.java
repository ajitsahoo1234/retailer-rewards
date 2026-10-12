package com.example.rewards.exception;

/** Signals that an explicitly requested customer does not exist. */
public class CustomerNotFoundException extends RuntimeException {
  /** Includes the missing customer ID in the diagnostic message. */
  public CustomerNotFoundException(long id) {
    super("Customer not found: " + id);
  }
}
