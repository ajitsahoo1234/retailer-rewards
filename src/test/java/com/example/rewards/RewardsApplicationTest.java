package com.example.rewards;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.rewards.controller.RewardsController;
import com.example.rewards.repository.RewardsRepository;
import com.example.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/** Verifies application startup through the main method. */
@SpringBootTest(
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class RewardsApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    /** Confirms that startup succeeds and the main application beans exist. */
    @Test
    void shouldStartApplicationAndLoadRequiredBeans() {
        assertNotNull(applicationContext.getBean(RewardsController.class));
        assertNotNull(applicationContext.getBean(RewardsService.class));
        assertNotNull(applicationContext.getBean(RewardsRepository.class));
        assertNotNull(applicationContext.getBean(RewardCalculator.class));
    }
}