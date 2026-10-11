package com.example.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the retailer rewards REST application. */
@SpringBootApplication
public class RewardsApplication {
  /** Launches Spring Boot with the supplied command-line arguments. */
  public static void main(String[] args) {
    SpringApplication.run(RewardsApplication.class, args);
  }
}
